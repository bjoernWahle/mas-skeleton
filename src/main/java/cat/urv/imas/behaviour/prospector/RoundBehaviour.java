package cat.urv.imas.behaviour.prospector;

import java.io.IOException;

import cat.urv.imas.agent.ProspectorAgent;
import cat.urv.imas.behaviour.ReceiverBehaviour;
import cat.urv.imas.onthology.GameHasEnded;
import cat.urv.imas.onthology.RoundStart;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RoundBehaviour extends FSMBehaviour {
    ProspectorAgent agent;
    
    private final String START = "start_round";
    private final String EXAMINE = "examine";
    private final String COMMUNICATE = "communicate";
    private final String MOVING = "moving";
    private final String END = "end";

    public RoundBehaviour(ProspectorAgent prospectorAgent) {
        agent = prospectorAgent;
        
        ReceiverBehaviour start = new ReceiverBehaviour(agent, MessageTemplate.and(MessageTemplate.MatchOntology("digger-ontology"), MessageTemplate.MatchPerformative(ACLMessage.INFORM)), false) {
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
                try {
                    ContentElement ce = agent.getContentManager().extractContent(m);
                    if(ce instanceof RoundStart) {
                        RoundStart rs = (RoundStart) ce;
                        agent.startRound(rs);
                        setExitCode(0);

                    } else if (ce instanceof GameHasEnded) {
                        setExitCode(1);
                    }
                } catch (Codec.CodecException | OntologyException e) {
                    e.printStackTrace();
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
        
        OneShotBehaviour communicateBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.informCoordinator();
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
        registerState(communicateBehaviour, COMMUNICATE);
        registerLastState(end, END);
        
        registerTransition(START, EXAMINE, 0);
        registerTransition(START, END, 1);
        registerDefaultTransition(EXAMINE, MOVING);
        registerDefaultTransition(MOVING, COMMUNICATE);
        registerDefaultTransition(COMMUNICATE, START);
    }

}
