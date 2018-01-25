package cat.urv.imas.onthology;

import jade.content.Concept;
import jade.content.Predicate;

public class DiggerTask extends Task {

    public int x;
    public int y;
    public String taskType;
    public String metalType;
    public int amount;

    @Override
    public String toString() {
        return "DiggerTask{" +
                "x=" + x +
                ", y=" + y +
                ", taskType='" + taskType + '\'' +
                ", metalType='" + metalType + '\'' +
                ", amount=" + amount +
                ", last=" + last +
                ", state=" + getCurrentState() +
                '}';
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean last = false;

    public DiggerTask() {

    }

    public DiggerTask(DiggerTask task) {
        this.x = task.x;
        this.y = task.y;
        this.taskType = task.taskType;
        this.metalType = task.metalType;
        this.amount = task.amount;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public DiggerTask(int x, int y, String taskType, String metalType, int amount) {
        this.x = x;
        this.y = y;
        this.taskType = taskType;
        this.metalType = metalType;
        this.amount = amount;
    }

    public DiggerTask(String currentState, int x, int y, String taskType, String metalType, int amount) {
        super(currentState);
        this.x = x;
        this.y = y;
        this.taskType = taskType;
        this.metalType = metalType;
        this.amount = amount;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getMetalType() {
        return metalType;
    }

    public void setMetalType(String metalType) {
        this.metalType = metalType;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
