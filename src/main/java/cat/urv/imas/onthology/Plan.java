package cat.urv.imas.onthology;

import cat.urv.imas.map.PathCell;

import java.util.List;

public class Plan {

    List<PathCell> pathCellList;

    public Plan(List<PathCell> pathCellList) {
        this.pathCellList = pathCellList;
    }

    public List<PathCell> getPathCellList() {
        return pathCellList;

    }

    public void setPathCellList(List<PathCell> pathCellList) {
        this.pathCellList = pathCellList;
    }

    public PathCell getFirst() {
        return pathCellList.get(0);
    }
}
