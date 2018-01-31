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
    private final String ROUND_START = "round_start";

    public RoundBehaviour(DiggerAgent diggerAgent) {
        agent = diggerAgent;

        ReceiverBehaviour rs = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM)) {
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
                        DiggerInfoAgent infoAgent = (DiggerInfoAgent) game.getInfoAgent(agent.getType(), agent.getAID());
                        Cell cell = game.getAgentCell(agent.getType(), agent.getAID());
                        // get own position
                        agent.startRound(cell.getX(), cell.getY(), infoAgent.getCapacity());
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

        ReceiverBehaviour roundStartBehaviour = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(Performatives.INFORM_ROUND_START)) {
            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                String content = m.getContent();
                if(content.equals(MessageContent.INFORM_NO_NEGOTIATION)) {
                    agent.log("I was informed that there is nothing to negotiate about this round.");
                    ACLMessage response = m.createReply();
                    response.setPerformative(ACLMessage.AGREE);
                    agent.send(response);
                    setExitCode(1);
                } else if(content.equals(MessageContent.INFORM_NEGOTIATION)) {
                    agent.log("Alright let's get ready for some serious business!");
                    ACLMessage response = m.createReply();
                    response.setPerformative(ACLMessage.AGREE);
                    agent.send(response);
                    setExitCode(0);
                }
            }
        };


        registerFirstState(roundStartBehaviour, ROUND_START);
        registerState(communicationBehaviour, COMMUNICATING);
        registerState(rs, WAITING);
        registerState(performBehaviour, PERFORMING);
        registerLastState(endBehaviour, END);

        registerTransition(WAITING, ROUND_START, 0);
        registerTransition(WAITING, END, 1);
        registerTransition(ROUND_START, COMMUNICATING, 0);
        registerTransition(ROUND_START, PERFORMING, 1);
        registerTransition(COMMUNICATING, COMMUNICATING, 0);
        registerTransition(COMMUNICATING, PERFORMING, 1);
        registerDefaultTransition(PERFORMING, WAITING);

    }



}
