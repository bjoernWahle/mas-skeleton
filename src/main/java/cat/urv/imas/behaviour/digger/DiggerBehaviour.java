package cat.urv.imas.behaviour.digger;

import cat.urv.imas.agent.DiggerAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.GameSettings;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class DiggerBehaviour extends FSMBehaviour {

    private DiggerAgent agent;

    private String INIT = "init";
    private String ROUNDS = "rounds";
    private String END = "end";

    public DiggerBehaviour(DiggerAgent a) {
        super(a);
        this.agent = a;

        ReceiverBehaviour init = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM)) {

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                Object contentObject = null;
                try {
                    contentObject = m.getContentObject();
                    if(contentObject instanceof GameSettings) {
                        // set game
                        agent.log("I received the initial game settings. Let's go!");
                        agent.setGame((GameSettings) contentObject);
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }

            }
        };

        RoundBehaviour round = new RoundBehaviour(agent);

        OneShotBehaviour end = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.log("Digger behaviour has ended.");
            }
        };

        registerFirstState(init, INIT);
        registerState(round, ROUNDS);
        registerLastState(end, END);

        registerDefaultTransition(INIT, ROUNDS);
        registerDefaultTransition(ROUNDS, END);
    }

}
