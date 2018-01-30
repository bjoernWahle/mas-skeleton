package cat.urv.imas.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import cat.urv.imas.behaviour.prospector.ProspectorBehaviour;
import cat.urv.imas.onthology.*;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;

public class ProspectorAgent extends ImasAgent implements MovingAgentInterface {

	private static final long serialVersionUID = 1L;
	private int currentX;
    private int currentY;
    private AID prospectorCoordinator;
    private GameSettings game;
    private long roundEnd;
    private MobileAgentAction currentAction;
    private Map<Cell,Integer> subMapToExplore = new HashMap<Cell,Integer>();
    private Plan currentMovementPlan;
    private List<Cell> currentExplorationArea;
    private int ongoingDirection_x = 0; 
    private int ongoingDirection_y = 0; 
    
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
    
    /**
     * This function is never used
     */
    @Override
    public int stepsToPosition(int row, int col) {
        // easy approach: euclidean distance
        int yDistance = Math.abs (row - currentY);
        int xDistance = Math.abs (col - currentX);
        double distance = Math.sqrt((yDistance)*(yDistance) +(xDistance)*(xDistance));
        return (int) Math.ceil(distance);
    }
    
    /**
     * this method updates the current position with the new position of the prospector
     * @param x
     * @param y
     */
    public void startRound(int x, int y) {
        setCurrentPosition(x, y);
        roundEnd = game.getCurrentRoundEnd();
    }
    
    /**
     * getter for the game settings
     * @return
     */
    public GameSettings getGame() {
        return game;
    }
    
    /**
     * Setter for the game settings
     * @param game
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }
    
    /**
     * This method checks if the position has changed. If so, it also updated the map to keep track of the explored cells.
     * @param x
     * @param y
     */
    public void setCurrentPosition(int x, int y) {
    	if ((currentX != x) || (currentY != y)) {
            //Prospector moved to a new position
    		currentX = x;
            currentY = y;
            updateExploredMap();
    	}
    }
    
    /**
     * This sets the subarea to explore. This will use a Map linking each cell with the value of the last time the cell was explored (so wew can keep track
     * of it). The area to explore is the subarea of the map the prospector will go and explore.
     * @param area
     */
    public void setCellsToExplore(List<Cell> area) {
    	if(currentExplorationArea == null || !currentExplorationArea.equals(area)) {
	        for (Cell el: area){
	        	subMapToExplore.put(el, 0);
	        }
	        currentExplorationArea = area;
    	}
    	
    	updateExploredMap();
    }
    
    /**
     * This private method updates the Map of cells to explore and the number of times a cell was explored. Every time is called, it assigns for the current cell (key)
     * a new value (corresponding to times it has been explored).
     */
    private void updateExploredMap() {
    	if(subMapToExplore.containsKey(game.get(currentY,currentX))) {
        	subMapToExplore.put(game.get(currentY,currentX),subMapToExplore.get(game.get(currentY,currentX))+1);
        	//subMapToExplore.put(game.get(currentY,currentX),game.getCurrentSimulationStep());
        }
    }
    
    /**
     * This method search for the best cell to move to. 
     * In case the prospector is not in the assigned subarea, it will follow the steps to get there. 
     * If the prospector is already into the assigned subarea, it will try to explore the area as efficiently as possible.
     */
    public void moveNextCell() {

    	//Implementation of not efficient movement to explore the map.
    	List<PathCell> possibleMovements = game.getPathNeighbors(game.get(currentY,currentX),false);
    	PathCell nextCell = getNextCellToExplore(possibleMovements);

    	if(nextCell == null) {
    		nextCell = (PathCell) game.get(currentY, currentX);
    	}
    	currentAction = new MoveAction(nextCell.getX(), nextCell.getY());
    	log("I want to move to ("+ nextCell.getY() + "," + nextCell.getX() +")!");
    	
    	//Send new movement to SystemAgent
    	informCoordinator(currentAction);

    }
    
    /**
     * This will send a DetectAction to explore the current cell
     */
    public void examine() {
    	currentAction = new DetectAction(currentX,currentY);

    	//Send new movement to SystemAgent
    	informCoordinator(currentAction);
    }
    
    /**
     * This method is used to send the different kind of actions to the prospector coordinator.
     * @param currentAction
     */
    public void informCoordinator(MobileAgentAction currentAction) {
    	//Send new position of the prospector and metals found (if any)
    	ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(prospectorCoordinator);
        try {
        	getContentManager().fillContent(message, new InformAgentRound(currentAction));
            log("Sending msg with my next action " + message.getContent());
            send(message);
            this.currentAction = null;
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Some error while sending?");
        }     
    }

    /**
     * Get next cell to explore
     */
    private PathCell getNextCellToExplore(List<PathCell> possibleMovements) {
    	Cell nextCell = null;
    	List<Cell> keysSubMap = new ArrayList<Cell>(subMapToExplore.keySet());
    	keysSubMap.retainAll(possibleMovements);
    	if(!keysSubMap.isEmpty()) {
        	int minimum = 0;
        	for(Cell cell : keysSubMap) {
        		if(nextCell == null) {
        			nextCell = cell;
        			minimum = subMapToExplore.get(cell);
        		}else {
        			if (subMapToExplore.get(cell) < minimum) {
        				nextCell = cell;
        				minimum = subMapToExplore.get(cell);
        			}else if(subMapToExplore.get(cell) == minimum) {
        				//If there is no improvement we will prioritize going the same direction we were going
        				if(((cell.getX()-currentX) == ongoingDirection_x) && ((cell.getY()-currentY) == ongoingDirection_y)) {
        					nextCell = cell;
            				minimum = subMapToExplore.get(cell);
        				}
        			}
        		}
        	}
        	ongoingDirection_x = nextCell.getX() - currentX;
        	ongoingDirection_y = nextCell.getY() - currentY;
        	
    	}else {
    		if(currentMovementPlan == null) {
    			currentMovementPlan = getNewPlan();
    		}
    		
    		// check if I have moved
            PathCell pc = currentMovementPlan.getFirst();
            if(pc.getX() == currentX && pc.getY() == currentY) {
                currentMovementPlan.dropFirst();
            }
    		if(!possibleMovements.contains(currentMovementPlan.getFirst())){
    			currentMovementPlan = getNewPlan();
    			nextCell = currentMovementPlan.getFirst();
    		}else {
    			nextCell = currentMovementPlan.getFirst();
    		}
    	}
    	return (PathCell) nextCell;
    }
    
    /**
     * This creates a plan to get to the assigned subarea as fast as possible
     * @return
     */
    public Plan getNewPlan() {
    	List<Cell> vList = game.getMapGraph().getShortestPath(game.get(currentY, currentX), new ArrayList<Cell>(subMapToExplore.keySet()));
        List<PathCell> pc = vList.stream().map(c -> (PathCell) c).collect(Collectors.toList());
		return new Plan(pc);    	
    }
    
    /**
     * this method sorts the possible ares to explore by order of preference (in this case this order is the number of steps to get there).
     * After the preference has been obtained, it sends it to the coordinator.
     */
	public void chooseAreas() {
		//Order the areas by preference. We will try to get the closest
		Map<Integer,List<Cell>> assignement = game.getCellAssignement();
		Map<Long,Long> distances = new HashMap<Long,Long>();
		List<Long> preferenceOrder = new ArrayList<Long>();
		for(Integer key: assignement.keySet()) {
			distances.put(key.longValue(), (long) game.getMapGraph().getShortestDistance(game.get(currentX, currentY), assignement.get(key)));
		}
		
		distances = distances.entrySet().stream().sorted(Map.Entry.comparingByValue())
    	.collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    	
		for(Long key: distances.keySet()) {
			preferenceOrder.add(key);
		}
		
		//Send order to prospector
    	ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(prospectorCoordinator);
        try {
        	getContentManager().fillContent(message, new InformProspectorInitialization(preferenceOrder));
            log("Sending msg with my preference to the coordinator");
            send(message);
            
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Some error while sending?");
        }     
		
		
	}
}