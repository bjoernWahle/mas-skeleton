package cat.urv.imas.onthology;

public enum TaskState {
    NOT_STARTED, IN_PROGRESS, DONE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static TaskState fromString(String string) {
        for(TaskState taskState: TaskState.values()) {
            if(taskState.toString().equals(string)) {
                return taskState;
            }
        }
        throw new IllegalArgumentException("Illegal task state: "+ string);
    }
}