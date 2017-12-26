package cat.urv.imas.onthology;

import jade.core.AID;

public class CollectMetalBid implements java.io.Serializable {
    private AID agent;
    private int time;
    private double remainingCapacity;

    public CollectMetalBid(AID agent, int time, double remainingCapacity) {
        this.agent = agent;
        this.time = time;
        this.remainingCapacity = remainingCapacity;
    }

    @Override
    public String toString() {
        return "CollectMetalBid{" +
                "agent=" + agent +
                ", time=" + time +
                ", remainingCapacity=" + remainingCapacity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectMetalBid that = (CollectMetalBid) o;

        if (time != that.time) return false;
        if (Double.compare(that.remainingCapacity, remainingCapacity) != 0) return false;
        return agent != null ? agent.equals(that.agent) : that.agent == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agent != null ? agent.hashCode() : 0;
        result = 31 * result + time;
        temp = Double.doubleToLongBits(remainingCapacity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public AID getAgent() {

        return agent;
    }

    public void setAgent(AID agent) {
        this.agent = agent;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(double remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }
}
