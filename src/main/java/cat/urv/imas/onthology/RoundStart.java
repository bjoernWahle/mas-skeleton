package cat.urv.imas.onthology;

import jade.content.Predicate;

public class RoundStart implements Predicate {
    private int x;
    private int y;
    private long roundEnd;

    public RoundStart() {

    }

    public RoundStart(int x, int y, long roundEnd) {
        setX(x);
        setY(y);
        setRoundEnd(roundEnd);
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

    public long getRoundEnd() {
        return roundEnd;
    }

    public void setRoundEnd(long roundEnd) {
        this.roundEnd = roundEnd;
    }
}
