package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
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
    private DiggerCoordinatorAgent agent;

    private final String COMMUNICATING = "communicating";
    private final String COLLECTING = "collecting";
    private final String WAITING = "waiting";
    private final String END = "end";


    public RoundBehaviour(DiggerCoordinatorAgent a) {
        super(a);
        this.agent = a;

        ReceiverBehaviour waiting = new ReceiverBehaviour(a,  MessageTemplate.MatchPerformative(ACLMessage.INFORM), false) {
            @Override
            public void handle(ACLMessage m) {
                agent.log("Received message.");
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
                    try {
                        Object contentObject = m.getContentObject();
                        if(contentObject instanceof GameSettings) {
                            // set game
                            agent.log("I received the game settings for this round.");
                            agent.setGameSettings((GameSettings) contentObject);
                            agent.informDiggers();
                            agent.resetRoundActions();
                            setExitCode(0);
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        DelegateTasksBehaviour communicationBehaviour = new DelegateTasksBehaviour(agent);

        CollectingActionsBehaviour ca = new CollectingActionsBehaviour(agent, 5000);


        OneShotBehaviour endBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.log("I have been told that the simulation has ended. Was great to work with y'all mates.");
            }
        };

        registerFirstState(waiting, WAITING);
        registerState(communicationBehaviour, COMMUNICATING);
        registerState(ca, COLLECTING);
        registerLastState(endBehaviour, END);

        registerTransition(WAITING, COMMUNICATING, 0);
        registerTransition(WAITING, END, 1);
        registerDefaultTransition(COMMUNICATING, COLLECTING);
        registerDefaultTransition(COLLECTING, WAITING);
    }
}
