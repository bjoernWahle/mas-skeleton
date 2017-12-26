package cat.urv.imas.agent;

import cat.urv.imas.agent.onthology.DiggerAction;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.MetalType;
import cat.urv.imas.onthology.Task;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DiggerAgent extends ImasAgent implements MovingAgentInterface  {

    public enum roundState {
        WAITING, COMMUNICATING, PERFORMING
    }

    private roundState currentRoundState;

    private static final int MAX_CAPACITY = 5;

    private AID diggerCoordinator;

    private int currentX;
    private int currentY;
    private int currentCapacity = 0;
    private MetalType currentMetal = null;

    private List<DiggerTask> tasks;

    private DiggerTask currentTask;

    private DiggerAction currentAction;

    public DiggerAgent() {
        super(AgentType.DIGGER);
    }

    @Override
    public void setup() {
        super.setup();

        // TODO implement and add behaviours
        // set starting position
        String[] args = (String[]) getArguments();
        currentX = Integer.parseInt(args[0]);
        currentY = Integer.parseInt(args[1]);

        tasks = new LinkedList<>();

        log("I am at ("+ currentX +","+ currentY +")");

        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP) );


        // TODO add FSM behaviour


        addBehaviour(new ContractNetResponder(this, template) {
            int tempX;
            int tempY;

            @Override
            protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
                String content = cfp.getContent();
                log("CFP received from "+cfp.getSender().getLocalName()+". Action is "+content);
                String task = content.substring(0, content.indexOf("("));
                if(task.equals("CollectMetal")) {
                    String[] args = content.substring(content.indexOf("(")+1, content.indexOf(")")).split(",");
                    MetalType metalType = MetalType.fromShortString(args[0]);
                    int units = Integer.parseInt(args[1]);
                    tempX = Integer.parseInt(args[2]);
                    tempY = Integer.parseInt(args[3]);
                    if ((currentMetal == null || metalType == currentMetal) && currentCapacity < MAX_CAPACITY) {
                        // We provide a proposal
                        int time = evaluateAction(tempX,tempY);
                        double percentage = Math.max((MAX_CAPACITY-currentCapacity)/units, 1.0);
                        String proposal = time+","+percentage;
                        log("Proposing "+proposal);
                        ACLMessage propose = cfp.createReply();
                        propose.setPerformative(ACLMessage.PROPOSE);
                        propose.setContent(proposal);
                        return propose;
                    }
                    else {
                        // We refuse to provide a proposal
                        log("Refusing "+cfp);
                        throw new RefuseException("evaluation-failed");
                    }
                } else {
                    throw new NotUnderstoodException("Didn't understand that task mate.");
                }
            }

            @Override
            protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
                log("Proposal accepted: "+accept);
                if (checkIfTaskCanBeDone()) {
                    log("Starting action: CollectMetal");
                    tasks.add(new DiggerTask(tempX, tempY,DiggerTask.TaskType.COLLECT_METAL));
                    ACLMessage inform = accept.createReply();
                    inform.setPerformative(ACLMessage.INFORM);
                    return inform;
                }
                else {
                    throw new FailureException("unexpected-error");
                }
            }

            protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                log("I didn't wanna dig that crap anyways!");
            }
        } );
    }

    private int evaluateAction(int x, int y) {
        return stepsToPosition(x, y);
    }

    private boolean checkIfTaskCanBeDone() {
        // TODO maybe return false if in the meanwhile the agent changed its mind (better offer or whatever)
        return true;
    }

    private void performAction() {
        if(currentTask == null) {
            Optional<DiggerTask> nextTask = getNextTask();
            if(!nextTask.isPresent()) {
                log("I am idle.");
                return;
            } else {
                currentTask = nextTask.get();
            }
        }
        switch(currentTask.getTaskType()) {
            case COLLECT_METAL:
                if(checkPosition(currentTask.x, currentTask.y)) {
                    if(currentCapacity < MAX_CAPACITY) {
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
        currentRoundState = roundState.WAITING;
    }

    private void returnMetal(int x, int y) {
        // TODO set currentTask
        log("I will return my metal now");
    }

    private void notifyDiggerCoordinator(DiggerAction currentAction) {
        ACLMessage message = prepareMessage(ACLMessage.INFORM, diggerCoordinator);
        try {
            getContentManager().fillContent(message, currentAction);
            send(message);
        } catch (Codec.CodecException e) {
            e.printStackTrace();
        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }

    private void moveTowards(int x, int y) {
        // TODO set currentTask
        log("I am on my way to "+x+","+y);
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
        return tasks.stream().filter(task -> task.getCurrentState() == Task.TaskState.NOT_STARTED).findFirst();
    }

    @Override
    public int stepsToPosition(int x, int y) {
        // easy approach: manhattan distance
        // TODO calculate path
        int yDistance = Math.abs (y - currentY);
        int xDistance = Math.abs (x - currentX);
        return xDistance+yDistance;
    }
}
