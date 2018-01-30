/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.behaviour.system.StepBehaviour;
import cat.urv.imas.gui.GraphicInterface;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;


/**
 * System agent that controls the GUI and loads initial configuration settings.
 * TODO: You have to decide the onthology and protocol when interacting among
 * the Coordinator agent.
 */
public class SystemAgent extends ImasAgent {

    /**
     * GUI with the map, system agent log and statistics.
     */
    private GraphicInterface gui;
    /**
     * Game settings. At the very beginning, it will contain the loaded
     * initial configuration settings.
     */
    private InitialGameSettings game;

    private List<MobileAgentAction> requestedActions;

    /**
     * The Coordinator agent with which interacts sharing game settings every
     * round.
     */
    private AID coordinatorAgent;
    private List<InfoAgent> diggerAgents;
    private List<InfoAgent> prospectorAgents;

    /**
     * Builds the System agent.
     */
    public SystemAgent() {
        super(AgentType.SYSTEM);
    }

    /**
     * A message is shown in the log area of the GUI, as well as in the
     * stantard output.
     *
     * @param log String to show
     */
    @Override
    public void log(String log) {
        if (gui != null) {
            gui.log(getLocalName()+ ": " + log + "\n");
        }
        super.log(log);
    }

    /**
     * An error message is shown in the log area of the GUI, as well as in the
     * error output.
     *
     * @param error Error to show
     */
    @Override
    public void errorLog(String error) {
        if (gui != null) {
            gui.log("ERROR: " + getLocalName()+ ": " + error + "\n");
        }
        super.errorLog(error);
    }

    /**
     * Gets the game settings.
     *
     * @return game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Adds (if probability matches) new elements onto the map
     * for every simulation step.
     * This method is expected to be run from the corresponding Behaviour
     * to add new elements onto the map at each simulation step.
     */
    public void addElementsForThisSimulationStep() {
        this.game.addElementsForThisSimulationStep();
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        // 1. Register with DF
        super.setup();

        // 2. Load game settings.
        this.game = InitialGameSettings.load("game2.settings");
        log("Initial configuration settings loaded");

        // 3. Start other agents
        startAgents();

        // 4. Load GUI
        try {
            this.gui = new GraphicInterface(game);
            gui.setVisible(true);
            log("GUI loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // search CoordinatorAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        log("The Coordinators AID is" + coordinatorAgent);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        // Setup finished. When the last inform is received, the agent itself will add
        // a behaviour to send/receive actions
        addBehaviour(new StepBehaviour(this));
        
        //This statement is just for testing the division of the map
        game.dividePathCellsInto(4);
    }

    public void startSimulation() {
        game.setCurrentSimulationStep(0);
        game.advanceToNextRound();
        requestedActions = new LinkedList<>();
    }

    @Override
    public long getRoundEnd() {
        return game.getCurrentRoundEnd();
    }

    /**
     * Starts the agents that are in defined in the game.
     * It will start all the digger agents and their coordinator in one container and all the prospector agents and
     * their coordinator in another container.
     * Also it adds the AIDs of the agents to the game.
     */
    private void startAgents() {
        if(game == null) {
            throw new IllegalStateException("Game has not been initialized yet.");
        }
        int diggerAgentsIndex = 0;
        int prospectorAgentsIndex = 0;
        diggerAgents = new LinkedList<>();
        prospectorAgents = new LinkedList<>();
        jade.wrapper.AgentContainer diggerContainer = null;
        jade.wrapper.AgentContainer prospectorContainer = null;
        // start digger agents
        for(Cell cell: this.game.getAgentList().get(AgentType.DIGGER)) {
            PathCell pathCell = (PathCell) cell;
            for (InfoAgent agent : pathCell.getAgents().get(AgentType.DIGGER)) {
                String name = "Digger_"+diggerAgentsIndex++;
                String[] args = new String[3];
                args[0] = Integer.toString(cell.getX());
                args[1] = Integer.toString(cell.getY());
                args[2] = Integer.toString(((DiggerInfoAgent) agent).getMaxCapacity());
                if(diggerContainer == null) {
                    diggerContainer = UtilsAgents.createAgentGetContainer(name, agent.getType().getClassName(), args);
                } else {
                    UtilsAgents.createAgent(diggerContainer, name, agent.getType().getClassName(), args);
                }
                agent.setAID(new AID(name, AID.ISLOCALNAME));
                diggerAgents.add(agent);
            }
        }
        // start prospector agents
        for(Cell cell: this.game.getAgentList().get(AgentType.PROSPECTOR)) {
            PathCell pathCell = (PathCell) cell;
            for (InfoAgent agent : pathCell.getAgents().get(AgentType.PROSPECTOR)) {
                String name = "Prospector_"+prospectorAgentsIndex++;
                String[] args = new String[2];
                args[0] = Integer.toString(cell.getX());
                args[1] = Integer.toString(cell.getY());
                if(prospectorContainer == null) {
                    prospectorContainer = UtilsAgents.createAgentGetContainer(name, agent.getType().getClassName(), args);
                } else {
                    UtilsAgents.createAgent(prospectorContainer, name, agent.getType().getClassName(), args);
                }
                agent.setAID(new AID(name, AID.ISLOCALNAME));
                prospectorAgents.add(agent);
            }
        }

        AID diggerCoordinator = new AID("DiggerCoordinator", AID.ISLOCALNAME);
        // start digger coordinator agent if at least one digger was in the map
        if(diggerContainer != null) {
            UtilsAgents.createAgent(diggerContainer, diggerCoordinator.getLocalName(), AgentType.DIGGER_COORDINATOR.getClassName(), null);
        }
        // start prospector coordinator agent if at least one prospector was in the map
        if(prospectorContainer != null) {
            UtilsAgents.createAgent(prospectorContainer, "ProspectorCoordinator", AgentType.PROSPECTOR_COORDINATOR.getClassName(), null);
        }
        // start coordinator agent
        UtilsAgents.createAgent(getContainerController(), "Coordinator", AgentType.COORDINATOR.getClassName(), null);

    }

    public void updateGUI() {
        this.gui.updateGame();
    }

    public void advanceToNextRound() {
        // TODO get planned actions for this round and apply them (started...)
        // TODO add stats for last round
        checkAndApplyActions();
        addElementsForThisSimulationStep();
        updateStats();
        game.checkFoundMetals();
        updateGUI();
        this.game.advanceToNextRound();
        log("Starting round "+this.game.getCurrentSimulationStep());
    }

    private void updateStats() {
        DecimalFormat f = new DecimalFormat("##.00");
        StringBuilder sb = new StringBuilder();
        sb.append("Round ");
        sb.append(this.game.getCurrentSimulationStep());
        sb.append(": Total benefits: ");
        sb.append(game.getCollectedPoints());
        int totalManufacturedMetal = 0;
        for(MetalType metalType: MetalType.values()) {
            sb.append(", Total ");
            sb.append(metalType.toString());
            sb.append(" manufactured: ");
            int amount = game.getManufacturedMetal(metalType);
            totalManufacturedMetal = totalManufacturedMetal +amount;
            sb.append(amount);
        }
        sb.append(", Total manufactured metal: ");
        sb.append(totalManufacturedMetal);
        sb.append("\n\t");
        sb.append(", Average benefit per unit: ");
        if(totalManufacturedMetal == 0) {
            sb.append("NaN");
        } else {
            sb.append(game.getCollectedPoints() / ((double) totalManufacturedMetal));
        }
        sb.append(", Average time for discovery: ");
        double avgDiscoveryTime = game.getAverageDiscoveryTime();
        if(avgDiscoveryTime == -1.0) {
            sb.append("NaN");
        } else {
            sb.append(f.format(avgDiscoveryTime));
        }
        sb.append(", Average time for collection: ");
        double avgCollectionTime = game.getAverageCollectionTime();
        if(avgCollectionTime == -1.0) {
            sb.append("NaN");
        } else {
            sb.append(f.format(avgCollectionTime));
        }
        sb.append(", Total metal on map: ");
        sb.append(game.getTotalMetal());
        sb.append(", Ratio of discovered metal: ");
        sb.append(f.format(game.getRatioOfDiscoveredMetal()));
        sb.append(", Ratio of collected metal:");
        sb.append(f.format(totalManufacturedMetal / ((double) game.getTotalMetal())));
        sb.append("\n");
        gui.showStatistics(sb.toString());
    }

    private void checkAndApplyActions() {
        // TODO maybe deep clone gameSettings before
        // check actions
        for(MobileAgentAction action : requestedActions) {
            if(action instanceof CollectMetalAction) {
                CollectMetalAction collectAction = (CollectMetalAction) action;
                try {
                    this.game.applyCollectMetal(collectAction);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    log(e.getMessage());
                    // TODO notify agents about their illegal moves
                }
            }
            if(action instanceof MoveAction) {
                MoveAction moveAction = (MoveAction) action;
                try {
                    this.game.applyMove(moveAction);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    log(e.getMessage());
                    // TODO notify agents about their illegal moves
                }
            }
            if(action instanceof ReturnMetalAction) {
                ReturnMetalAction returnAction = (ReturnMetalAction) action;
                try {
                    this.game.applyReturnMetal(returnAction);
                } catch (IllegalArgumentException e) {
                    log(e.getMessage());
                }
            }
            if(action instanceof DetectAction) {
            	DetectAction detectAction = (DetectAction) action;
                try {
                    this.game.applyDetection(detectAction);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    log(e.getMessage());
                    // TODO notify agents about their illegal moves
                }
            }

        }
        //Clear requested actions because they have already been checked
        requestedActions.clear();

        // TODO check return actions (needed)
        // TODO blame agents that wanna be idle
    }

    public void storeActions(ActionList agentActions) {
        log("Storing actions");
        // remove last rounds actions (just in case - they should be deleted after being applied)
        requestedActions = new LinkedList<>();
        // store actions
        requestedActions.addAll(agentActions.getAgentActions());
    }

    public void notifyCoordinator() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setSender(getAID());
        msg.addReceiver(coordinatorAgent);
        try {
            msg.setContentObject(this.game);
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
            log("Error while serializing InitialGameSettings object");
        }
    }
}
