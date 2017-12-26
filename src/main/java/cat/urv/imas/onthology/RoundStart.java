package cat.urv.imas.onthology;

import jade.content.Predicate;

public class RoundStart implements Predicate {
    private int x;
    private int y;

    public RoundStart() {

    }

    public RoundStart(int x, int y) {
        setX(x);
        setY(y);
    }

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
}
