package cat.urv.imas.agent;

import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

public class ProspectorAgent extends ImasAgent {

    /**
     * System agent id.
     */
    private AID systemAgent;

    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }

    @Override
    public void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(type.toString());
        sd.setName(getName());
        sd.setOwnership(OWNER);
        dfd.setName(getAID());
        dfd.addServices(sd);

        this.systemAgent = findSystemAgent();

        try {
            DFService.register(this,dfd);

        } catch (FIPAException e) {
            doDelete();
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
        System.out.println("Prospector-agent"+getAID().getName()+" terminating.");
    }
}
