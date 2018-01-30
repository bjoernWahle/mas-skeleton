package cat.urv.imas.behaviour.prospector_coordinator;

import cat.urv.imas.agent.ProspectorCoordinatorAgent;
import cat.urv.imas.onthology.InformProspectorInitialization;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.LinkedList;
import java.util.List;

public class CollectingInitializationBehaviour extends SimpleBehaviour {
    ProspectorCoordinatorAgent agent;
    long millis;
    long endTime;
    boolean finished = false;
    List<AID> prospectors;
    MessageTemplate messageTemplate;
    
    public CollectingInitializationBehaviour(ProspectorCoordinatorAgent agent, long millis) {
        this.agent = agent;
        this.millis = millis;
        messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
    }

    @Override
    public void onStart() {
        super.onStart();
        prospectors = new LinkedList<>(agent.getProspectors());
        endTime = System.currentTimeMillis() + millis;
        agent.log("Waiting for prospectors initialization");
    }

    @Override
    public void action() {

        ACLMessage msg = agent.receive(messageTemplate);

        if(msg != null) {
            try {
                AID sender = msg.getSender();
                if(prospectors.contains(sender)) {
                    ContentElement ce = agent.getContentManager().extractContent(msg);
                    if(ce instanceof InformProspectorInitialization) {
                    	List<Long> preferenceOrder = ((InformProspectorInitialization) ce).getDistances();
                    	
                    	for(Long element: preferenceOrder) {
                    		if(!agent.areaAssignament.containsValue(element.intValue())) {
                    			agent.areaAssignament.put(msg.getSender(), element.intValue());
                    			break;
                    		}	
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

        if((prospectors.isEmpty()) /*|| (System.currentTimeMillis() >= endTime && msg == null)*/) {
            agent.setInitialized(true);
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
        agent.log("Finished initialization");
        return super.onEnd();
    }

    @Override
    public void reset() {
        super.reset();
        finished = false;
        endTime = -1;
    }
}
