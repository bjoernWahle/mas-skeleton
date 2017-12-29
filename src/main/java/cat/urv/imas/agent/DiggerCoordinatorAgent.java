package cat.urv.imas.agent;

import cat.urv.imas.behaviour.digger_coordinator.RoundBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DiggerCoordinatorAgent extends ImasAgent {

    private int nResponders;

    public DiggerCoordinatorAgent() {
        super(AgentType.DIGGER_COORDINATOR);
    }

    List<DiggerTask> tasks;
    List<DiggerTask> currentTasks;
    
    private long roundEnd;

    List<MobileAgentAction> roundActions;

    private GameSettings gameSettings;

    private AID coordinatorAgent;

    @Override
    public void setup() {
        super.setup();

        // find coordinator agent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);

        tasks = new LinkedList<>();

        addTask(new DiggerTask(5, 0, TaskType.COLLECT_METAL.toString(), MetalType.SILVER.getShortString(), 4));


        addBehaviour(new RoundBehaviour(this));
    }

    public void informDiggers() {
        for (Cell cell: (gameSettings.getAgentList().get(AgentType.DIGGER))) {
            PathCell pathCell = (PathCell) cell;
            for(InfoAgent digger: ((PathCell) cell).getAgents().get(AgentType.DIGGER)) {
                ACLMessage msg = prepareMessage(ACLMessage.INFORM);
                msg.addReceiver(digger.getAID());
                try {
                    getContentManager().fillContent(msg, new RoundStart(pathCell.getX(), pathCell.getY(), this.gameSettings.getCurrentRoundEnd()));
                    send(msg);
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addTask(DiggerTask task) {
        tasks.add(task);
    }

    public List<DiggerTask> getTasks() {
        return tasks;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(GameSettings gameSettings) {
        log(" "+ gameSettings.getAgentList());
        this.gameSettings = gameSettings;
    }

    public List<AID> getDiggers() {
        return getGameSettings().getAgentList().get(AgentType.DIGGER).stream().flatMap(cell -> ((PathCell)cell).getAgents().get(AgentType.DIGGER).stream().map(InfoAgent::getAID)).collect(Collectors.toList());
    }

    public void resetRoundActions() {
        roundActions = new LinkedList<>();
    }

    public void addRoundAction(MobileAgentAction action) {
        roundActions.add(action);
    }

    public List<MobileAgentAction> getRoundActions() {
        return roundActions;
    }

    public void informCoordinator() {
        ACLMessage msg = prepareMessage(ACLMessage.INFORM);
        msg.addReceiver(coordinatorAgent);
        try {
            getContentManager().fillContent(msg, new ActionList(roundActions));
            log("Sending message with the list of actions to my boss: "+ msg.getContent());
            send(msg);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
    }

    public long getRoundEnd() {
        return roundEnd;
    }

    public void setRoundEnd(long roundEnd) {
        this.roundEnd = roundEnd;
    }

    public void initDiggers() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        for(AID digger : getDiggers()) {
            message.addReceiver(digger);
        }
        try {
            message.setContentObject(gameSettings);
            send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
