package cat.urv.imas.onthology;

import cat.urv.imas.agent.onthology.DiggerAction;

public class MoveAction extends DiggerAction {

    public final int x;
    public final int y;


    public MoveAction(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
