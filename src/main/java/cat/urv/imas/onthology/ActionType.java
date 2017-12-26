package cat.urv.imas.onthology;

public enum ActionType {
    MOVE, COLLECT, RETURN, DETECT;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static ActionType fromString(String string) {
        for(ActionType actionType: ActionType.values()) {
            if(actionType.toString().equals(string)) {
                return actionType;
            }
        }
        throw new IllegalArgumentException("Illegal action type: "+ string);
    }
}
