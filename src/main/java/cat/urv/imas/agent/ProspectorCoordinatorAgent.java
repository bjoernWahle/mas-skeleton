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
    	/*
        for (Cell cell: (gameSettings.getAgentList().get(AgentType.PROSPECTOR))) {
            PathCell pathCell = (PathCell) cell;
            for(InfoAgent prospector: ((PathCell) cell).getAgents().get(AgentType.PROSPECTOR)) {
                ACLMessage msg = prepareMessage(ACLMessage.INFORM);
                msg.addReceiver(prospector.getAID());
                try {
                    getContentManager().fillContent(msg, new RoundStart(pathCell.getX(), pathCell.getY(), this.gameSettings.getCurrentRoundEnd()));
                    send(msg);
                } catch (Codec.CodecException e) {
                    e.printStackTrace();
                } catch (OntologyException e) {
                    e.printStackTrace();
                }
            }
        }*/
        
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
