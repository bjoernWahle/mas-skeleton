package cat.urv.imas.onthology;

import jade.content.Predicate;

public class ProposeTask implements Predicate {
    DiggerTask task;

    public ProposeTask() {

    }

    public ProposeTask(DiggerTask task) {
        this.task = task;
    }

    public DiggerTask getTask() {
        return task;
    }

    public void setTask(DiggerTask task) {
        this.task = task;
    }
}
