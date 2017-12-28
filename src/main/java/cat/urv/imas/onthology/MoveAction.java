package cat.urv.imas.onthology;

import jade.content.Concept;

public class MoveAction extends MobileAgentAction implements Concept {

    public int x;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int y;

    public MoveAction() {
        super(ActionType.MOVE.toString());
    }

    public MoveAction(int x, int y) {
        super(ActionType.MOVE.toString());
        this.x = x;
        this.y = y;
    }

    public MoveAction(int x, int y, InfoAgent aid) {
        super(ActionType.MOVE.toString(), aid);
        this.x = x;
        this.y = y;
    }

}
