package cat.urv.imas.onthology;

public abstract class Task {

    public enum TaskState {
        NOT_STARTED, IN_PROGRESS, DONE
    }

    private TaskState currentState;

    public Task() {
        this.currentState = TaskState.NOT_STARTED;
    }

    public Task(TaskState currentState) {
        this.currentState = currentState;
    }

    public TaskState getCurrentState() {
        return currentState;
    }

    public void startTask() {
        if(currentState == TaskState.NOT_STARTED) {
            currentState = TaskState.IN_PROGRESS;
        } else {
            throw new IllegalStateException("Illegal state to start task: " +currentState);
        }
    }

    public void finishTask() {
        if(currentState == TaskState.IN_PROGRESS) {
            currentState = TaskState.DONE;
        } else {
            throw new IllegalStateException("Illegal state to finish task:" + currentState);
        }
    }

}
