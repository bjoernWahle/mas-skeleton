package cat.urv.imas.agent;

import cat.urv.imas.behaviour.digger.DiggerBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DiggerAgent extends ImasAgent implements MovingAgentInterface  {

    public void startRound(int x, int y, int currentCapacity) {
        setCurrentPosition(x, y);
        this.currentCapacity = currentCapacity;
        roundEnd = game.getCurrentRoundEnd();
        logPosition();
    }

    public MetalType getCurrentMetal() {
        return currentMetal;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void addTask(DiggerTask diggerTask) {
        tasks.add(diggerTask);
    }

    public int maxCapacity;

    private AID diggerCoordinator;

    private int currentX;
    private int currentY;
    private int currentCapacity = 0;
    private MetalType currentMetal = null;

    private List<DiggerTask> tasks;

    private GameSettings game;

    private DiggerTask currentTask;
    private Plan currentMovementPlan;
    private long roundEnd;

    private MobileAgentAction currentAction;

    public DiggerAgent() {
        super(AgentType.DIGGER);
    }

    @Override
    public void setup() {
        super.setup();

        // find coordinator agent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinator = UtilsAgents.searchAgent(this, searchCriterion);

        // set starting position
        String[] args = (String[]) getArguments();
        currentX = Integer.parseInt(args[0]);
        currentY = Integer.parseInt(args[1]);
        maxCapacity = Integer.parseInt(args[2]);
        tasks = new LinkedList<>();

        addBehaviour(new DiggerBehaviour(this));
    }

    public int evaluateAction(int x, int y) {
        return stepsToPosition(x, y);
    }

    public boolean checkIfTaskCanBeDone() {
        // TODO maybe return false if in the meanwhile the agent changed its mind (better offer or whatever)
        return true;
    }

    public void performAction() {
        if(currentTask == null) {
            Optional<DiggerTask> nextTask = getNextTask();
            if(!nextTask.isPresent()) {
                doNothing();
                return;
            } else {
                currentTask = nextTask.get();
                if(currentTask.taskType.equals(TaskType.COLLECT_METAL.toString())) {
                    currentMetal = MetalType.fromString(currentTask.getMetalType());
                }
                currentTask.startTask();
            }
        }
        TaskType currentTaskType = TaskType.fromString(currentTask.taskType);
        switch(currentTaskType) {
            case COLLECT_METAL:
                if(checkPosition(currentTask.x, currentTask.y)) {
                    FieldCell fieldCell;
                    Cell cell = game.get(currentTask.y, currentTask.x);
                    if(cell instanceof FieldCell) {
                        fieldCell = (FieldCell) cell;
                    } else {
                        throw new IllegalArgumentException("Collect metal cells have to be always field cells.");
                    }
                    int metalCapacity;
                    if(!fieldCell.wasFound()) {
                        log("Cell " +fieldCell.toString() + " was not found.");
                    }
                    log(fieldCell.getMetal().toString());
                    metalCapacity = fieldCell.getMetalAmount();
                    if(currentCapacity < maxCapacity && metalCapacity > 0) {
                        collectMetal(currentTask.x, currentTask.y);
                    } else {
                        startReturnMetal();
                        log("I gonna go return that metal.");
                    }
                } else {
                    moveTowards(currentTask.x, currentTask.y);
                }
                break;
            case RETURN_METAL:
                if(checkPosition(currentTask.x, currentTask.y)) {
                    returnMetal(currentTask.x, currentTask.y);
                } else {
                    moveTowards(currentTask.x, currentTask.y);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown task type: "+currentTask.getTaskType());
        }
        // tell digger coordinator about plan
        notifyDiggerCoordinator(currentAction);
    }

    private void startReturnMetal() {
        // remove current task from the list
        tasks.remove(currentTask);
        // find closest manufacturing center
        List<Cell> mfcs = game.getManufacturingCenters();
        ManufacturingCenterCell bestManufacturingCenter = null;
        double bestRatio = 0;
        List<Cell> bestPath = null;
        Cell currentCell = game.get(currentY, currentX);
        for(Cell cell: mfcs) {
            ManufacturingCenterCell manufacturingCenter = (ManufacturingCenterCell) cell;
            if(manufacturingCenter.getMetal() != currentMetal) {
                continue;
            }
            List<Cell> pathNeighbors = new ArrayList<>(game.getPathNeighbors(cell));
            List<Cell> shortestPath = game.getMapGraph().getShortestPath(currentCell, pathNeighbors);
            double ratio = ((double) manufacturingCenter.getPrice()) / ((double) shortestPath.size());
            if(ratio > bestRatio) {
                bestRatio = ratio;
                bestPath = shortestPath;
                bestManufacturingCenter = manufacturingCenter;
            }
        }


        currentTask = new DiggerTask(bestManufacturingCenter.getX(), bestManufacturingCenter.getY(), TaskType.RETURN_METAL.toString(), currentMetal.toString(), currentCapacity);
        List<PathCell> pc = bestPath.stream().map(c -> (PathCell) c).collect(Collectors.toList());
        currentMovementPlan = new Plan(pc);
        moveTowards(bestManufacturingCenter.getX(), bestManufacturingCenter.getY());
    }

    private void doNothing() {
        log("I won't do anything this round. No tasks for me bro.");
        notifyDiggerCoordinator(new IdleAction(getAID()));
    }

    private void returnMetal(int x, int y) {
        currentAction = new ReturnMetalAction(x, y, currentCapacity, currentMetal.getShortString());
        log("I will return my metal now");
        tasks.remove(currentTask);
        // get next task
        Optional<DiggerTask> nextTask = getNextTask();
        if(nextTask.isPresent()) {
            currentTask = nextTask.get();
        } else {
            currentTask = null;
            currentMetal = null;
        }
    }

    private void notifyDiggerCoordinator(MobileAgentAction currentAction) {
        ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(diggerCoordinator);
        try {
            getContentManager().fillContent(message, new InformAgentAction(currentAction));
            log("Sending msg with my current action: " + message.getContent());
            send(message);
            this.currentAction = null;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Some error while sending?");
        }
    }

    private void moveTowards(int x, int y) {
        if(currentMovementPlan == null) {
            currentMovementPlan = getPathTo(x, y);
        } else {
            // check if I have moved
            PathCell pc = currentMovementPlan.getFirst();
            if(pc.getX() == currentX && pc.getY() == currentY) {
                currentMovementPlan.dropFirst();
            }
        }
        PathCell pc = currentMovementPlan.getFirst();
        currentAction = new MoveAction(pc.getX(), pc.getY());
        log("I am on my way to "+x+","+y);
    }

    private Plan getPathTo(int x, int y) {
        Cell currentCell = game.get(currentY, currentX);
        Cell destCell = game.get(y, x);

        List<Cell> pathNeighbors = new ArrayList<>(game.getPathNeighbors(destCell));

        List<Cell> vList = game.getMapGraph().getShortestPath(currentCell, pathNeighbors);
        List<PathCell> pc = vList.stream().map(c -> (PathCell) c).collect(Collectors.toList());
        return new Plan(pc);
    }

    private void collectMetal(int x, int y) {
        this.currentAction = new CollectMetalAction(x, y);
        log("I gonna collect that metal now.");
    }

    private boolean checkPosition(int x, int y) {
        int yDistance = Math.abs (x - currentX);
        int xDistance = Math.abs (y - currentY);
        return yDistance + xDistance == 1;
    }

    private Optional<DiggerTask> getNextTask() {
        return tasks.stream().filter(task -> task.getCurrentState().equals(TaskState.NOT_STARTED.toString())).findFirst();
    }

    @Override
    public int stepsToPosition(int x, int y) {
        return getPathTo(x, y).getPathCellList().size();
    }

    private void setCurrentPosition(int x, int y) {
        currentX = x;
        currentY = y;
    }

    private void logPosition() {
        log("I am at ("+ currentX +","+ currentY +")");
    }

    public long getRoundEnd() {
        return roundEnd;
    }

    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }

    public DiggerTask getCurrentTask() {
        return currentTask;
    }

    public List<DiggerTask> getTasks() {
        return tasks;
    }

    public void setCurrentTask(DiggerTask currentTask) {
        this.currentTask = currentTask;
    }
}
