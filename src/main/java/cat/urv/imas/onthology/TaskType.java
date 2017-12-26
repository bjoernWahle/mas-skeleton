package cat.urv.imas.onthology;

public enum TaskType {
    COLLECT_METAL, RETURN_METAL;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static TaskType fromString(String string) {
        for(TaskType taskType: TaskType.values()) {
            if(taskType.toString().equals(string)) {
                return taskType;
            }
        }
        throw new IllegalArgumentException("Illegal task type: "+ string);
    }
}
