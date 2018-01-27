package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.CollectMetalBid;
import cat.urv.imas.onthology.DiggerTask;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.*;

public class TaskContractNetInitiatorBehaviour extends SimpleBehaviour {
    ContractNetInitiator cni;
    DiggerCoordinatorAgent agent;
    DiggerTask task;
    boolean last = false;
    int nResponders;
    boolean finished = false;

    public TaskContractNetInitiatorBehaviour(DiggerCoordinatorAgent agent, DiggerTask task) {
        super(agent);
        this.agent = agent;
        this.task = task;
        this.last = false;
    }

    public TaskContractNetInitiatorBehaviour(DiggerCoordinatorAgent agent, DiggerTask task, boolean last) {
        super(agent);
        this.agent = agent;
        this.task = task;
        this.last = last;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void action() {
        if(cni == null) {
            nResponders = agent.getDiggers().size();

            agent.log("Alriiiiight fellas I got some new metal at ("+task.x+","+task.y+"). Who's keen to dig it?");

            // Fill the CFP message
            ACLMessage msg = new ACLMessage(ACLMessage.CFP);
            msg.setLanguage(ImasAgent.LANGUAGE);
            msg.setOntology(ImasAgent.ONTOLOGY);
            for (AID diggerAgent : agent.getDiggers()) {
                msg.addReceiver(diggerAgent);
            }
            msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // We want to receive a reply in 10 secs
            msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            try {
                DiggerTask tempTask = new DiggerTask(task);
                tempTask.setLast(last);
                agent.getContentManager().fillContent(msg, task);

                cni = new ContractNetInitiator(agent, msg) {

                    protected void handlePropose(ACLMessage propose, Vector v) {
                        agent.log("Agent "+propose.getSender().getLocalName()+" proposed "+propose.getContent());
                    }

                    protected void handleRefuse(ACLMessage refuse) {
                        agent.log("Agent "+refuse.getSender().getLocalName()+" refused");
                    }

                    protected void handleFailure(ACLMessage failure) {
                        if (failure.getSender().equals(myAgent.getAMS())) {
                            // FAILURE notification from the JADE runtime: the receiver
                            // does not exist
                            agent.log("Responder does not exist");
                        }
                        else {
                            agent.log("Agent "+failure.getSender().getLocalName()+" failed");
                        }
                        // Immediate failure --> we will not receive a response from this agent
                        nResponders--;
                    }

                    protected void handleAllResponses(Vector responses, Vector acceptances) {
                        if (responses.size() < nResponders) {
                            // Some responder didn't reply within the specified timeout
                            agent.log("Timeout expired: missing "+(nResponders - responses.size())+" responses");
                        }
                        // Evaluate proposals.
                        int bestTime = Integer.MAX_VALUE;
                        CollectMetalBid bestProposal = null;
                        ACLMessage accept = null;
                        Enumeration e = responses.elements();
                        List<CollectMetalBid> proposals = new LinkedList<>();
                        while (e.hasMoreElements()) {
                            ACLMessage msg = (ACLMessage) e.nextElement();
                            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                acceptances.addElement(reply);
                                String proposal = msg.getContent();
                                int time = Integer.parseInt(proposal.split(",")[0]);
                                double capacity = Double.parseDouble(proposal.split(",")[1]);
                                CollectMetalBid collectMetalBid = new CollectMetalBid(msg.getSender(), time, capacity);
                                proposals.add(collectMetalBid);
                                if (collectMetalBid.getRemainingCapacity() == 1.0 && time < bestTime) {
                                    bestTime = time;
                                    bestProposal = collectMetalBid;
                                    accept = reply;
                                }
                            }
                        }
                        // Accept the proposal of the best proposer
                        if (accept != null) {
                            agent.log("I am gonna accept proposal "+bestProposal+" from good ol' gold mining fella "+bestProposal.getAgent().getLocalName());
                            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                            // otherwise start coalition formation process
                        }
                    }

                    protected void handleInform(ACLMessage inform) {
                        agent.log("Brave "+inform.getSender().getLocalName()+" gonna collect that metal. Big up fella!");
                        task.startTask();
                    }
                };

            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
                finished = true;
            }
            agent.addBehaviour(cni);
        }
    }

    @Override
    public boolean done() {
        return finished || (cni != null && cni.done());
    }
}
