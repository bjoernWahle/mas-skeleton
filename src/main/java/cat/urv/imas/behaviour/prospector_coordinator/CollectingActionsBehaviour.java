package cat.urv.imas.behaviour.prospector_coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.ActionType;
import cat.urv.imas.onthology.FoundMetalsList;
import cat.urv.imas.onthology.InformAgentAction;
import cat.urv.imas.onthology.MobileAgentAction;
import cat.urv.imas.onthology.MoveAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.LinkedList;
import java.util.List;

public class CollectingActionsBehaviour extends SimpleBehaviour {
    ProspectorCoordinatorAgent agent;
    long millis;
    long endTime;
    boolean finished = false;
    List<AID> prospectors;
    MessageTemplate messageTemplate;
    
    public CollectingActionsBehaviour(ProspectorCoordinatorAgent agent, long millis) {
        this.agent = agent;
        this.millis = millis;
        messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    }

    @Override
    public void onStart() {
        super.onStart();
        prospectors = new LinkedList<>(agent.getProspectors());
        prospectors.addAll(agent.getProspectors());
        endTime = System.currentTimeMillis() + millis;
        agent.log("Waiting for action informations");
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(messageTemplate);

        if(msg != null) {
            try {
                AID sender = msg.getSender();
                if(prospectors.contains(sender)) {
                    agent.log("Received action from "+ sender.getLocalName());
                    ContentElement ce = agent.getContentManager().extractContent(msg);
                    if(ce instanceof InformAgentAction) {
                        MobileAgentAction action = ((InformAgentAction) ce).getAction();
                        ActionType actionType = ActionType.fromString(action.getActionType());
                        switch (actionType) {
                            case MOVE:
                            case IDLE:
                            case COLLECT:
                            case RETURN:
                                // add agent info
                                action.setAgent(agent.getGameSettings().getInfoAgent(AgentType.PROSPECTOR, sender));
                                agent.addRoundAction(action);
                                prospectors.remove(msg.getSender());
                                break;
                            case DETECT:
                            default:
                                throw new IllegalArgumentException("Illegal action type for a prospector: " + actionType);
                        }
                    }else if(ce instanceof FoundMetalsList) {
                    	if (((FoundMetalsList) ce).anyElements()) {
                    		agent.addFoundMetals(((FoundMetalsList) ce).getFoundMetalsList());
                    	}
                    	prospectors.remove(msg.getSender());
                    }
                } else {
                    agent.log("Receiving action from " + sender.getLocalName() + ". Either I do not know this Agent yet or he already told me about his action.");
                }

            } catch (Codec.CodecException | OntologyException e) {
                agent.log("Not readable: " + msg);
                e.printStackTrace();
            }
        }

        if(prospectors.isEmpty() || System.currentTimeMillis() >= endTime) {
            agent.informCoordinator();
            finished = true;
        }


    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public int onEnd() {
        this.reset();
        agent.log("Finished waiting for action informations");
        return super.onEnd();
    }

    @Override
    public void reset() {
        super.reset();
        finished = false;
        endTime = -1;
    }
}
