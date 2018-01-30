package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.ActionList;
import cat.urv.imas.onthology.GameSettings;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.FSMBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class StepBehaviour extends FSMBehaviour {
    private SystemAgent agent;

    private static final String INIT = "INIT";
    private static final String ADVANCING = "ADVANCING";
    private static final String WAITING = "WAITING";
    private static final String END = "END";

    public StepBehaviour(SystemAgent systemAgent) {
        agent = systemAgent;
        ReceiverBehaviour waiting = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM), true) {
            @Override
            public void onStart() {
                super.onStart();
                agent.log("Starting round "+agent.getGame().getCurrentSimulationStep());
            }

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                try {
                	if(m.getOntology() != null && m.getOntology().equals("digger-ontology")) {
	                    ContentElement ce = agent.getContentManager().extractContent(m);
	                    if(ce instanceof ActionList) {
	                        ActionList agentActions = (ActionList) ce;
	                        // TODO later we should have one message with actions and stats
	                        agent.storeActions(agentActions);
	                    }
                	}else {
                		Object contentObject = m.getContentObject();
                		if(contentObject instanceof GameSettings) {
	                        //This is just for printing the division done by prospector coordinator to explore the map.
	                    	agent.getGame().setAreaDivision(((GameSettings) contentObject).getCellAssignement());
	                    }
                	}
                } catch (Codec.CodecException | OntologyException e) {
                    e.printStackTrace();
                } catch (UnreadableException e) {
					e.printStackTrace();
				}
            }
        };
        // we wait for the initialization of the game
        MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        registerFirstState(new RequestResponseBehaviour(agent, mt), INIT);
        registerState(waiting, WAITING);
        registerState(new AdvanceToNextRoundBehavior(agent), ADVANCING);
        registerLastState(new GameEndedBehaviour(agent), END);

        registerDefaultTransition(INIT, WAITING);
        registerDefaultTransition(WAITING, ADVANCING);
        registerTransition(ADVANCING, WAITING, 0);
        registerTransition(ADVANCING, END, 1);

    }

    public int onEnd() {
        agent.doDelete();
        return super.onEnd();
    }
}
