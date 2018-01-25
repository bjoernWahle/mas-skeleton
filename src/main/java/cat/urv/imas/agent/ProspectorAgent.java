package cat.urv.imas.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import cat.urv.imas.behaviour.digger.DiggerBehaviour;
import cat.urv.imas.behaviour.prospector.ProspectorBehaviour;
import cat.urv.imas.onthology.DiggerInfoAgent;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InformAgentAction;
import cat.urv.imas.onthology.InformProspector;
import cat.urv.imas.onthology.MobileAgentAction;
import cat.urv.imas.onthology.MoveAction;
import cat.urv.imas.onthology.RoundStart;
import cat.urv.imas.util.Graph;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.PathCell;

public class ProspectorAgent extends ImasAgent implements MovingAgentInterface {

    private int currentX;
    private int currentY;
    private AID prospectorCoordinator;
    private GameSettings game;
    private long roundEnd;
    private List<FieldCell> detectedMetals = new ArrayList<FieldCell>(); 
    private MobileAgentAction currentAction;
    private Map<Cell,Integer> subMapToExplore = new HashMap<Cell,Integer>();
    
    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }

    @Override
    public void setup() {
        super.setup();

        //Find prospector coordinator
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        
        
        // TODO implement and add behaviours
        // set starting position
        String[] args = (String[]) getArguments();
        currentX = Integer.parseInt(args[0]);
        currentY = Integer.parseInt(args[1]);        
        
        log("I am at ("+ currentX +","+ currentY +")!");
        

        // add behaviours
        addBehaviour(new ProspectorBehaviour(this));
    }

    @Override
    public int stepsToPosition(int row, int col) {
        // easy approach: euclidean distance
        int yDistance = Math.abs (row - currentY);
        int xDistance = Math.abs (col - currentX);
        double distance = Math.sqrt((yDistance)*(yDistance) +(xDistance)*(xDistance));
        return (int) Math.ceil(distance);
    }
    
    public void startRound(RoundStart rs) {
        setCurrentPosition(rs.getX(),rs.getY());
        roundEnd = rs.getRoundEnd();
    }
    
    public GameSettings getGame() {
        return game;
    }

    public void setGame(GameSettings game) {
        this.game = game;
    }
    
    public void setCurrentPosition(int x, int y) {
    	if ((currentX != x) || (currentY != y)) {
            //Prospector moved to a new position
    		currentX = x;
            currentY = y; 
            subMapToExplore.put(game.get(currentY,currentX),subMapToExplore.get(game.get(currentY,currentX))+1);
    	}
    }
    
    public void setCellsToExplore() {
    	/* Create Map with path cell. This is temporarily, it will use the whole map as a map to explore. 
         * In the future it will only have to take some part of the map
         */
        Map<CellType, List<Cell>> temp = game.getCellsOfType();
        for (Cell el:temp.get(CellType.PATH)){
        	subMapToExplore.put(el, 0);
        }
        subMapToExplore.put(game.get(currentY,currentX),subMapToExplore.get(game.get(currentY,currentX))+1);
    }
    
    public void moveNextCell() {
    	//Implementation of not efficient movement to explore the map.
    	List<PathCell> possibleMovements = game.getPathNeighbors(game.get(currentY,currentX));
    	PathCell nextCell = getNextCellToExplore(possibleMovements);
    	if(nextCell == null) {
    		nextCell = (PathCell) game.get(currentY, currentX);
    	}
    	currentAction = new MoveAction(nextCell.getX(), nextCell.getY());
    	log("I want to move to ("+ nextCell.getY() + "," + nextCell.getX() +")!");
    	//TODO: Implementation of an intelligent movement to efficiently explore the map.
    	
    }
    
    public void examine() {
    	detectedMetals = game.detectFieldsWithMetal(currentY, currentX);
    	if(!detectedMetals.isEmpty()) {
    		log("Hey fella, I found "+ detectedMetals.size() + " new metals!");
    	}
    }
        
    public void informCoordinator() {
    	//Send new position of the prospector and metals found (if any)
    	ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(prospectorCoordinator);
        try {
        	getContentManager().fillContent(message, new InformProspector(currentAction,detectedMetals));
            log("Sending msg with my current action and metals found: " + message.getContent());
            send(message);
            detectedMetals.clear();
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Some error while sending?");
        }     
    }
    
    /**
     * Get next cell to explore
     */
    private PathCell getNextCellToExplore(List<PathCell> possibleMovements) {
    	
    	PathCell nextCell = null;
    	int minimum = 0;
    	for(PathCell cell : possibleMovements) {
    		if(nextCell == null) {
    			nextCell = cell;
    			minimum = subMapToExplore.get(cell);
    		}else {
    			if (subMapToExplore.get(cell) < minimum) {
    				nextCell = cell;
    				minimum = subMapToExplore.get(cell);
    			}
    		}
    	}
    	return nextCell;
    }
}