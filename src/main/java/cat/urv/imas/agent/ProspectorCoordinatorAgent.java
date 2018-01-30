package cat.urv.imas.agent;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cat.urv.imas.behaviour.prospector_coordinator.RoundBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.*;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ProspectorCoordinatorAgent extends ImasAgent {

	private long roundEnd;
    private GameSettings gameSettings;
    private boolean prospectorsInicialized = false;
    private AID coordinatorAgent;
    private Map<Integer,List<Cell>> areaDivision;
    public Map<AID,Integer> areaAssignament;
    private List<MobileAgentAction> roundActions;
    
    
    public ProspectorCoordinatorAgent() {
        super(AgentType.PROSPECTOR_COORDINATOR);
    }

    @Override
    public void setup() {
        super.setup();

     // find coordinator agent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.COORDINATOR.toString());
        this.coordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);

        addBehaviour(new RoundBehaviour(this));
        
        areaAssignament = new HashMap<AID,Integer>();
    }
    
    
    /**
     * Getter for areaDivision variable. This is the division of the map in different subareas. Each key specifies a different area where the value is a list
     * of the cells forming the area.
     * @return
     */
    public Map<Integer, List<Cell>> getAreaDivision() {
		return areaDivision;
	}

    /**
     * Setter for the area Division. This is the division of the map in different subareas. Each key specifies a different area where the value is a list
     * of the cells forming the area.
     * @param areaDivision
     */
	public void setAreaDivision(Map<Integer, List<Cell>> areaDivision) {
		this.areaDivision = areaDivision;
	}
	
	/**
	 * This forces the calculation of the map division given the number of prospectors. The map will be divided into numProspectors parts.
	 * @param numProspectors
	 */
	public void calculateAreaDivision(int numProspectors) {
		this.areaDivision = gameSettings.dividePathCellsInto(numProspectors);
	}

	/**
	 * This is the getter for the areaAssignament. This variable specifies which prospector (AID) explore
	 * each area (defined by an integer equal to the key in areaDivision)
	 * @return
	 */
	public Map<AID, Integer> getAreaAssignament() {
		return areaAssignament;
	}

	/**
	 * Setter for the areaAssignament.
	 * @param areaAssignament
	 */
	public void setAreaAssignament(Map<AID, Integer> areaAssignament) {
		this.areaAssignament = areaAssignament;
	}

	/**
	 * This method returns the list of prospectors in the system.
	 * @return
	 */
	public List<AID> getProspectors() {
        return getGameSettings().getAgentList().get(AgentType.PROSPECTOR).stream().flatMap(cell -> ((PathCell)cell).getAgents().get(AgentType.PROSPECTOR).stream().map(InfoAgent::getAID)).collect(Collectors.toList());
    }
    
	/**
	 * Getter for game settings
	 * @return
	 */
    public GameSettings getGameSettings() {
        return gameSettings;
    }

    /**
     * Setter for game settings
     * @param gameSettings
     */
    public void setGameSettings(GameSettings gameSettings) {
        //log(" "+ gameSettings.getAgentList());
        this.gameSettings = gameSettings;
    }
    
    /**
     * This method resets the actions to perfrom.
     */
    public void resetRoundActions() {
        roundActions = new LinkedList<>();
    }
    
    /**
     * This method adds an action to be sent to the coordinator.
     * @param action
     */
    public void addRoundAction(MobileAgentAction action) {
        roundActions.add(action);
    }

    /**
     * This is the getter of roundActions
     * @return
     */
    public List<MobileAgentAction> getRoundActions() {
        return roundActions;
    }
    
    /**
     * This method sends a message to all prospector with the current gameSettings.
     */
    public void informProspectors() {
    	
    	ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        for(AID prospector : getProspectors()) {
            message.addReceiver(prospector);
        }
        try {
            message.setContentObject(gameSettings);
            send(message);
        } catch (IOException e) {
            e.printStackTrace();
            log("Something went wrong sending the message.");
        }
    }
    
    /**
     * This method sends the new actions to perform to the coordinator.
     */
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
    
    /**
     * This is the getter for the variable initialized. When this is false, the process of dividing the map into different parts and assigning one
     * to each agent will be performed.
     * @return
     */
	public boolean isInitialized() {
		return prospectorsInicialized;
	}
	
	/**
	 * Setter for the variable prospectorsInicialized. 
	 * @param inicialized
	 */
	public void setInitialized(boolean inicialized) {
		this.prospectorsInicialized = inicialized;
	}

    public void broadCastGameHasEnded() {
        ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        for(AID digger : getProspectors()) {
            message.addReceiver(digger);
        }
        try {
            getContentManager().fillContent(message, new GameHasEnded());
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
        }
        send(message);
    }
}
