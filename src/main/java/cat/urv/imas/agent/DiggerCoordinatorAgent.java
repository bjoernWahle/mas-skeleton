package cat.urv.imas.agent;

import cat.urv.imas.behaviour.digger_coordinator.RoundBehaviour;
import cat.urv.imas.map.*;
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

    public Map<ManufacturingCenterCell, Map<Cell, Integer>> mfcDistances;

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

        addBehaviour(new RoundBehaviour(this));
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

    public void informDiggers() {
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
            log("Something went wrong sending the message.");
        }
    }

    public void broadcastSimpleMessageToDiggers(int performative, String content) {
        ACLMessage message = new ACLMessage(performative);
        message.setSender(getAID());
        for(AID digger : getDiggers()) {
            message.addReceiver(digger);
        }
        message.setContent(content);
        send(message);
    }

    public List<FieldCell> getMetalsBeingCollected() {
        return tasks.stream().map(t -> (FieldCell) gameSettings.get(t.y, t.x)).collect(Collectors.toList());
    }

    public List<DiggerTask> getNotStartedTasks() {
        return getTasks().stream().filter(t -> t.getCurrentState().equals(TaskState.NOT_STARTED.toString())).collect(Collectors.toList());
    }

    public void handleFinishedTask(DiggerTask finishedTask) {
        // digger informed about that a task was finished;
        if(finishedTask.getTaskType().equals(TaskType.COLLECT_METAL.toString())) {
            finishedTask.setCurrentState(TaskState.IN_PROGRESS.toString());
            Optional<DiggerTask> task = tasks.stream().filter(t -> t.equals(finishedTask)).findFirst();
            if(task.isPresent()) {
                removeTask(task.get());
            } else {
                log("Task was not found in list: "+finishedTask);
            }
        }
    }

    public void removeTask(DiggerTask task) {
        tasks.remove(task);
    }

    public void broadCastGameHasEnded() {
        ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        for(AID digger : getDiggers()) {
            message.addReceiver(digger);
        }
        try {
            getContentManager().fillContent(message, new GameHasEnded());
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
        send(message);
    }

    public void initManufacturingCenterDistances() {
        log("Initializing distances to manufacturing centers. This takes some seconds, but is only done once.");
        this.mfcDistances = new HashMap<>();
        for(Cell cell : gameSettings.getCellsOfType().get(CellType.MANUFACTURING_CENTER)) {
            Map<Cell, Integer> mfcDistances = new HashMap<>();
            ManufacturingCenterCell mfc = (ManufacturingCenterCell) cell;
            List<PathCell> mNeighbors = gameSettings.getPathNeighbors(mfc, true);
            for(Cell fieldCell : gameSettings.getCellsOfType().get(CellType.FIELD)) {
                Integer shortestDistance = Integer.MAX_VALUE;
                for(PathCell pc : mNeighbors) {
                    Map<Cell, Integer> distances = gameSettings.getMapGraph().getDistances(pc);
                    List<PathCell> fNeighbors = gameSettings.getPathNeighbors(fieldCell, true);
                    int tempDistance = distances.get(fNeighbors.stream().min(Comparator.comparingInt(distances::get)).get());
                    if(tempDistance < shortestDistance) {
                        shortestDistance = tempDistance;
                    }
                }
                mfcDistances.put(fieldCell, shortestDistance);
            }
            this.mfcDistances.put(mfc, mfcDistances);
        }
        log("Initializing done.");
    }

    public void sortDiggerTasks() {
        tasks.sort(Comparator.comparingDouble(this::getBestMFCRatio));
        Collections.reverse(tasks);
    }

    private double getBestMFCRatio(DiggerTask task) {
        Cell cell = gameSettings.get(task.y, task.x);
        MetalType metalType = task.getMetal();
        double bestRatio = 0.0;
        for(ManufacturingCenterCell mfc : mfcDistances.keySet()) {
            if(mfc.getMetal() == metalType) {
                // calculating points per round
                double ratio = ((double) mfc.getPrice()*task.getAmount()) / ((double) mfcDistances.get(mfc).get(cell)+task.getAmount());
                if(ratio > bestRatio) {
                    bestRatio = ratio;
                }
            }
        }
        return bestRatio;
    }
}
