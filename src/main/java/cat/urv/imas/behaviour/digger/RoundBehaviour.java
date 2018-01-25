package cat.urv.imas.behaviour.digger;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.*;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;

public class RoundBehaviour extends FSMBehaviour {
    DiggerAgent agent;

    private final String END = "end";
    private final String COMMUNICATING = "communicating";
    private final String PERFORMING = "performing";
    private final String WAITING = "waiting";

    public RoundBehaviour(DiggerAgent diggerAgent) {
        agent = diggerAgent;

        ReceiverBehaviour rs = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM), false) {
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

        TaskContractNetResponder communicationBehaviour = new TaskContractNetResponder(agent);

        OneShotBehaviour performBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.performAction();
            }
        };

        OneShotBehaviour endBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.log("I was told that the simulation has ended. Bye fellas.");
            }
        };


        registerFirstState(communicationBehaviour, COMMUNICATING);
        registerState(rs, WAITING);
        registerState(performBehaviour, PERFORMING);
        registerLastState(endBehaviour, END);

        registerTransition(WAITING, COMMUNICATING, 0);
        registerTransition(WAITING, END, 1);
        registerTransition(COMMUNICATING, COMMUNICATING, 1);
        registerTransition(COMMUNICATING, PERFORMING, 0);
        registerDefaultTransition(PERFORMING, WAITING);

    }



}
