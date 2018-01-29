package cat.urv.imas.behaviour.digger;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.MetalType;
import cat.urv.imas.onthology.ProposeTask;
import cat.urv.imas.onthology.TaskType;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class TaskContractNetResponder extends SimpleBehaviour {
    ContractNetResponder cnr;
    DiggerAgent agent;
    boolean finished = false;
    public TaskContractNetResponder(DiggerAgent agent) {
        this.agent = agent;
    }
    private int exitCode = 0;

    public void setLast() {
        this.exitCode = 1;
    }

    @Override
    public void action() {
        if(cnr == null) {
            cnr = new ContractNetResponder(agent, MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP))) {
                DiggerTask tempTask;

                @Override
                protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException { ;
                    try {
                        ContentElement ce = agent.getContentManager().extractContent(cfp);
                        if(ce instanceof ProposeTask) {
                            tempTask = ((ProposeTask) ce).getTask();
                        }
                    } catch (Codec.CodecException | OntologyException e) {
                        e.printStackTrace();
                        throw new NotUnderstoodException("not-understood");
                    }
                    if(tempTask.isLast()) {
                        agent.log("Thankfully this was the last one.");
                        setLast();
                    }
                    TaskType taskType = TaskType.fromString(tempTask.taskType);
                    MetalType metalType = MetalType.fromString(tempTask.metalType);
                    int capacityAfterCurrentTasks = agent.evaluateCapacityAfterCurrentTasks();
                    if (taskType == TaskType.COLLECT_METAL) {
                        if ((agent.getCurrentMetal() == null || metalType == agent.getCurrentMetal())
                                && capacityAfterCurrentTasks < agent.maxCapacity) {
                            // We provide a proposal
                            int time = agent.evaluateAction(tempTask.x, tempTask.y);
                            double percentage = Math.min((agent.maxCapacity - capacityAfterCurrentTasks) / ((double) tempTask.amount), 1.0);
                            String proposal = time + "," + percentage;
                            agent.log("Proposing " + proposal);
                            ACLMessage propose = cfp.createReply();
                            propose.setPerformative(ACLMessage.PROPOSE);
                            propose.setContent(proposal);
                            return propose;
                        } else {
                            // We refuse to provide a proposal
                            agent.log("Refusing to collect metal from (" +tempTask.x+","+tempTask.y +")");
                            finished = true;
                            throw new RefuseException("evaluation-failed");
                        }
                    } else {
                        throw new NotUnderstoodException("Didn't understand that task mate.");
                    }
                }

                @Override
                protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                    if (agent.checkIfTaskCanBeDone()) {
                        if(agent.getTasks().isEmpty()) {
                            agent.setCurrentMetal(MetalType.fromString(tempTask.metalType));
                        }
                        agent.addTask(tempTask);
                        ACLMessage inform = accept.createReply();
                        inform.setPerformative(ACLMessage.INFORM);
                        agent.log("Proposal accepted: " + tempTask);
                        finished = true;
                        return inform;
                    } else {
                        throw new FailureException("unexpected-error");
                    }
                }

                protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
                    agent.log("I didn't wanna dig that crap anyways!");
                    finished = true;
                }
            };
            agent.addBehaviour(cnr);
        }
    }

    @Override
    public boolean done() {
        return finished || (cnr != null && cnr.done());
    }

    @Override
    public int onEnd() {
        agent.log("Communication ended with exit code "+exitCode);
        int tempExitCode = exitCode;
        reset();
        return tempExitCode;
    }

    @Override
    public void reset() {
        super.reset();
        exitCode = 0;
        cnr = null;
        finished = false;
    }
}
