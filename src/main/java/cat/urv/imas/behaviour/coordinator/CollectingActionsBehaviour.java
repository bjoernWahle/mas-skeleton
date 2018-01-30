package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.onthology.ActionList;
import cat.urv.imas.onthology.GameSettings;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CollectingActionsBehaviour extends SimpleBehaviour {
    private CoordinatorAgent agent;
    private MessageTemplate mt;
    private boolean diggerActionsReceived;
    private boolean prospectorActionsReceived;

    CollectingActionsBehaviour(CoordinatorAgent agent) {
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
            AID sender = msg.getSender();
	        if(!sender.equals(agent.systemAgent)) {
            	ContentElement ce = null;
	            try {
	            	if(msg.getOntology() != null && msg.getOntology().equals("digger-ontology")) {
		                ce = agent.getContentManager().extractContent(msg);
		                // check who send message
		                if(sender.equals(agent.diggerCoordinatorAgent)) {
		                    // check if actions or stats
		                    if(ce instanceof ActionList) {
		                        agent.setDiggerActions((ActionList) ce);
		                        diggerActionsReceived = true;
		                    }
		                } else if (sender.equals(agent.prospectorCoordinatorAgent)) {
		                    // check if actions or stats
		                    if(ce instanceof ActionList) {
		                        agent.setProspectorActions((ActionList) ce);
		                        prospectorActionsReceived = true;
		                    }
		                } else {
		                    agent.log("Message from unknown sender: " + msg);
		                }
	            	}else {
	            		Object contentObject = msg.getContentObject();
	            		if (sender.equals(agent.prospectorCoordinatorAgent)) {
		                    if(contentObject instanceof GameSettings) {
		                    	//This is only used for printing the map with the area division done by the prospector coordinator.
		                        agent.informAboutMapDivision((GameSettings) contentObject);
		                    }
	            		}
	            	}
	            } catch (Codec.CodecException | OntologyException e) {
	                e.printStackTrace();
	                agent.log("Not readable message: " + msg);
	            } catch (UnreadableException e) {
					e.printStackTrace();
				}
	        }
        }
        if(diggerActionsReceived && prospectorActionsReceived) {
            finished = true;
        }

    }

    @Override
    public int onEnd() {
        this.reset();
        agent.log("Collecting actions ended.");
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
        prospectorActionsReceived = false;
        finished = false;
    }
}
