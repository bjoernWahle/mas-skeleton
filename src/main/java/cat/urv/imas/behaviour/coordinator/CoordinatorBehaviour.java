package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.GameHasEnded;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class CoordinatorBehaviour extends FSMBehaviour {
    CoordinatorAgent agent;

    private String INIT = "init";
    private String COLLECTING = "collecting";
    private String SENDING = "sending";
    private String WAITING = "waiting";
    private String END = "end";

    public CoordinatorBehaviour(CoordinatorAgent coordinatorAgent) {
        this.agent = coordinatorAgent;

        //we add a behaviour that sends the message and waits for an answer (INIT)
        /* ********************************************************************/
        ACLMessage initialRequest = new ACLMessage(ACLMessage.REQUEST);
        initialRequest.clearAllReceiver();
        initialRequest.addReceiver(agent.systemAgent);
        initialRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to agent");
        initialRequest.setContent(MessageContent.GET_MAP);
        agent.log("Request message content:" + initialRequest.getContent());
        RequesterBehaviour init = new RequesterBehaviour(agent, initialRequest);
        registerFirstState(init, INIT);

        CollectingActionsBehaviour collect = new CollectingActionsBehaviour(agent);
        registerState(collect, COLLECTING);

        OneShotBehaviour send = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.notifySystemAgent();
            }
        };
        registerState(send, SENDING);

        // TODO MatchPerformative (protocol + only accept messages from SystemAgent)
        ReceiverBehaviour waiting = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM), false) {
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
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                        agent.log("Content was not readable, sending old game status... Content:" + m);
                    }

                    agent.broadcastCurrentGameStatus();
                    setExitCode(0);
                }
            }
        };

        registerState(waiting, WAITING);

        OneShotBehaviour end = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.log("I was told that the game has ended. Broadcasting those sad news now.");
                agent.broadCastGameHasEnded();
            }
        };

        registerState(end, END);

        registerDefaultTransition(INIT, COLLECTING);
        registerDefaultTransition(COLLECTING, SENDING);
        registerDefaultTransition(SENDING, WAITING);
        registerTransition(WAITING, COLLECTING, 0);
        registerTransition(WAITING, END, 1);
    }
}
