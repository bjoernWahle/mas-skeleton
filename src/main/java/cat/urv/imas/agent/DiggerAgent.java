package cat.urv.imas.agent;

import cat.urv.imas.behaviour.digger.RoundBehaviour;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DiggerAgent extends ImasAgent implements MovingAgentInterface  {

    public void startRound(RoundStart rs) {
        setCurrentPosition(rs.getX(),rs.getY());
        roundEnd = rs.getRoundEnd();
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

        logPosition();

        addBehaviour(new RoundBehaviour(this));
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
            }
        }
        TaskType currentTaskType = TaskType.fromString(currentTask.taskType);
        switch(currentTaskType) {
            case COLLECT_METAL:
                if(checkPosition(currentTask.x, currentTask.y)) {
                    if(currentCapacity < maxCapacity) {
                        collectMetal(currentTask.x, currentTask.y);
                    } else {
                        // TODO add return task to the start of the list
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

    private void doNothing() {
        log("I won't do anything this round. No tasks for me bro.");
        notifyDiggerCoordinator(new IdleAction(getAID()));
    }

    private void returnMetal(int x, int y) {
        // TODO set currentTask
        log("I will return my metal now");
    }

    private void notifyDiggerCoordinator(MobileAgentAction currentAction) {
        ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(diggerCoordinator);
        try {
            getContentManager().fillContent(message, new InformAgentAction(currentAction));
            log("Sending msg with my current action: " + message.getContent());
            send(message);
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
            if(pc.getCol() == currentX && pc.getRow() == currentY) {
                currentMovementPlan.dropFirst();
            }
        }
        PathCell pc = currentMovementPlan.getFirst();
        currentAction = new MoveAction(pc.getCol(), pc.getRow());
        log("I am on my way to "+x+","+y);
    }

    private Plan getPathTo(int x, int y) {
        List<PathCell> pathCells = new LinkedList<>();
        pathCells.add(new PathCell(2, 6));
        pathCells.add(new PathCell(2, 5));
        pathCells.add(new PathCell(1, 5));
        return new Plan(pathCells);
    }

    private void collectMetal(int x, int y) {
        // TODO set currentTask
        log("I gonna collect that metal now.");
    }

    private boolean checkPosition(int x, int y) {
        int yDistance = Math.abs (x - currentY);
        int xDistance = Math.abs (y - currentX);
        return yDistance + xDistance == 1;
    }

    private Optional<DiggerTask> getNextTask() {
        return tasks.stream().filter(task -> task.getCurrentState().equals(TaskState.NOT_STARTED.toString())).findFirst();
    }

    @Override
    public int stepsToPosition(int x, int y) {
        // easy approach: manhattan distance
        // TODO calculate path
        int yDistance = Math.abs (y - currentY);
        int xDistance = Math.abs (x - currentX);
        return xDistance+yDistance;
    }

    public void setCurrentPosition(int x, int y) {
        currentX = x;
        currentY = y;
    }

    public void logPosition() {
        log("I am at ("+ currentX +","+ currentY +")");
    }
}
