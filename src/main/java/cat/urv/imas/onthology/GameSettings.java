/**
 * IMAS base code for the practical work.
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import cat.urv.imas.map.*;
import cat.urv.imas.util.Graph;
import cat.urv.imas.util.StatisticsTracker;
import cat.urv.imas.util.Vertex;
import jade.core.AID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Current game settings. Cell coordinates are zero based: row and column values
 * goes from [0..n-1], both included.
 *
 * Use the GenerateGameSettings to build the game.settings configuration file.
 *
 */
@XmlRootElement(name = "GameSettings")
public class GameSettings implements java.io.Serializable {

    protected StatisticsTracker statisticsTracker = new StatisticsTracker();

    /* Default values set to all attributes, just in case. */
    /**
     * Seed for random numbers.
     */
    private long seed = 0;
    /**
     * Metal price for each manufacturing center. They appear in the same order
     * than they appear in the map.
     */
    protected int[] manufacturingCenterPrice = {8, 9, 6, 7};
    /**
     * Metal type for each manufacturing center. They appear in the same order
     * than they appear in the map.
     */
    protected MetalType[] manufacturingCenterMetalType = {
        MetalType.GOLD,
        MetalType.SILVER,
        MetalType.SILVER,
        MetalType.GOLD
    };

    /**
     * Total number of simulation steps.
     */
    private int simulationSteps = 100;

    /**
     * Current simulation step.
     */
    int currentSimulationStep = 0;
    /**
     * current round end
     */
    long currentRoundEnd = 0;

    /**
     * how long a round is
     */
    int stepTime = 2000000;

    /**
     * City map.
     */
    protected Cell[][] map;

    /**
     * Graph representation of the map.
     */
    protected Graph<Cell> mapGraph;
    /**
     * From 0 to 100 (meaning percentage) of probability of having new
     * metal in the city at every step.
     */
    protected int newMetalProbability = 10;
    /**
     * If there is new metal in a certain simulation step, this number
     * represents the maximum number of fields with new metal.
     */
    protected int maxNumberFieldsWithNewMetal = 5;
    /**
     * For each field with new metal, this number represents the maximum
     * amount of new metal that can appear.
     */
    protected int maxAmountOfNewMetal = 5;
    /**
     * All harvesters will have this capacity of garbage units.
     */
    protected int diggersCapacity = 6;
    /**
     * Computed summary of the position of agents in the city. For each given
     * type of mobile agent, we get the list of their positions.
     */
    protected Map<AgentType, List<Cell>> agentList;
    /**
     * Title to set to the GUI.
     */
    protected String title = "Default game settings";
    /**
     * List of cells per type of cell.
     */
    protected Map<CellType, List<Cell>> cellsOfType;
    
    /**
     * List of metals already discovered by the prospectors
     */
    List<FieldCell> foundMetals;
    
    /**
     * This map classifies the different areas of the map
     */
    private Map<Integer,List<Cell>> cellAssignement;
    
    /**
     * This map links an area with a prospectors (is set by the prospector coordinator each round)
     */
    private Map<AID,Integer> areaAssignament;
    
    
    public void setAreaAssignament(Map<AID,Integer> areaAssignament){
    	this.areaAssignament=areaAssignament;
    }

    public List<FieldCell> getFoundMetals() {
        return foundMetals;
    }

    public long getSeed() {
        return seed;
    }

    @XmlElement(required = true)
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int[] getManufacturingCenterPrice() {
        return manufacturingCenterPrice;
    }

    @XmlElement(required = true)
    public void setManufacturingCenterPrice(int[] prices) {
        this.manufacturingCenterPrice = prices;
    }

    public MetalType[] getManufacturingCenterMetalType() {
        return manufacturingCenterMetalType;
    }

    @XmlElement(required = true)
    public void setManufacturingCenterMetalType(MetalType[] types) {
        this.manufacturingCenterMetalType = types;
    }

    public int getSimulationSteps() {
        return simulationSteps;
    }

    @XmlElement(required = true)
    public void setSimulationSteps(int simulationSteps) {
        this.simulationSteps = simulationSteps;
    }

    public String getTitle() {
        return title;
    }

    @XmlElement(required=true)
    public void setTitle(String title) {
        this.title = title;
    }

    public int getNewMetalProbability() {
        return newMetalProbability;
    }

    @XmlElement(required=true)
    public void setNewMetalProbability(int newMetalProbability) {
        this.newMetalProbability = newMetalProbability;
    }

    public int getMaxNumberFieldsWithNewMetal() {
        return maxNumberFieldsWithNewMetal;
    }

    @XmlElement(required=true)
    public void setMaxNumberFieldsWithNewMetal(int maxNumberFieldsWithNewMetal) {
        this.maxNumberFieldsWithNewMetal = maxNumberFieldsWithNewMetal;
    }

    public int getMaxAmountOfNewMetal() {
        return maxAmountOfNewMetal;
    }

    @XmlElement(required=true)
    public void setMaxAmountOfNewMetal(int maxAmountOfNewMetal) {
        this.maxAmountOfNewMetal = maxAmountOfNewMetal;
    }

    public int getDiggersCapacity() {
        return diggersCapacity;
    }

    @XmlElement(required=true)
    public void setDiggersCapacity(int diggersCapacity) {
        this.diggersCapacity = diggersCapacity;
    }

    /**
     * Gets the full current city map.
     * @return the current city map.
     */
    @XmlTransient
    public Cell[][] getMap() {
        return map;
    }

    public void detectFieldsWithMetal(int row, int col) {
        //Find all surrounding cells to (row,col) that are
        //buildings and have garbage on it.
        //Use: FieldCell.detectMetal() to do so.
    	
    	for (int i=-1; i<2; i++) {
    		for (int j=-1;j<2;j++) {
	    		if((row+i) < map.length && (col+j) < map[0].length && (row+i)>=0 && (col+j) >= 0) {
	    			if (map[row+i][col+j].getCellType() == CellType.FIELD) {
	    				FieldCell tempCell = (FieldCell) map[row+i][col+j];
	    				if(!tempCell.detectMetal().isEmpty()) {
	    					foundMetals.add(tempCell);
	    					statisticsTracker.trackCellDiscovery(tempCell, currentSimulationStep);
	    				}
	    			}
	    		}
    		}
    	}
    }

    /**
     * Gets the cell given its coordinate.
     * @param row row number (zero based)
     * @param col column number (zero based).
     * @return a city's Cell.
     */
    public Cell get(int row, int col) {
        return map[row][col];
    }

    @XmlTransient
    public Map<AgentType, List<Cell>> getAgentList() {
        return agentList;
    }

    public void setAgentList(Map<AgentType, List<Cell>> agentList) {
        this.agentList = agentList;
    }

    public String toString() {
        //TODO: show a human readable summary of the game settings.
        return "Game settings";
    }

    public String getShortString() {
        //TODO: list of agents
        return "Game settings: agent related string";
    }

    @XmlTransient
    public Map<CellType, List<Cell>> getCellsOfType() {
        return cellsOfType;
    }

    public void setCellsOfType(Map<CellType, List<Cell>> cells) {
        cellsOfType = cells;
    }

    public int getNumberOfCellsOfType(CellType type) {
        return cellsOfType.get(type).size();
    }

    public int getNumberOfCellsOfType(CellType type, boolean empty) {
        int max = 0;
        for(Cell cell : cellsOfType.get(type)) {
            if (cell.isEmpty()) {
                max++;
            }
        }
        return max;
    }

    public InfoAgent getInfoAgent(AgentType agentType, AID sender) {
        Optional<InfoAgent> infoAgent = agentList.get(agentType).stream().flatMap(cell -> ((PathCell) cell).getAgents().get(agentType).stream()).filter(i -> i.getAID().equals(sender)).findFirst();
        if(infoAgent.isPresent()) {
            return infoAgent.get();
        } else {
            throw new IllegalArgumentException("AID " +sender + " not found as a "+ agentType + " in the game");
        }
    }

    public int getCurrentSimulationStep() {
        return currentSimulationStep;
    }

    public boolean hasEnded() {
        return currentSimulationStep >= simulationSteps;
    }

    public long getCurrentRoundEnd() {
        return currentRoundEnd;
    }

    public Graph<Cell> getMapGraph() {
        return mapGraph;
    }

    public List<PathCell> getPathNeighbors(Cell destCell, boolean diagonally) {
        return getNeighbors(destCell, diagonally).stream().filter(c -> c instanceof PathCell).map(c -> (PathCell) c).collect(Collectors.toList());
    }

    private List<Cell> getNeighbors(Cell destCell, boolean diagonally) {
        List<Cell> neighbors = new LinkedList<>();
        int x = destCell.getX();
        int y = destCell.getY();
        int [] adj = {-1,0,1};
        for(int dx : adj) {
            for(int dy : adj) {
                boolean condition;
                if(diagonally) {
                    condition = Math.abs(dx)+Math.abs(dy)>=1;
                } else {
                    condition = Math.abs(dx)+Math.abs(dy)==1;
                }
                if(condition) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if(nx >= 0 && ny >= 0 && ny < map.length && nx < map[0].length) {
                        neighbors.add(map[ny][nx]);
                    }
                }
            }
        }
        return neighbors;
    }
    
    
    public PathCell getAgentCell(AgentType agentType, AID aid) {
        return (PathCell) getAgentList().get(agentType).stream().filter(c -> ((PathCell) c).getAgents().get(agentType).contains(new InfoAgent(agentType, aid)) ).findFirst().get();
    }

    public List<Cell> getManufacturingCenters() {
        return cellsOfType.get(CellType.MANUFACTURING_CENTER);
    }

    public Plan getShortestPlan(Cell currentCell, Cell destCell) {
        List<Cell> pathNeighbors = new ArrayList<>(getPathNeighbors(destCell, true));
        if(pathNeighbors.isEmpty()) {
            return null;
        }
        if(pathNeighbors.contains(currentCell)) {
            return new Plan(new LinkedList<>());
        }

        List<Cell> vList = getMapGraph().getShortestPath(currentCell, pathNeighbors);
        List<PathCell> pc = vList.stream().map(c -> (PathCell) c).collect(Collectors.toList());
        return new Plan(pc);
    }
    
    /**
     * This methods aims to divide the map into different subareas so the prospectors explore different parts of the map.
     */
    public void dividePathCellsInto(int numOfProspectors) {
    	LinkedList<Cell> unAssignedCells = new LinkedList(this.getCellsOfType().get(CellType.PATH));
    	LinkedList<Cell> temporalCells = new LinkedList<Cell>();
    	cellAssignement = new HashMap<Integer,List<Cell>>();
    	final int paramExpansion = 2;
    	int totalPathCells = unAssignedCells.size();
        int cellsPerAgent = totalPathCells/numOfProspectors;
        Cell tempCell = null;
        temporalCells.add(unAssignedCells.get(0));
        
        for (int i = 0; i < numOfProspectors; i++) {
			for (int j = 0; j < cellsPerAgent; j++) {
				if(temporalCells.isEmpty()) {
					break;
				}
				if(!cellAssignement.containsKey(i)) {
					cellAssignement.put(i,new ArrayList<Cell>());
				}
				tempCell = temporalCells.pop();
				//Assing the Cell to the current prospector
				cellAssignement.get(i).add(tempCell);
				unAssignedCells.remove(tempCell);
				
				//add neighbors to the temporal list
				temporalCells.addAll(getPathNeighbors(tempCell, true));		
				//remove from temporal list all elements already assigned.
				temporalCells.removeIf(s -> !unAssignedCells.contains(s));
				//We will try to force the expansion in only one direction
				int currentX = tempCell.getX();
				int currentY = tempCell.getY();
				temporalCells.removeIf(s -> (Math.abs(currentX-s.getX()) + Math.abs(currentY-s.getY())> paramExpansion));
			}
			if(unAssignedCells.isEmpty()) {
				break;
			}else if(temporalCells.isEmpty()) {
				temporalCells.add(unAssignedCells.get(0));
			}else {
				tempCell = temporalCells.pop();
				temporalCells.clear();
				temporalCells.add(tempCell);
			}
		}
        
        //deep copy of map
        Map<Integer,List<Cell>> copyCellAssignement = new HashMap<Integer,List<Cell>>();
        for(int i = 0; i < numOfProspectors; i++) {
        	copyCellAssignement.put(i, new LinkedList<Cell>(cellAssignement.get(i)));
        }
        
        getMapGraph();
        for(Cell cell : unAssignedCells) {
        	int assignedProspector = Integer.MAX_VALUE;
        	int minDistance = Integer.MAX_VALUE;
        	for(int i = 0; i < numOfProspectors; i++) {
        		int temp = mapGraph.getShortestDistance(cell, copyCellAssignement.get(i));
        		if(temp<minDistance) {
        			minDistance = temp;
        			assignedProspector = i;
        		}
        	}
        	if(cellAssignement.containsKey(assignedProspector)) {
        		cellAssignement.get(assignedProspector).add(cell);
        		
        	}
        }
        
        
        //this part is just used for testing, it will set a different color for the cells depending on the prospector assigned with a maximum of 9
        for(int prospector : cellAssignement.keySet()) {
        	for(Cell cell : cellAssignement.get(prospector)) {
        		cell.prospectorDivision = prospector;
        	}
        }
        
    }

	public Map<Integer, List<Cell>> getCellAssignement() {
		return cellAssignement;
	}
    
    public List<Cell> getExplorationArea(AID prospector){
    	if(!areaAssignament.containsKey(prospector)) {
    		Map<CellType, List<Cell>> temp = getCellsOfType();
            return temp.get(CellType.PATH);
    	}else {
    		return cellAssignement.get(areaAssignament.get(prospector));
    	}
    }
    
    public ManufacturingCenterCell getClosestManufacturingCenter(Cell currentCell, MetalType currentMetal) {
        List<Cell> mfcs = getManufacturingCenters();
        ManufacturingCenterCell bestManufacturingCenter = null;
        double bestRatio = 0;

        for(Cell cell: mfcs) {
            ManufacturingCenterCell manufacturingCenter = (ManufacturingCenterCell) cell;
            if(manufacturingCenter.getMetal() != currentMetal) {
                continue;
            }
            List<Cell> pathNeighbors = new ArrayList<>(getPathNeighbors(cell, true));
            List<Cell> shortestPath = getMapGraph().getShortestPath(currentCell, pathNeighbors);
            double ratio = ((double) manufacturingCenter.getPrice()) / ((double) shortestPath.size());
            if(ratio > bestRatio) {
                bestRatio = ratio;
                bestManufacturingCenter = manufacturingCenter;
            }
        }
        return bestManufacturingCenter;
    }
}
