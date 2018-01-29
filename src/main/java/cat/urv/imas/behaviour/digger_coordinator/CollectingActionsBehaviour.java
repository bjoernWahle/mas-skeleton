package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.*;
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
    DiggerCoordinatorAgent agent;
    long millis;
    long endTime;
    boolean finished = false;
    List<AID> diggers;
    MessageTemplate messageTemplate;
    public CollectingActionsBehaviour(DiggerCoordinatorAgent agent, long millis) {
        this.agent = agent;
        this.millis = millis;

        messageTemplate = MessageTemplate.MatchPerformative(Performatives.INFORM_AGENT_ACTION);


    }

    @Override
    public void onStart() {
        super.onStart();
        diggers = new LinkedList<>(agent.getDiggers());
        endTime = System.currentTimeMillis() + millis;
        agent.log("Waiting for action informations");
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(messageTemplate);

        if(msg != null) {
            try {
                AID sender = msg.getSender();
                if(diggers.contains(sender)) {
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
                                action.setAgent(agent.getGameSettings().getInfoAgent(AgentType.DIGGER, sender));
                                agent.addRoundAction(action);
                                diggers.remove(msg.getSender());
                                break;
                            case DETECT:
                            default:
                                throw new IllegalArgumentException("Illegal action type for a digger: " + actionType);
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

        if(diggers.isEmpty() || (System.currentTimeMillis() >= endTime && msg == null)) {
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
