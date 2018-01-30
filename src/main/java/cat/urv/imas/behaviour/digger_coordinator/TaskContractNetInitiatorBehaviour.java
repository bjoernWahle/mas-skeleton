package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.CollectMetalBid;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.ProposeTask;
import com.sun.xml.internal.ws.api.message.Message;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.*;
import java.util.stream.Collectors;

public class TaskContractNetInitiatorBehaviour extends SimpleBehaviour {
    ContractNetInitiator cni;
    DiggerCoordinatorAgent agent;
    DiggerTask task;
    Map<AID, DiggerTask> diggerTasks = new HashMap<>();
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
                agent.getContentManager().fillContent(msg, new ProposeTask(tempTask));

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
                        List<ACLMessage> messages = new LinkedList<>();
                        Enumeration e = responses.elements();
                        List<CollectMetalBid> proposals = new LinkedList<>();
                        List<CollectMetalBid> partialProposals = new LinkedList<>();
                        while (e.hasMoreElements()) {
                            ACLMessage msg = (ACLMessage) e.nextElement();
                            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                                messages.add(msg);
                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                                acceptances.addElement(reply);
                                String proposal = msg.getContent();
                                int time = Integer.parseInt(proposal.split(",")[0]);
                                double capacity = Double.parseDouble(proposal.split(",")[1]);
                                CollectMetalBid collectMetalBid = new CollectMetalBid(msg.getSender(), time, capacity);
                                proposals.add(collectMetalBid);
                                if (collectMetalBid.getRemainingCapacity() == 1.0) {
                                    if(time > -1 && time < bestTime) {
                                        bestTime = time;
                                        bestProposal = collectMetalBid;
                                        accept = reply;
                                    }
                                } else {
                                    partialProposals.add(collectMetalBid);
                                }
                            }
                        }
                        // Accept the proposal of the best proposer
                        if (accept != null) {
                            diggerTasks.put(bestProposal.getAgent(), task);
                            agent.log("I am gonna accept proposal "+bestProposal+" from good ol' gold mining fella "+bestProposal.getAgent().getLocalName());
                            try {
                                agent.getContentManager().fillContent(accept, new ProposeTask(task));
                            } catch (Codec.CodecException | OntologyException e1) {
                                e1.printStackTrace();
                            }
                            accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        } else {
                            formCoalition(acceptances, messages, partialProposals);
                        }
                    }

                    protected void handleInform(ACLMessage inform) {
                        agent.log("Brave "+inform.getSender().getLocalName()+" gonna collect that metal. Big up fella!");
                        diggerTasks.get(inform.getSender()).startTask();
                    }
                };

            } catch (Codec.CodecException | OntologyException e) {
                e.printStackTrace();
                finished = true;
            }
            agent.addBehaviour(cni);
        }
    }

    private void formCoalition(Vector acceptances, List<ACLMessage> messages, List<CollectMetalBid> partialProposals) {
        Set<Set<CollectMetalBid>> possibleCoalitions = new HashSet<>();
        // create all permutations of 2 that can collect all the metal
        for(CollectMetalBid c1 : partialProposals) {
            double remainingMetal = 1.0 - c1.getRemainingCapacity();
            for(CollectMetalBid c2 : partialProposals) {
                if(c2 != c1 && c2.getRemainingCapacity() >= remainingMetal) {
                    Set<CollectMetalBid> coalition = new HashSet<>();
                    coalition.add(c1);
                    coalition.add(c2);
                    possibleCoalitions.add(coalition);
                }
            }
        }
        // find fastest permutation (minimum of the maximum of a coalition)
        Set<CollectMetalBid> bestCoalition = null;
        int bestCoalitionTime = Integer.MAX_VALUE;
        for(Set<CollectMetalBid> possibleCoalition : possibleCoalitions) {
            int maxTime = possibleCoalition.stream()
                    .max(Comparator.comparingInt(CollectMetalBid::getTime)).get().getTime();
            if(maxTime < bestCoalitionTime) {
                bestCoalition = possibleCoalition;
                bestCoalitionTime = maxTime;
            }
        }

        if(bestCoalition != null) {
            acceptances.clear();
            agent.log("Guys, my home boys "+bestCoalition.stream().map(c -> c.getAgent().getLocalName()).collect( Collectors.joining( ", " ) ) + " will dig that metal.");
            agent.removeTask(task);
            int remainingMetal = task.getAmount();
            for(CollectMetalBid cmb : bestCoalition) {
                DiggerTask partTask = new DiggerTask(task);
                int amount = Math.min((int) Math.ceil(cmb.getRemainingCapacity()*task.getAmount()), remainingMetal);
                remainingMetal = remainingMetal - amount;
                partTask.setAmount(amount);
                diggerTasks.put(cmb.getAgent(), partTask);
            }
            Set<AID> aids = diggerTasks.keySet();
            for(ACLMessage msg : messages) {
                if(aids.contains(msg.getSender())) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    try {
                        agent.getContentManager().fillContent(reply, new ProposeTask(diggerTasks.get(msg.getSender())));
                    } catch (Codec.CodecException | OntologyException e) {
                        e.printStackTrace();
                    }
                    acceptances.addElement(reply);
                } else {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                }
            }
        // no coalition could be formed, so go through the list of proposals again and find the one with the best ratio
        } else {
            double bestRatio = 0.0;
            CollectMetalBid bestProposal = null;
            for(CollectMetalBid cmb : partialProposals) {
                double ratio = cmb.getTime() / cmb.getRemainingCapacity();
                if(ratio < bestRatio) {
                    bestRatio = ratio;
                    bestProposal = cmb;
                }
            }
            if(bestProposal != null) {
                for (ACLMessage msg : messages) {
                    if (bestProposal.getAgent().equals(msg.getSender())) {
                        ACLMessage reply = msg.createReply();
                        try {
                            agent.getContentManager().fillContent(reply, new ProposeTask(diggerTasks.get(bestProposal.getAgent())));
                        } catch (Codec.CodecException | OntologyException e) {
                            e.printStackTrace();
                        }
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        acceptances.addElement(reply);
                    }
                }
            }
        }
    }

    @Override
    public boolean done() {
        return finished || (cni != null && cni.done());
    }
}
