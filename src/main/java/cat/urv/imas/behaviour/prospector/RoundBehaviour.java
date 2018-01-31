package cat.urv.imas.behaviour.prospector;

import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameHasEnded;
import cat.urv.imas.onthology.GameSettings;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RoundBehaviour extends FSMBehaviour {

	ProspectorAgent agent;
    
    private final String START = "start_round";
    private final String EXAMINE = "examine";
    private final String COMMUNICATE = "communicate";
    private final String MOVING = "moving";
    private final String END = "end";

    public RoundBehaviour(ProspectorAgent prospectorAgent) {
        agent = prospectorAgent;
        
        ReceiverBehaviour start = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM)) {

			@Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public int onEnd() {
                return super.onEnd();
            }

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                if(m.getOntology() != null && m.getOntology().equals("digger-ontology")) {
                    try {
                        ContentElement ce = agent.getContentManager().extractContent(m);
                        if(ce instanceof GameHasEnded) {
                            setExitCode(1);
                        }
                    } catch (Codec.CodecException | OntologyException e) {
                        e.printStackTrace();
                    }
                } else {
                    GameSettings game = null;
                    try {
                        game = (GameSettings) m.getContentObject();
                        agent.setGame(game);
                        Cell cell = game.getAgentCell(agent.getType(), agent.getAID());
                        // get area of exploration
                        agent.setCellsToExplore(game.getExplorationArea(agent.getAID()));
                        // get own position
                        agent.startRound(cell.getX(), cell.getY());
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                        agent.log("Content was not readable, sending old game status... Content:" + m);
                    }
                    setExitCode(0);
                }
            }
        };
        
        
        OneShotBehaviour examineBehaviour = new OneShotBehaviour() {

			@Override
            public void action() {
                agent.examine();
            }
        };
        
        OneShotBehaviour moveBehaviour = new OneShotBehaviour() {

			@Override
            public void action() {
                agent.moveNextCell();
            }
        };
        
        OneShotBehaviour end = new OneShotBehaviour() {
        	
			@Override
            public void action() {
                agent.log("I'm fed up of exploring fellas!");
            }
        };
        
        registerFirstState(start, START);
        registerState(examineBehaviour, EXAMINE);
        registerState(moveBehaviour,MOVING);
        registerLastState(end, END);
        
        registerTransition(START, EXAMINE, 0);
        registerTransition(START, END, 1);
        registerDefaultTransition(EXAMINE, MOVING);
        registerDefaultTransition(MOVING, START);
    }

}
