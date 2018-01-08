package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.onthology.ActionList;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CollectingActionsAndStatsBehaviour extends SimpleBehaviour {
    private CoordinatorAgent agent;
    private MessageTemplate mt;
    private boolean diggerActionsReceived;
    private boolean diggerStatsReceived;
    private boolean prospectorActionsReceived;
    private boolean prospectorStatsReceived;

    CollectingActionsAndStatsBehaviour(CoordinatorAgent agent) {
        this.agent = agent;
        // TODO better message template
        this.mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    }
    private boolean finished = false;

    @Override
    public void onStart() {
        super.onStart();
        agent.log("Collection actions from my buddies.");
        this.reset();
        agent.resetDiggerActions();
        agent.resetProspectorActions();
    }

    @Override
    public void action() {
        ACLMessage msg = agent.receive(mt);
        if(msg != null) {
            agent.log("Received some actions broo: "+msg);
            AID sender = msg.getSender();
            agent.log("AID: "+sender);
	        if(!sender.equals(agent.systemAgent)) {
            	ContentElement ce = null;
	            try {
	                ce = agent.getContentManager().extractContent(msg);
	                // check who send message
	                if(sender.equals(agent.diggerCoordinatorAgent)) {
	                    // check if actions or stats
	                    if(ce instanceof ActionList) {
	                        agent.setDiggerActions((ActionList) ce);
	                        diggerActionsReceived = true;
	                    } else {
	                        // TODO stats
	                    }
	                } else if (sender.equals(agent.prospectorCoordinatorAgent)) {
	                    // check if actions or stats
	                    if(ce instanceof ActionList) {
	                        agent.setProspectorActions((ActionList) ce);
	                        prospectorActionsReceived = true;
	                    } else {
	                        // TODO stats
	                    }
	                } else {
	                    agent.log("Message from unknown sender: " + msg);
	                }
	            } catch (Codec.CodecException | OntologyException e) {
	                e.printStackTrace();
	                agent.log("Not readable message: " + msg);
	            }
	        }
        }
        // TODO time and actions (prospector) + stats
        if(diggerActionsReceived && prospectorActionsReceived) {
            finished = true;
        }

    }

    @Override
    public int onEnd() {
        this.reset();
        agent.log("Collecting actions and stats ended.");
        return super.onEnd();
    }

    @Override
    public boolean done() {
        return finished;
    }

    @Override
    public void reset() {
        super.reset();
        diggerActionsReceived = false;
        diggerStatsReceived = false;
        prospectorActionsReceived = false;
        prospectorStatsReceived = false;
        finished = false;
    }
}
