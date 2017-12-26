package cat.urv.imas.onthology;

import jade.content.Predicate;

import java.util.List;

public class AgentList implements Predicate {
    private List<InfoAgent> agentList;

    public AgentList() {

    }

    public AgentList(List<InfoAgent> agentList) {
        this.agentList = agentList;
    }

    public List<InfoAgent> getAgentList() {
        return agentList;
    }

    public void setAgentList(List<InfoAgent> agentList) {
        this.agentList = agentList;
    }
}
