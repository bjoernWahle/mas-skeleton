package cat.urv.imas.behaviour.digger_coordinator;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.agent.DiggerCoordinatorAgent;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.MetalType;
import cat.urv.imas.onthology.TaskType;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;

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

        OneShotBehaviour planningBehaviour = new OneShotBehaviour() {
            @Override
            public void action() {
                for(FieldCell foundCell: agent.getGameSettings().getFoundMetals()) {
                    if (!agent.getMetalsBeingCollected().contains(foundCell)) {
                        agent.addTask(new DiggerTask(foundCell.getX(), foundCell.getY()
                                , TaskType.COLLECT_METAL.toString(), foundCell.getMetalType().getShortString()
                                , foundCell.getMetalAmount()));
                    }
                }
                agent.log(agent.getTasks().toString());
            }

            @Override
            public int onEnd() {
                if(agent.getTasks().isEmpty()) {
                    return 1;
                } else {
                    return 0;
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
