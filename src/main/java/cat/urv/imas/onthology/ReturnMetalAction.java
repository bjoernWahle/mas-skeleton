package cat.urv.imas.onthology;

import jade.content.Concept;

public class ReturnMetalAction extends MobileAgentAction implements Concept {

    int x;
    int y;
    int amount;

    public String getMetal() {
        return metal;
    }

    public void setMetal(String metal) {
        this.metal = metal;
    }

    String metal;

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

    public ReturnMetalAction() {
        super(ActionType.RETURN.toString());
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ReturnMetalAction(int x, int y, int amount, String metal) {
        super(ActionType.RETURN.toString());
        this.x = x;
        this.y = y;
        this.amount = amount;
        this.metal = metal;
    }

    public ReturnMetalAction(InfoAgent agent, int x, int y, int amount, String metal) {
        super(ActionType.RETURN.toString(), agent);
        this.x = x;
        this.y = y;
        this.amount =amount;
        this.metal = metal;
    }
}
