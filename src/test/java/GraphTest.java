import cat.urv.imas.map.Cell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.InitialGameSettings;

import java.util.List;

public class GraphTest {

    public static void main(String[] args) {
        testGraph();
    }

    private static void testGraph() {
        GameSettings gameSettings = InitialGameSettings.load("game.settings");
        gameSettings.getMapGraph();
        }
}
