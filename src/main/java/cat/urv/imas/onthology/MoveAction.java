package cat.urv.imas.onthology;

import jade.content.Concept;
import jade.core.AID;

public class MoveAction extends MobileAgentAction implements Concept {

    public AID aid;

    public AID getAid() {
        return aid;
    }

    public void setAid(AID aid) {
        this.aid = aid;
    }

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

    public MoveAction(AID aid, int x, int y) {
        super(ActionType.MOVE.toString());
        this.aid = aid;
        this.x = x;
        this.y = y;
    }

}
