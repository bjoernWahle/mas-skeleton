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
    int stepTime = 20000;

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

    public List<FieldCell> detectFieldsWithMetal(int row, int col) {
        //Find all surrounding cells to (row,col) that are
        //buildings and have garbage on it.
        //Use: FieldCell.detectMetal() to do so.
    	List<FieldCell> detectedMetals = new ArrayList<FieldCell>();
    	for (int i=-1; i<2; i++) {
    		for (int j=-1;j<2;j++) {
	    		if((row+i) < map.length && (col+j) < map[0].length && (row+i)>=0 && (col+j) >= 0) {
	    			if (map[row+i][col+j].getCellType() == CellType.FIELD) {
	    				FieldCell tempCell = (FieldCell) map[row+i][col+j];
	    				if(!tempCell.detectMetal().isEmpty()) {
	    					detectedMetals.add(tempCell);
	    					statisticsTracker.trackCellDiscovery(tempCell, currentSimulationStep);
	    				}
	    			}
	    		}
    		}
    	}
        return detectedMetals;
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
        if(mapGraph == null) {
            buildGraphFromMap();
        }
        return mapGraph;
    }

    private void buildGraphFromMap() {
        ArrayList<Vertex<Cell>> vertices = new ArrayList<>();
        for(Cell[] cellRow: map) {
            for(Cell cell : cellRow) {
                vertices.add(new Vertex<>(cell));
            }
        }
        this.mapGraph = new Graph<Cell>(vertices);
        // add edges
        int[] adj = {-1, 0, 1};
        for(Vertex<Cell> vc : mapGraph.getVertices().values()) {
            // get neighbour cells
            Cell c = vc.getLabel();
            for(PathCell pc : getPathNeighbors(c, false)) {
                Vertex<Cell> nvc = mapGraph.getVertex(pc);
                mapGraph.addEdge(vc, nvc);
            }
        }

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
    
    /**
     * Method for adding metals found by the prospectors
     */
    public void addFoundMetals(List<FieldCell> newMetals) {
    	//We first remove them to avoid repetition
    	foundMetals.removeAll(newMetals);
    	//Then we add them to have them all
    	foundMetals.addAll(newMetals);
    	//updateFoundedMetals();
    }
    
    private void updateFoundedMetals() {
    	List<Cell> fields = cellsOfType.get(CellType.FIELD);
    	for (Cell field : fields) {
    		FieldCell temp = (FieldCell) field;
    		if (foundMetals.contains(temp)){
    			temp.detectMetal();
    		}else {
    			temp.removeDetected();
    		}
    		
    	}
    }

    public PathCell getAgentCell(AgentType agentType, AID aid) {
        return (PathCell) getAgentList().get(agentType).stream().filter(c -> ((PathCell) c).getAgents().get(agentType).contains(new InfoAgent(agentType, aid)) ).findFirst().get();
    }

    public List<Cell> getManufacturingCenters() {
        return cellsOfType.get(CellType.MANUFACTURING_CENTER);
    }
}
