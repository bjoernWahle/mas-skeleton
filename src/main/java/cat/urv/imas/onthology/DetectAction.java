package cat.urv.imas.onthology;

import jade.content.Concept;

public class DetectAction extends MobileAgentAction implements Concept {

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

    public DetectAction() {
        super(ActionType.DETECT.toString());
    }

    public DetectAction(int x, int y) {
        super(ActionType.DETECT.toString());
        this.x = x;
        this.y = y;
    }

    public DetectAction(int x, int y, InfoAgent aid) {
        super(ActionType.DETECT.toString(), aid);
        this.x = x;
        this.y = y;
    }

}
