package cat.urv.imas.behaviour.prospector_coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.ActionType;
import cat.urv.imas.onthology.InformProspector;
import cat.urv.imas.onthology.MobileAgentAction;
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
    List<AID> prospectorsMoving;
    List<AID> prospectorsDetecting;
    MessageTemplate messageTemplate;
    
    public CollectingActionsBehaviour(ProspectorCoordinatorAgent agent, long millis) {
        this.agent = agent;
        this.millis = millis;
        messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    }

    @Override
    public void onStart() {
        super.onStart();
        prospectorsMoving = new LinkedList<>(agent.getProspectors());
        prospectorsDetecting = new LinkedList<>(agent.getProspectors());
        endTime = System.currentTimeMillis() + millis;
        agent.log("Waiting for action informations");
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(messageTemplate);

        if(msg != null) {
            try {
                AID sender = msg.getSender();
                if(prospectorsMoving.contains(sender) || prospectorsDetecting.contains(sender)) {
                    ContentElement ce = agent.getContentManager().extractContent(msg);
                    if(ce instanceof InformAgentAction) {
                    	MobileAgentAction action = ((InformAgentAction) ce).getAction();
                        ActionType actionType = ActionType.fromString(action.getActionType());
                        switch (actionType) {
                            case MOVE:
                            	// add agent info
                            	agent.log("Received movement action from "+ sender.getLocalName());
                                action.setAgent(agent.getGameSettings().getInfoAgent(AgentType.PROSPECTOR, sender));
                                agent.addRoundAction(action);
                                prospectorsMoving.remove(msg.getSender());
                                break;
                            case IDLE:
                            case COLLECT:
                            case DETECT:
                            	// add agent info
                            	agent.log("Received detection action from "+ sender.getLocalName());
                                action.setAgent(agent.getGameSettings().getInfoAgent(AgentType.PROSPECTOR, sender));
                                agent.addRoundAction(action);
                                prospectorsDetecting.remove(msg.getSender());
                                break;
                            case RETURN:
                            default:
                                throw new IllegalArgumentException("Illegal action type for a prospector: " + actionType);
                        }
                    }
                } else {
                    agent.log("Receiving action from " + sender.getLocalName() + ". Either I do not know this Agent yet or he already told me about his action.");
                }

            } catch (Codec.CodecException | OntologyException e) {
                agent.log("Not readable: " + msg);
                e.printStackTrace();
            }
        }

        if((prospectorsMoving.isEmpty() && prospectorsMoving.isEmpty()) || (System.currentTimeMillis() >= endTime && msg == null)) {
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
