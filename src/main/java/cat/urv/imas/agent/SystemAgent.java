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
import cat.urv.imas.onthology.AgentList;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InitialGameSettings;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

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


    /**
     * Round number.
     */
    private int currentRound;
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
        this.game = InitialGameSettings.load("game.settings");
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
        startSimulation();
        addBehaviour(new StepBehaviour(this));
    }

    private void startSimulation() {
        currentRound = 0;
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
                String[] args = new String[2];
                args[0] = Integer.toString(cell.getCol());
                args[1] = Integer.toString(cell.getRow());
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
                args[0] = Integer.toString(cell.getCol());
                args[1] = Integer.toString(cell.getRow());
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

    public int getCurrentRound() {
        return currentRound;
    }

    public void advanceToNextRound() {
        // TODO get planned actions for this round and apply them
        addElementsForThisSimulationStep();
        updateGUI();
        currentRound++;
        log("Starting round "+currentRound);
    }
}
