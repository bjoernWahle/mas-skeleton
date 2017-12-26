package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.agent.ImasAgent;
import cat.urv.imas.onthology.ActionType;
import cat.urv.imas.onthology.InformAgentAction;
import cat.urv.imas.onthology.MobileAgentAction;
import cat.urv.imas.onthology.MoveAction;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CollectingActionsBehaviour extends SimpleBehaviour {
    DiggerCoordinatorAgent agent;
    long millis;
    long endTime;
    boolean finished = false;
    MessageTemplate messageTemplate;
    public CollectingActionsBehaviour(DiggerCoordinatorAgent agent, long millis) {
        this.agent = agent;
        this.millis = millis;

        messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);


    }

    @Override
    public void onStart() {
        super.onStart();
        endTime = System.currentTimeMillis() + millis;
        agent.log("Waiting for action informations");
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(messageTemplate);

        if(msg != null) {
            agent.log("Received message" + msg);
            try {
                ContentElement ce = agent.getContentManager().extractContent(msg);
                if(ce instanceof InformAgentAction) {
                    ActionType actionType = ActionType.fromString(((InformAgentAction) ce).getAction().getActionType());
                    switch (actionType) {
                        case MOVE:
                            agent.addRoundAction(((InformAgentAction) ce).getAction());
                            break;
                        case COLLECT:
                            // todo implement
                            break;
                        case RETURN:
                            // todo implement
                            break;
                        default:
                            throw new IllegalArgumentException("Illegal action type for a digger: " + actionType);
                    }
                }
            } catch (Codec.CodecException | OntologyException e) {
                agent.log("Not readable: " + msg);
                e.printStackTrace();
            }
        }

        if(System.currentTimeMillis() >= endTime) {
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
