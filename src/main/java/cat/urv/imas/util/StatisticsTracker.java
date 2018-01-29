package cat.urv.imas.util;

import cat.urv.imas.map.Cell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StatisticsTracker implements java.io.Serializable {

    private List<Integer> explorationTimes;
    private List<Integer> collectionTimes;

    private Map<Cell, Integer> cellAppearances;
    private Map<Cell, Integer> cellDiscoveries;

    public StatisticsTracker() {
        explorationTimes = new LinkedList<>();
        collectionTimes = new LinkedList<>();

        cellAppearances = new HashMap<>();
        cellDiscoveries = new HashMap<>();
    }

    public void trackCellAppearance(Cell cell, Integer round, boolean visible) {
        if(visible) {
            cellDiscoveries.put(cell, round);
        } else {
            cellAppearances.put(cell, round);
        }
    }

    public void trackCellDiscovery(Cell cell, Integer round) {
        if(cellAppearances.containsKey(cell)) {
            Integer appearance = cellAppearances.get(cell);
            explorationTimes.add(round-appearance);
            cellAppearances.remove(cell);
        }
        cellDiscoveries.put(cell, round);
    }

    public void trackCellCollection(Cell cell, Integer round) {
        if(cellDiscoveries.containsKey(cell)) {
            Integer discovery = cellDiscoveries.get(cell);
            collectionTimes.add(round-discovery);
            cellDiscoveries.remove(cell);
        }
    }

    public double getAverageDiscoveryTime() {
        if(!explorationTimes.isEmpty()) {
            return explorationTimes.stream().mapToDouble(d -> d).average().getAsDouble();
        } else {
            return -1.0;
        }
    }

    public double getAverageCollectionTime() {
        if(!collectionTimes.isEmpty()) {
            return collectionTimes.stream().mapToDouble(d -> d).average().getAsDouble();
        } else {
            return -1.0;
        }
    }
}
