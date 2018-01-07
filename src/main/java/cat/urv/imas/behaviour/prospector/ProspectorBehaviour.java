package cat.urv.imas.behaviour.prospector;

import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.GameSettings;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ProspectorBehaviour extends FSMBehaviour {

    private ProspectorAgent agent;

    private String INIT = "init";
    private String ROUND = "ROUND";
    private String END = "end";

    public ProspectorBehaviour(ProspectorAgent a) {
        super(a);
        this.agent = a;

        ReceiverBehaviour init = new ReceiverBehaviour(agent, MessageTemplate.MatchPerformative(ACLMessage.INFORM)) {

            @Override
            public void handle(ACLMessage m) {
                super.handle(m);
                agent.log("message: " + m);
                Object contentObject = null;
                try {
                    contentObject = m.getContentObject();
                    if(contentObject instanceof GameSettings) {
                        // set game settings
                        agent.log("I received the initial game settings");
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
                agent.log("Prospector behaviour has ended.");
            }
        };

        registerFirstState(init, INIT);
        //TODO: Add another state for exploring a subarea of the global map.
        registerState(round, ROUND);
        registerLastState(end, END);

        registerDefaultTransition(INIT, ROUND);
        registerDefaultTransition(ROUND, END);
    }
}

