package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.onthology.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DelegateTasksBehaviour extends FSMBehaviour {
    DiggerCoordinatorAgent agent;

    private final String PLANNING = "planning";
    private final String NEGOTIATING = "negotiating";
    private final String BUILDING_COALITION = "building_coalition"; // TODO add behaviour
    private final String END = "end";

    @Override
    public void onStart() {
        super.onStart();
        agent.log("Start delegating tasks.");
    }

    public DelegateTasksBehaviour(DiggerCoordinatorAgent agent) {
        super(agent);
        this.agent = agent;

        SimpleBehaviour planningBehaviour = new SimpleBehaviour() {
            boolean finished= false;
            boolean negotiation = false;
            List<AID> receivers;

            public void onStart() {
                finished = false;
                if(agent.mfcDistances == null) {
                    agent.initManufacturingCenterDistances();
                }
                for(FieldCell foundCell: agent.getGameSettings().getFoundMetals()) {
                    if (!agent.getMetalsBeingCollected().contains(foundCell)) {
                        agent.addTask(new DiggerTask(foundCell.getX(), foundCell.getY()
                                , TaskType.COLLECT_METAL.toString(), foundCell.getMetalType().getShortString()
                                , foundCell.getMetalAmount()));
                    }
                }
                agent.sortDiggerTasks();
                receivers = new LinkedList<>();
                receivers.addAll(agent.getDiggers());
                if(agent.getNotStartedTasks().isEmpty()) {
                    agent.broadcastSimpleMessageToDiggers(Performatives.INFORM_ROUND_START, MessageContent.INFORM_NO_NEGOTIATION);
                    negotiation = false;
                } else {
                    agent.broadcastSimpleMessageToDiggers(Performatives.INFORM_ROUND_START, MessageContent.INFORM_NEGOTIATION);
                    negotiation = true;
                }
            }

            @Override
            public void action() {
                ACLMessage message = agent.receive(MessageTemplate.MatchPerformative(ACLMessage.AGREE));
                if(message != null) {
                    handleMessage(message);
                }
            }

            public void handleMessage(ACLMessage message) {
                agent.log("Received message from "+ message.getSender());
                AID sender = message.getSender();
                receivers.remove(sender);
                if(receivers.isEmpty()) {
                    finished = true;
                }

            }

            @Override
            public boolean done() {
                return finished;
            }

            @Override
            public void reset() {
                super.reset();
                finished = false;
                negotiation = false;
                receivers = null;
            }

            @Override
            public int onEnd() {
                if(negotiation) {
                    this.reset();
                    return 0;
                } else {
                    this.reset();
                    return 1;
                }

            }
        };

        NegotiatorBehaviour negotiationBehaviour = new NegotiatorBehaviour(agent);

        OneShotBehaviour endBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                agent.log("Delegating tasks has ended.");
            }
        };

        registerFirstState(planningBehaviour, PLANNING);
        registerState(negotiationBehaviour, NEGOTIATING);
        registerLastState(endBehaviour, END);

        registerTransition(PLANNING, NEGOTIATING, 0);
        registerTransition(PLANNING, END, 1);
        registerDefaultTransition(NEGOTIATING, END);

    }

    @Override
    public int onEnd() {
        this.reset();
        return super.onEnd();

    }
}
