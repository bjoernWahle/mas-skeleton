package cat.urv.imas.onthology;

import jade.content.Concept;

public class CollectMetalAction extends MobileAgentAction implements Concept {

    int x;
    int y;

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

    public CollectMetalAction() {
        super(ActionType.COLLECT.toString());
    }

    public CollectMetalAction(int x, int y) {
        super(ActionType.COLLECT.toString());
        this.x = x;
        this.y = y;
    }

    public CollectMetalAction(InfoAgent agent, int x, int y) {
        super(ActionType.COLLECT.toString(), agent);
        this.x = x;
        this.y = y;
    }
}
