package cat.urv.imas.onthology;

import jade.content.Predicate;

public abstract class Task implements Predicate {

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    private String currentState;

    public Task() {
        this.currentState = "not_started";
    }

    public Task(String currentState) {
        this.currentState = currentState;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void startTask() {
        if(currentState == TaskState.NOT_STARTED.toString()) {
            currentState = TaskState.IN_PROGRESS.toString();
        } else {
            throw new IllegalStateException("Illegal state to start task: " +currentState);
        }
    }

    public void finishTask() {
        if(currentState == TaskState.IN_PROGRESS.toString()) {
            currentState = TaskState.DONE.toString();
        } else {
            throw new IllegalStateException("Illegal state to finish task:" + currentState);
        }
    }

}
