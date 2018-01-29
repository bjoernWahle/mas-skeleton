package cat.urv.imas.onthology;

import jade.content.Predicate;

public class InformAgentRound implements Predicate {
    MobileAgentAction action;

    Task finishedTask;

    public InformAgentRound(MobileAgentAction action, Task finishedTask) {
        this.action = action;
        this.finishedTask = finishedTask;
    }

    public InformAgentRound(MobileAgentAction action) {
        this.action = action;
    }

    public Task getFinishedTask() {
        return finishedTask;
    }

    public void setFinishedTask(Task finishedTask) {
        this.finishedTask = finishedTask;
    }

    public InformAgentRound() {

    }

    public MobileAgentAction getAction() {
        return action;
    }

    public void setAction(MobileAgentAction action) {
        this.action = action;
    }
}
