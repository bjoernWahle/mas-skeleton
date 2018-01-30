package cat.urv.imas.agent;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cat.urv.imas.behaviour.prospector_coordinator.RoundBehaviour;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.ActionList;
import cat.urv.imas.onthology.DiggerTask;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.MetalType;
import cat.urv.imas.onthology.MobileAgentAction;
import cat.urv.imas.onthology.RoundStart;
import cat.urv.imas.onthology.TaskType;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ProspectorCoordinatorAgent extends ImasAgent {

	private long roundEnd;

    List<MobileAgentAction> roundActions;

    private GameSettings gameSettings;
    
    private boolean prospectorsInicialized = false;

    private AID coordinatorAgent;
    
    private Map<Integer,List<Cell>> areaDivision;
    
    public Map<AID,Integer> areaAssignament;

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
    
    
    public Map<Integer, List<Cell>> getAreaDivision() {
		return areaDivision;
	}

	public void setAreaDivision(Map<Integer, List<Cell>> areaDivision) {
		this.areaDivision = areaDivision;
	}
	
	public void calculateAreaDivision(int numProspectors) {
		this.areaDivision = gameSettings.dividePathCellsInto(numProspectors);
	}

	public Map<AID, Integer> getAreaAssignament() {
		return areaAssignament;
	}

	public void setAreaAssignament(Map<AID, Integer> areaAssignament) {
		this.areaAssignament = areaAssignament;
	}

	public List<AID> getProspectors() {
        return getGameSettings().getAgentList().get(AgentType.PROSPECTOR).stream().flatMap(cell -> ((PathCell)cell).getAgents().get(AgentType.PROSPECTOR).stream().map(InfoAgent::getAID)).collect(Collectors.toList());
    }
    
    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(GameSettings gameSettings) {
        //log(" "+ gameSettings.getAgentList());
        this.gameSettings = gameSettings;
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
    
    public void initProspectors() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        for(AID digger : getProspectors()) {
            message.addReceiver(digger);
        }
        try {
            message.setContentObject(gameSettings);
            send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
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

	public boolean isInitialized() {
		return prospectorsInicialized;
	}
	public void setInitialized(boolean inicialized) {
		this.prospectorsInicialized = inicialized;
	}
}
