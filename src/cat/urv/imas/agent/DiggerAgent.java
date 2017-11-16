package cat.urv.imas.agent;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class DiggerAgent extends ImasAgent {

    /**
     * System agent id.
     */
    private AID systemAgent;

    public DiggerAgent() {
        super(AgentType.DIGGER);
    }

    @Override
    public void setup() { {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type.getClassName());
        sd.setName(getName());
        sd.setOwnership(OWNER);
        dfd.setName(getAID());
        dfd.addServices(sd);

        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        try {
            DFService.register(this,dfd);

        } catch (FIPAException e) {
            doDelete();
        }
    }
    }

    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println(type+":"+getAID().getName()+" terminating.");
    }
}
