package cat.urv.imas.onthology;

public class DiggerTask extends Task {

    public enum TaskType {
        COLLECT_METAL, RETURN_METAL
    }

    public final int x;
    public final int y;

    final TaskType taskType;

    public TaskType getTaskType() {
        return taskType;
    }

    public DiggerTask(int x, int y, TaskType taskType) {
        super();
        this.x = x;
        this.y = y;
        this.taskType = taskType;
    }

    public DiggerTask(TaskState currentState, int x, int y, TaskType taskType) {
        super(currentState);
        this.x = x;
        this.y = y;
        this.taskType = taskType;
    }
}
