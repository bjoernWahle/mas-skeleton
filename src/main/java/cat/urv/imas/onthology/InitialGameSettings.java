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
import jade.content.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.*;

/**
 * Initial game settings and automatic loading from file.
 *
 * Use the GenerateGameSettings to build the game.settings configuration file.
 */
@XmlRootElement(name = "InitialGameSettings")
public class InitialGameSettings extends GameSettings implements Predicate {

    /**
     * Path cell.
     */
    public static final int P = 0;
    /**
     * Digger cell.
     */
    public static final int DC = -1;
    /**
     * Prospector cell.
     */
    public static final int PC = -2;
    /**
     * Manufacturing center cell.
     */
    public static final int MCC = -3;
    /**
     * Field cell.
     */
    public static final int F = -4;

    /**
     * City initialMap. Each number is a cell. The type of each is expressed by a
     * constant (if a letter, see above), or a building (indicating the number
     * of people in that building).
     */
    private int[][] initialMap
            = {
            {F, F, F, MCC, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F},
            {F, P, P, P, P, P, P, P, P, P, P, DC, P, P, P, P, P, P, P, F},
            {F, P, PC, P, P, P, P, DC, P, P, P, P, P, P, P, P, P, P, DC, F},
            {F, P, P, F, F, F, F, F, F, P, P, F, F, F, F, F, F, F, F, F},
            {F, P, P, F, F, F, F, F, MCC, P, P, F, F, F, F, F, F, F, F, F},
            {F, PC, P, F, F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F},
            {F, P, P, F, F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, P, F, F, P, DC, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, PC, F, F, P, P, F, F, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, MCC, F, P, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, DC, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, DC, P, F},
            {F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, P, MCC, F, P, P, F, F, P, P, F, F, P, P, F, F, P, P, F},
            {F, P, PC, P, DC, P, P, F, F, P, P, P, P, P, P, F, F, P, P, F},
            {F, P, P, P, P, P, P, F, F, P, P, P, P, P, P, F, F, DC, P, F},
            {F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F},
    };

    /**
     * Number of initial elements to put in the map.
     */
    private int numberInitialElements = 0;
    /**
     * Number of those initial elements which will be visible from
     * the very beginning. At maximum, this value will be numberInitialElements.
     */
    private int numberVisibleInitialElements = 0;
    /**
     * Random number generator.
     */
    private Random numberGenerator;

    /**
     * points collected
     */
    private int collectedPoints = 0;
    private int totalMetal = 0;

    @XmlElement(required = true)
    public void setNumberInitialElements(int initial) {
        numberInitialElements = initial;
    }

    public int getNumberInitialElements() {
        return numberInitialElements;
    }

    @XmlElement(required = true)
    public void setNumberVisibleInitialElements(int initial) {
        numberVisibleInitialElements = initial;
    }

    public int getNumberVisibleInitialElements() {
        return numberVisibleInitialElements;
    }

    public int[][] getInitialMap() {
        return initialMap;
    }

    @XmlElement(required = true)
    public void setInitialMap(int[][] initialMap) {
        this.initialMap = initialMap;
    }

    public static final InitialGameSettings load(String filename) {
        if (filename == null) {
            filename = "game.settings";
        }
        try {
            // create JAXBContext which will be used to update writer
            JAXBContext context = JAXBContext.newInstance(InitialGameSettings.class);
            Unmarshaller u = context.createUnmarshaller();
            InitialGameSettings starter = (InitialGameSettings) u.unmarshal(new FileReader(filename));
            starter.initMap();
            return starter;
        } catch (Exception e) {
            System.err.println("Loading of settings from file '" + filename + "' failed!");
            System.exit(-1);
        }
        return null;
    }

    /**
     * Initializes the cell map.
     * @throws Exception if some error occurs when adding agents.
     */
    private void initMap() throws Exception {
        int rows = this.initialMap.length;
        int cols = this.initialMap[0].length;
        map = new Cell[rows][cols];
        int manufacturingCenterIndex = 0;
        this.agentList = new HashMap();
        this.foundMetals = new LinkedList<>();
        numberGenerator = new Random(this.getSeed());

        int cell;
        PathCell c;
        Map<CellType, List<Cell>> cells = new HashMap();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                cell = initialMap[row][col];
                switch (cell) {
                    case DC:
                        c = new PathCell(row, col);
                        c.addAgent(new DiggerInfoAgent(AgentType.DIGGER, getDiggersCapacity()));
                        map[row][col] = c;
                        addAgentToList(AgentType.DIGGER, c);
                        break;
                    case PC:
                        c = new PathCell(row, col);
                        c.addAgent(new ProspectorInfoAgent(AgentType.PROSPECTOR));
                        map[row][col] = c;
                        addAgentToList(AgentType.PROSPECTOR, c);
                        break;
                    case P:
                        map[row][col] = new PathCell(row, col);
                        break;
                    case MCC:
                        if (manufacturingCenterIndex >= manufacturingCenterPrice.length) {
                            throw new Error(getClass().getCanonicalName() + " : More manufacturing centers in the map than given prices");
                        }
                        if (manufacturingCenterIndex >= manufacturingCenterMetalType.length) {
                            throw new Error(getClass().getCanonicalName() + " : More manufacturing centers in the map than given metal types");
                        }
                        map[row][col] = new ManufacturingCenterCell(row, col, manufacturingCenterPrice[manufacturingCenterIndex], manufacturingCenterMetalType[manufacturingCenterIndex]);
                        manufacturingCenterIndex++;
                        break;
                    case F:
                        // Only SystemAgent can access to the SettableFieldCell
                        map[row][col] = new SettableFieldCell(row, col);
                        break;
                    default:
                        throw new Error(getClass().getCanonicalName() + " : Unexpected type of content in the 2D map");
                }
                CellType type = map[row][col].getCellType();
                List<Cell> list;
                if (cells.containsKey(type)) {
                    list = cells.get(type);
                } else {
                    list = new LinkedList();
                    cells.put(type, list);
                }
                list.add(map[row][col]);
            }
        }

        this.setCellsOfType(cells);

        if (manufacturingCenterIndex != manufacturingCenterPrice.length) {
            throw new Error(getClass().getCanonicalName() + " : Less manufacturing centers in the map than given prices.");
        }
        if (manufacturingCenterIndex != manufacturingCenterMetalType.length) {
            throw new Error(getClass().getCanonicalName() + " : Less manufacturing centers in the map than given metal types.");
        }
        if (0 > this.getNumberInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of elements.");
        }
        int availableCells = getNumberOfCellsOfType(CellType.FIELD);
        if (availableCells < this.getNumberInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : You set up more new initial elements ("+ this.getNumberInitialElements() +")than existing cells ("+ availableCells +").");
        }
        if (0 > this.getNumberVisibleInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of visible elements.");
        }
        if (this.getNumberVisibleInitialElements() > this.getNumberInitialElements()) {
            throw new Error(getClass().getCanonicalName() + " : More visible elements than initial elements.");
        }

        int maxInitial = this.getNumberInitialElements();
        int maxVisible = this.getNumberVisibleInitialElements();

        addElements(maxInitial, maxVisible);
        buildGraphFromMap();
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


    public void addElements(int maxElements, int maxVisible) {
        CellType ctype = CellType.FIELD;
        int maxCells = getNumberOfCellsOfType(ctype);
        int freeCells = this.getNumberOfCellsOfType(ctype, true);
        maxElements = Math.min(maxElements, freeCells);

        if (maxElements < 0) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of elements.");
        }
        if (maxElements > freeCells) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed add more elements than empty cells.");
        }
        if (maxVisible < 0) {
            throw new Error(getClass().getCanonicalName() + " : Not allowed negative number of visible elements.");
        }
        if (maxVisible > maxElements) {
            throw new Error(getClass().getCanonicalName() + " : More visible elements than number of elements.");
        }

        System.out.println(getClass().getCanonicalName() + " : Adding " + maxElements +
                " elements (" + maxVisible + " of them visible) on a map with " +
                maxCells + " cells (" + freeCells + " of them candidate).");

        if (0 == maxElements) {
            return;
        }

        Set<Integer> initialSet = new TreeSet();
        int index;
        while (initialSet.size() < maxElements) {
            index = numberGenerator.nextInt(maxCells);
            if (isEmpty(index)) {
                initialSet.add(index);
            }
        }

        Set<Integer> visibleSet = new TreeSet();
        Object[] initialCells = initialSet.toArray();
        while (visibleSet.size() < maxVisible) {
            visibleSet.add((Integer)initialCells[numberGenerator.nextInt(maxElements)]);
        }

        MetalType[] types = MetalType.values();
        MetalType type;
        int amount;
        boolean visible;
        for (int i: initialSet) {
            type = types[numberGenerator.nextInt(types.length)];
            amount = numberGenerator.nextInt(this.getMaxAmountOfNewMetal()) + 1;
            visible = visibleSet.contains(i);
            setElements(type, amount, visible, i);
        }
    }

    /**
     * Tells whether the given cell is empty of elements.
     * @param ncell nuber of cell.
     * @return true when empty.
     */
    private boolean isEmpty(int ncell) {
        return ((SettableFieldCell)cellsOfType.get(CellType.FIELD).get(ncell)).isEmpty();
    }

    /**
     * Set up the amount of elements of the given type on the cell specified by
     * ncell. It will be visible whenever stated.
     * @param type type of elements to put in the map.
     * @param amount amount of elements to put into.
     * @param ncell number of cell from a given list.
     * @param visible visible to agents?
     */
    private void setElements(MetalType type, int amount, boolean visible, int ncell) {
        SettableFieldCell cell = (SettableFieldCell)cellsOfType.get(CellType.FIELD).get(ncell);
        cell.setElements(type, amount);
        statisticsTracker.trackCellAppearance(cell, currentSimulationStep, visible);
        increaseTotalMetal(amount);
        if (visible) {
            cell.detectMetal();
            foundMetals.add(cell);
        }
    }

    private void increaseTotalMetal(int amount) {
        totalMetal = totalMetal + amount;
    }

    /**
     * Process the request of adding new elements onto the map to be run
     * every simulation step.
     *
     * Mainly, it checks the probability of having new elements. If so,
     * it finds the number of cells with new elements, to finally add
     * new elements to the given number of cells.
     *
     * This process also checks that if there is room for the given number of
     * cells. Otherwise and error is thrown.
     */
    public void addElementsForThisSimulationStep() {
        int probabilityOfNewElements = this.getNewMetalProbability();
        int stepProbability = numberGenerator.nextInt(100) +1;

        if (stepProbability < probabilityOfNewElements) {
            System.out.println(getClass().getCanonicalName() + " : " + stepProbability +
                    " < " + probabilityOfNewElements +
                    " (step probability for new elements < probability of new elements)");
            return;
        }

        int maxCells = this.getMaxNumberFieldsWithNewMetal();
        int numberCells = numberGenerator.nextInt(maxCells) + 1;

        // add elements to the given number of cells for this simulation step.
        // all of them hidden.
        addElements(numberCells, 0);
    }

    /**
     * Ensure agent list is correctly updated.
     *
     * @param type agent type.
     * @param cell cell where appears the agent.
     */
    private void addAgentToList(AgentType type, Cell cell) {
        List<Cell> list = this.agentList.get(type);
        if (list == null) {
            list = new ArrayList();
            this.agentList.put(type, list);
        }
        list.add(cell);
    }

    public Cell findAgentsCell(InfoAgent agent) {
        Optional<Cell> cell = getAgentList().get(agent.getType()).stream().filter(c -> ((PathCell) c).getAgents().get(agent.getType()).contains(agent)).findFirst();
        if(cell.isPresent()) {
            return cell.get();
        } else {
            throw new IllegalArgumentException("Agent "+ agent+ "was not found in the game");
        }
    }

    public void applyMove(MoveAction moveAction) throws IllegalStateException,IllegalArgumentException {
        InfoAgent agent = moveAction.getAgent();
        Cell newCell = get(moveAction.y, moveAction.x);
        if(newCell instanceof PathCell) {
            // if everything ok, remove agent from old cell and add it to the new cell
            PathCell newPathCell = (PathCell) newCell;
            Cell oldCell = findAgentsCell(agent);
            PathCell oldPathCell = (PathCell) oldCell;
            if(!oldCell.adjacent(newCell, false)) {
                throw new IllegalArgumentException("Refusing move request to "+moveAction.x +"," + moveAction.y +" because the cell is not adjacent to the current cell" );
            }
            if(((PathCell) newCell).isThereADiggerAgentWorking()) {
                throw new IllegalArgumentException("Refusing move request to "+moveAction.x +"," + moveAction.y +" because there is a digger working at this cell");
            }
            newPathCell.addAgent(agent);
            oldPathCell.removeAgent(agent);
            // check cells that were updated
            // check new cell
            if(!agentList.get(agent.getType()).contains(newCell)) {
                // add cell to agentList
                this.agentList.get(agent.getType()).add(newCell);
            }
            // check old cell
            if(oldPathCell.getAgents().get(AgentType.DIGGER).isEmpty() && oldPathCell.getAgents().get(AgentType.PROSPECTOR).isEmpty()) {
                this.agentList.get(agent.getType()).remove(oldCell);
            }
        } else {
            throw new IllegalArgumentException("Refusing move request to " +moveAction.x +"," + moveAction.y + " because the Cell is not a PathCell.");
        }
    }

    public void setCurrentSimulationStep(int currentSimulationStep) {
        this.currentSimulationStep = currentSimulationStep;
    }

    public void advanceToNextRound() {
        this.currentSimulationStep++;
    }

    public void applyCollectMetal(CollectMetalAction collectAction) {
        DiggerInfoAgent agent = (DiggerInfoAgent) getInfoAgent(collectAction.getAgent().getType(),collectAction.getAgent().getAID());
        PathCell agentCell = getAgentCell(agent.getType(), agent.getAID());
        if(!agentCell.collectingAllowed()) {
            throw new IllegalArgumentException("Refusing collect request from "+agentCell.getX() +"," + agentCell.getY() +" because there are too many agents on the cell");
        }
        Cell destCell = get(collectAction.y, collectAction.x);
        if(!destCell.adjacent(agentCell, true)) {
            throw new IllegalArgumentException("Refusing collect request from "+agentCell.getX() +"," + agentCell.getY() +" because it is not adjacent to the diggers position.");
        }
        if(!(destCell instanceof FieldCell)) {
            throw new IllegalArgumentException("Refusing collect request from "+agentCell.getX() +"," + agentCell.getY() +" because it is not a field cell");
        } else {
            FieldCell fieldCell = (FieldCell) destCell;
            if(!fieldCell.wasFound()) {
                throw new IllegalArgumentException("Refusing collect request from "+agentCell.getX() +"," + agentCell.getY() +" because it was not found yet.");
            }
            if(fieldCell.getMetalAmount() == 0) {
                throw new IllegalArgumentException("Refusing collect request from "+agentCell.getX() +"," + agentCell.getY() +" because there is no metal left.");
            }
            fieldCell.removeMetal();
            agentCell.setDiggerWorking(true);
            statisticsTracker.trackCellCollection(fieldCell, currentSimulationStep);
            agent.setCapacity(agent.getCapacity()+1);
        }
    }

    public void applyReturnMetal(ReturnMetalAction returnAction) {
        DiggerInfoAgent agent = (DiggerInfoAgent) getInfoAgent(returnAction.getAgent().getType(),returnAction.getAgent().getAID());
        Cell cell = get(returnAction.getY(), returnAction.getX());
        PathCell agentCell = getAgentCell(agent.getType(), agent.getAID());
        if(!agentCell.adjacent(cell, true)) {
            throw new IllegalArgumentException("Refusing return metal to "+cell.getX() + ","+cell.getY()+ " because the cell is not adjacent to the agents position.");
        }
        if(!(cell instanceof ManufacturingCenterCell)) {
            throw new IllegalArgumentException("Refusing return metal to "+cell.getX() + ","+cell.getY()+ " because it is not a manufacturing center");
        }
        ManufacturingCenterCell mfc = (ManufacturingCenterCell) cell;
        if(!mfc.getMetal().getShortString().equals(returnAction.getMetal())) {
            throw new IllegalArgumentException("Refusing return metal to "+cell.getX() + ","+cell.getY()+ " because the manufacturing center accepts a different metal.");
        }
        if(returnAction.amount > agent.getCapacity()) {
            throw new IllegalArgumentException("Refusing return metal to "+cell.getX() + ","+cell.getY()+ " because the agent does not carry the amount that it wants to return.");
        }
        // if no exception was thrown, apply action;
        mfc.addManufacturedMetal(returnAction.amount);
        collectedPoints = collectedPoints+(returnAction.amount * mfc.getPrice());
        agent.setCapacity(agent.getCapacity()-returnAction.amount);
    }
    
    public void applyDetection(DetectAction detectAction) {
    	detectFieldsWithMetal(detectAction.getY(), detectAction.getX());
    }

    public int getCollectedPoints() {
        return collectedPoints;
    }

    public int getTotalMetal() {
        return totalMetal;
    }

    public void checkFoundMetals() {
        List<FieldCell> cellsToRemove = new LinkedList<>();
        for(FieldCell cell : foundMetals) {
            if(!cell.wasFound()) {
                cellsToRemove.add(cell);
            }
        }
        foundMetals.removeAll(cellsToRemove);
    }

    public int getManufacturedMetal(MetalType metalType) {
        int manufacturedMetal = 0;
        for(Cell cell: getCellsOfType().get(CellType.MANUFACTURING_CENTER)) {
            ManufacturingCenterCell mfc = (ManufacturingCenterCell) cell;
            if(mfc.getMetal() == metalType) {
                manufacturedMetal = manufacturedMetal+ mfc.getManufacturedMetal();
            }
        }
        return manufacturedMetal;
    }

    public double getAverageDiscoveryTime() {
        return statisticsTracker.getAverageDiscoveryTime();
    }

    public double getAverageCollectionTime() {
        return statisticsTracker.getAverageCollectionTime();
    }

    public double getRatioOfDiscoveredMetal() {
        int discoveredButNotCollected = 0;
        for(Cell cell : cellsOfType.get(CellType.FIELD)) {
            FieldCell fc = (FieldCell) cell;
            if(fc.wasFound()) {
                discoveredButNotCollected = discoveredButNotCollected + fc.getMetalAmount();
            }
        }
        return (discoveredButNotCollected + getManufacturedMetal(MetalType.SILVER) + getManufacturedMetal(MetalType.GOLD)) / ((double) getTotalMetal());
    }

    public void resetPathCells() {
        for(Cell cell : cellsOfType.get(CellType.PATH)) {
            PathCell pc = (PathCell) cell;
            pc.setDiggerWorking(false);
        }
    }
}
