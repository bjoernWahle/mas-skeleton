package cat.urv.imas.agent;

import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.AgentList;
import cat.urv.imas.onthology.CollectMetalBid;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MetalType;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.util.*;
import java.util.stream.Collectors;

public class DiggerCoordinatorAgent extends ImasAgent {

    private List<AID> diggerAgents = new LinkedList<>();
    private int nResponders;

    public DiggerCoordinatorAgent() {
        super(AgentType.DIGGER_COORDINATOR);
    }

    @Override
    public void setup() {
        super.setup();

        // find digger agents

        addBehaviour(new SimpleBehaviour() {
            private boolean done = false;
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

                if (msg != null) {
                    try {
                        ContentElement contentElement = manager.extractContent(msg);
                        if(contentElement instanceof AgentList) {
                            AgentList agentList = (AgentList) contentElement;
                            diggerAgents = agentList.getAgentList().stream().filter(a -> a.getType() == AgentType.DIGGER).map(InfoAgent::getAID).collect(Collectors.toList());
                        }
                        // we received the list so now we can delegate
                        delegateCollectMetalTask(MetalType.GOLD, 3, 3, 3);
                        done = true;
                    } catch (Codec.CodecException e) {
                        e.printStackTrace();
                    } catch (OntologyException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public boolean done() {
                return done;
            }
        });

        // TODO implement and add behaviours
    }

    public void delegateCollectMetalTask(MetalType metalType, int amount, int x, int y) {
        nResponders = diggerAgents.size();
        log("Alriiiiight fellas I got some new metal. Who's keen to dig it?");

        // Fill the CFP message
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (AID diggerAgent : diggerAgents) {
            msg.addReceiver(diggerAgent);
        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // We want to receive a reply in 10 secs
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("CollectMetal("+metalType.getShortString()+","+amount+","+x+","+y+")");

        addBehaviour(new ContractNetInitiator(this, msg) {

            protected void handlePropose(ACLMessage propose, Vector v) {
                log("Agent "+propose.getSender().getLocalName()+" proposed "+propose.getContent());
            }

            protected void handleRefuse(ACLMessage refuse) {
                log("Agent "+refuse.getSender().getLocalName()+" refused");
            }

            protected void handleFailure(ACLMessage failure) {
                if (failure.getSender().equals(myAgent.getAMS())) {
                    // FAILURE notification from the JADE runtime: the receiver
                    // does not exist
                    log("Responder does not exist");
                }
                else {
                    log("Agent "+failure.getSender().getLocalName()+" failed");
                }
                // Immediate failure --> we will not receive a response from this agent
                nResponders--;
            }

            protected void handleAllResponses(Vector responses, Vector acceptances) {
                if (responses.size() < nResponders) {
                    // Some responder didn't reply within the specified timeout
                    log("Timeout expired: missing "+(nResponders - responses.size())+" responses");
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
                    log("I am gonna accept proposal "+bestProposal+" from good ol' gold mining fella "+bestProposal.getAgent().getLocalName());
                    accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                // otherwise start coalition formation process
                } else {
                    log("Aight mates let's form some COALITION!");
                }
            }

            protected void handleInform(ACLMessage inform) {
                log("Brave "+inform.getSender().getLocalName()+" gonna collect that metal. Big up fella!");
            }
        } );
    }

}
