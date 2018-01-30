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

    private DiggerTask finishedTask;

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
        int currentPlanSteps = 0;
        int tempX = currentX;
        int tempY = currentY;
        for(DiggerTask task : tasks) {
            if(!task.getCurrentState().equals(TaskState.DONE.toString())) {
                Cell currentCell = game.get(tempY, tempX);
                Cell destCell = game.get(task.getY(),task.getX());
                Plan plan = game.getShortestPlan(currentCell, destCell);
                currentPlanSteps = currentPlanSteps + plan.getPathCellList().size();
                if(task.getTaskType().equals(TaskType.COLLECT_METAL.toString())) {
                    // add rounds needing for collecting the metal
                    currentPlanSteps = currentPlanSteps + task.getAmount();
                } else {
                    // for returning metal, we only need one round
                    currentPlanSteps = currentPlanSteps + 1;
                }
                if(!plan.getPathCellList().isEmpty()) {
                    tempX = plan.getPathCellList().get(plan.getPathCellList().size()-1).getX();
                    tempY = plan.getPathCellList().get(plan.getPathCellList().size()-1).getY();
                }
            }
        }
        Cell destCell = game.get(y, x);
        Cell cellAfterCurrentTasks = game.get(tempY, tempX);
        return currentPlanSteps + game.getShortestPlan(cellAfterCurrentTasks, destCell).getPathCellList().size();
    }

    public boolean checkIfTaskCanBeDone() {
        // TODO maybe return false if in the meanwhile the agent changed its mind (better offer or whatever)
        return true;
    }

    public void performAction() {
        if(currentTask == null || currentTask.isDone()) {
            Optional<DiggerTask> nextTask = getNextTask();
            if(!nextTask.isPresent()) {
                doNothing();
                return;
            } else {
                startTask(nextTask.get());
            }
        }
        performCurrentTask();
        // tell digger coordinator about plan
        notifyDiggerCoordinator();
    }

    private void finishCurrentTask() {
        currentTask.finishTask();
        finishedTask = currentTask;
    }

    private void startReturnMetal() {
        // find closest manufacturing center
        ManufacturingCenterCell bestManufacturingCenter = getBestManufacturingCenterCell();
        startTask(new DiggerTask(bestManufacturingCenter.getX(), bestManufacturingCenter.getY(), TaskType.RETURN_METAL.toString(), currentMetal.toString(), currentCapacity));
    }

    private ManufacturingCenterCell getBestManufacturingCenterCell() {
        Cell currentCell = game.get(currentY, currentX);
        return game.getClosestManufacturingCenter(currentCell, currentMetal);
    }

    private void doNothing() {
        log("I won't do anything this round. No tasks for me bro.");
        currentAction= new IdleAction();
        notifyDiggerCoordinator();
    }

    private void returnMetal(int x, int y) {
        currentMovementPlan = null;
        currentAction = new ReturnMetalAction(x, y, currentCapacity, currentMetal.getShortString());
        log("I will return my metal now");
        finishCurrentTask();
        // get next task
        Optional<DiggerTask> nextTask = getNextTask();
        if(nextTask.isPresent()) {
            currentTask = nextTask.get();
            currentTask.startTask();
        } else {
            currentTask = null;
            currentMetal = null;
        }
    }

    private void notifyDiggerCoordinator() {
        ACLMessage message = prepareMessage(Performatives.INFORM_AGENT_ACTION);
        message.addReceiver(diggerCoordinator);
        try {
            getContentManager().fillContent(message, new InformAgentRound(currentAction, finishedTask));
            log("Sending msg with my current action: " + message.getContent());
            send(message);
            this.currentAction = null;
            this.finishedTask = null;
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
        PathCell currentCell = (PathCell) game.get(currentY, currentX);
        PathCell pc = currentMovementPlan.getFirst();
        PathCell gpc = (PathCell) game.get(pc.getY(), pc.getX());
        if(gpc.isThereADiggerAgentWorking()) {
            // if there is a digger working, move somewhere else and update movement plan
            currentAction = new IdleAction();
//            List<PathCell> neighborCells = game.getPathNeighbors(game.get(currentY, currentX), false);
//            List<PathCell> possibleOtherPathCells = neighborCells
//                    .stream().filter(p -> !p.isThereADiggerAgentWorking()).collect(Collectors.toList());
//            Plan bestPlan = null;
//            PathCell bestPathCell = null;
//            for (PathCell pathCell : possibleOtherPathCells) {
//                Plan tempPlan = game.getShortestPlan(pathCell, game.get(y, x));
//                if (bestPlan == null || tempPlan.getSteps() < bestPlan.getSteps()) {
//                    bestPlan = tempPlan;
//                    bestPathCell = (PathCell) game.get(pathCell.getY(), pathCell.getX());
//                }
//            }
//            if (bestPlan == null) {
//                currentAction = new IdleAction();
//            } else {
//                currentMovementPlan = bestPlan;
//                currentAction = new MoveAction(bestPathCell.getX(), bestPathCell.getY());
//            }
        } else {
            currentAction = new MoveAction(gpc.getX(), gpc.getY());
        }
    }

    private Plan getPathTo(int x, int y) {
        Cell currentCell = game.get(currentY, currentX);
        Cell destCell = game.get(y, x);

        return game.getShortestPlan(currentCell, destCell);
    }

    private void collectMetal(int x, int y) {
        FieldCell fieldCell;
        Cell cell = game.get(currentTask.y, currentTask.x);
        if(cell instanceof FieldCell) {
            fieldCell = (FieldCell) cell;
        } else {
            throw new IllegalArgumentException("Collect metal cells have to be always field cells.");
        }
        PathCell currentCell = (PathCell) game.get(currentY, currentX);
        int metalCapacity;
        metalCapacity = fieldCell.getMetalAmount();
        if(currentCapacity < maxCapacity && metalCapacity > 0) {
            if(currentCell.collectingAllowed()) {
                this.currentAction = new CollectMetalAction(x, y);
                log("I gonna collect that metal now.");
            } else {
                // only move if it is a digger, prospectors will move anyways next round
                List<InfoAgent> otherAgents = currentCell.getAgents().get(AgentType.DIGGER).stream()
                        .filter(a -> !a.getAID().equals(getAID())).collect(Collectors.toList());
                if(otherAgents.isEmpty()) {
                    this.currentAction = new IdleAction();
                    log("I can't collect metal because there is some a prospector at my cell. Go away dude.");
                } else {
                    // check if I might need to move or not
                    // I will not move, if I have the highest hashcode (implicit coordination yay)
                    int myHashCode = getAID().hashCode();
                    if(otherAgents.stream().allMatch(ia -> ia.getAID().hashCode() < myHashCode)) {
                        this.currentAction = new CollectMetalAction(x, y);
                    } else {
                        List<PathCell> pathNeighbors = game.getPathNeighbors(fieldCell, true);
                        List<PathCell> possibleNeighborCells = pathNeighbors.stream()
                                .filter(pn -> (pn.collectingAllowed() && pn.adjacent(currentCell, false))).collect(Collectors.toList());
                        if(!possibleNeighborCells.isEmpty()) {
                            PathCell newTargetCell = possibleNeighborCells.get(0);
                            currentAction = new MoveAction(newTargetCell.getX(), newTargetCell.getY());
                        }
                    }
                }
            }
        } else {
            if(currentCapacity < maxCapacity) {
                // we are done with the task
                // remove current task from the list
                finishCurrentTask();
                // see if another is available
                Optional<DiggerTask> nextTask = getNextTask();
                if(nextTask.isPresent()) {
                    startTask(nextTask.get());
                } else {
                    startReturnMetal();
                }
            } else {
                // we are not done but we can't carry more
                finishCurrentTask();
                startReturnMetal();
            }
        }
    }

    private boolean isAdjacentTo(int x, int y, boolean diagonally) {
        int dy = Math.abs (x - currentX);
        int dx = Math.abs (y - currentY);
        if(diagonally) {
            return (dx<=1 && dy<=1 && dx + dy >=1);
        } else {
            return dx + dy == 1;
        }
    }

    public void startTask(DiggerTask task) {
        setCurrentTask(task);
        currentTask.startTask();
        if(currentTask.isCollectTask()) {
            currentMetal = currentTask.getMetal();
        }

        performCurrentTask();
    }

    public void performCurrentTask() {
        // check if we might be already there
        if(isAdjacentTo(currentTask.getX(), currentTask.getY(), true)) {
            currentMovementPlan = null;
            if(currentTask.getTaskType().equals(TaskType.COLLECT_METAL.toString())) {
                collectMetal(currentTask.getX(), currentTask.getY());
            } else {
                returnMetal(currentTask.getX(), currentTask.getY());
            }
        } else {
            moveTowards(currentTask.getX(), currentTask.getY());
        }
    }

    private Optional<DiggerTask> getNextTask() {
        return tasks.stream().filter(task -> task.getCurrentState().equals(TaskState.NOT_STARTED.toString())).findFirst();
    }

    @Override
    public int stepsToPosition(int x, int y) {
        Plan shortestPathPlan = getPathTo(x, y);
        if(shortestPathPlan != null) {
            return shortestPathPlan.getPathCellList().size();
        } else {
            return -1;
        }
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

    public void setCurrentMetal(MetalType currentMetal) {
        this.currentMetal = currentMetal;
    }

    public int evaluateCapacityAfterCurrentTasks() {
        int tempCapacity = currentCapacity;
        for(DiggerTask task : tasks) {
            if(!task.isDone()) {
                if(task.getTaskType().equals(TaskType.COLLECT_METAL.toString())) {
                    tempCapacity = Math.min(tempCapacity + task.getAmount(), maxCapacity);
                } else {
                    // if its return metal, capacity is set to 0
                    tempCapacity = 0;
                }
            }
        }
        return tempCapacity;
    }
}
