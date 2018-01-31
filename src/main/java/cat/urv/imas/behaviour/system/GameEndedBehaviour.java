package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.onthology.GameHasEnded;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * just respond to everything that the game is over
 */
public class GameEndedBehaviour extends SimpleBehaviour {
    private SystemAgent agent;
    GameEndedBehaviour(SystemAgent agent) {
        this.agent = agent;
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive();
        if(msg != null) {
            ACLMessage response = agent.prepareMessage(ACLMessage.INFORM);
            response.addReceiver(msg.getSender());
            try {
                agent.getContentManager().fillContent(response, new GameHasEnded());
                agent.send(response);
            } catch (Codec.CodecException e) {
                e.printStackTrace();
            } catch (OntologyException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
