package cat.urv.imas.behaviour.digger;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.MetalType;
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
    long timeEnd;
    boolean finished = false;
    public TaskContractNetResponder(DiggerAgent agent) {
        this.agent = agent;
    }

    @Override
    public void onStart() {
        timeEnd = agent.getRoundEnd()-agent.getRoundTime()/3;
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
                        if(ce instanceof DiggerTask) {
                            tempTask = (DiggerTask) ce;
                        }
                    } catch (Codec.CodecException | OntologyException e) {
                        e.printStackTrace();
                        throw new NotUnderstoodException("not-understood");
                    }
                    agent.log("CFP received from " + cfp.getSender().getLocalName() + ". Action is " + tempTask.taskType);
                    TaskType taskType = TaskType.fromString(tempTask.taskType);
                    MetalType metalType = MetalType.fromShortString(tempTask.metalType);
                    if (taskType == TaskType.COLLECT_METAL) {
                        if ((agent.getCurrentMetal() == null || metalType == agent.getCurrentMetal()) && agent.getCurrentCapacity() < agent.maxCapacity) {
                            agent.log("proposal");
                            // We provide a proposal
                            int time = agent.evaluateAction(tempTask.x, tempTask.y);
                            double percentage = Math.max((agent.maxCapacity - agent.getCurrentCapacity()) / tempTask.amount, 1.0);
                            String proposal = time + "," + percentage;
                            agent.log("Proposing " + proposal);
                            ACLMessage propose = cfp.createReply();
                            propose.setPerformative(ACLMessage.PROPOSE);
                            propose.setContent(proposal);
                            return propose;
                        } else {
                            // We refuse to provide a proposal
                            agent.log("Refusing " + cfp);
                            throw new RefuseException("evaluation-failed");
                        }
                    } else {
                        throw new NotUnderstoodException("Didn't understand that task mate.");
                    }
                }

                @Override
                protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
                    agent.log("Proposal accepted: " + accept);
                    if (agent.checkIfTaskCanBeDone()) {
                        agent.log("Starting action: CollectMetal");
                        agent.addTask(tempTask);
                        ACLMessage inform = accept.createReply();
                        inform.setPerformative(ACLMessage.INFORM);
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
            timeEnd = System.currentTimeMillis() + 10000;
        } else {
            // if behaviour already there and running, check that agent does not wait longer than 10 seconds
            if(System.currentTimeMillis() > timeEnd) {
                finished = true;
            }
        }
    }

    @Override
    public boolean done() {
        return finished || (cnr != null && cnr.done());
    }

    @Override
    public int onEnd() {
        reset();
        return super.onEnd();
    }

    @Override
    public void reset() {
        super.reset();
        timeEnd = -1;
        cnr = null;
        finished = false;
    }
}
