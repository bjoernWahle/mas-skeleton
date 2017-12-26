/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.agent;

import cat.urv.imas.onthology.DiggerOntology;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntology;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * Agent abstraction used in this practical work.
 * It gathers common attributes and functionality from all agents.
 */
abstract public class ImasAgent extends Agent {
    public ContentManager manager = getContentManager();
    // This agent "speaks" the SL language
    private Codec codec = new SLCodec();
    private Ontology ontology;


    /**
     * Type of this agent.
     */
    protected AgentType type;
    
    /**
     * Agents' owner.
     */
    public static final String OWNER = "urv";
    /**
     * Language used for communication.
     */
    public static final String LANGUAGE = "fipa-sl";
    /**
     * Onthology used in the communication.
     */
    public static final String ONTOLOGY = "digger-ontology";
    
    /**
     * Creates the agent.
     * @param type type of agent to set.
     */
    public ImasAgent(AgentType type) {
        super();
        this.type = type;
    }

    @Override
    protected void setup() {
        super.setup();
                /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(type.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());

        // Set ontology
        try {
            ontology = DiggerOntology.getInstance();
        } catch (BeanOntologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        manager.registerLanguage(codec);
        manager.registerOntology(ontology);

        try {
            DFService.register(this, dfd);
            log("Hey there!");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
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
        System.out.println(getType()+":"+getAID().getName()+" terminating.");
    }
    
    /**
     * Informs the type of agent.
     * @return the type of agent.
     */
    public AgentType getType() {
        return this.type;
    }
    
    /**
     * Add a new message to the log.
     *
     * @param str message to show
     */
    public void log(String str) {
        System.out.println(getLocalName() + ": " + str);
    }
    
    /**
     * Add a new message to the error log.
     *
     * @param str message to show
     */
    public void errorLog(String str) {
        System.err.println(getLocalName() + ": " + str);
    }


    AID findSystemAgent() {
        // search SystemAgent
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        return UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

    }

    ACLMessage prepareMessage(int messageType, AID receiver) {
        ACLMessage message = new ACLMessage(messageType);
        message.setSender(getAID());
        message.addReceiver(receiver);
        message.setLanguage(LANGUAGE);
        message.setOntology(ONTOLOGY);
        return message;
    }
}
