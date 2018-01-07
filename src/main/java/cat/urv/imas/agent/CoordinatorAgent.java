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

import cat.urv.imas.behaviour.coordinator.CoordinatorBehaviour;
import cat.urv.imas.behaviour.coordinator.RequesterBehaviour;
import cat.urv.imas.onthology.ActionList;
import cat.urv.imas.onthology.GameHasEnded;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

/**
 * The main Coordinator agent.
 * TODO: This coordinator agent should get the game settings from the System
 * agent every round and share the necessary information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    private GameSettings game;
    /**
     * System agent id.
     */
    public AID systemAgent;

    /**
     * Digger coordinator agent
     */
    public AID diggerCoordinatorAgent;

    /**
     * prospector coordinator agent
     */
    public AID prospectorCoordinatorAgent;

    private ActionList diggerActions;
    private ActionList prospectorActions;
    // TODO stats

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        super.setup();


        this.systemAgent = findSystemAgent();
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.DIGGER_COORDINATOR.toString());
        this.diggerCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);
        searchCriterion.setType(AgentType.PROSPECTOR_COORDINATOR.toString());
        this.prospectorCoordinatorAgent = UtilsAgents.searchAgent(this, searchCriterion);

        // setup finished. When we receive the last inform, the agent itself will add
        // a behaviour to send/receive actions
        this.addBehaviour(new CoordinatorBehaviour(this));
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    public void broadcastCurrentGameStatus() {
        log("Broadcasting the current game status");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.setSender(getAID());
        message.addReceiver(diggerCoordinatorAgent);
        message.addReceiver(prospectorCoordinatorAgent);
        try {
            message.setContentObject(getGame());
            send(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetDiggerActions() {
        diggerActions = null;
    }

    public void resetProspectorActions() {
        prospectorActions = null;
    }

    public void setDiggerActions(ActionList diggerActions) {
        this.diggerActions = diggerActions;
    }

    public void setProspectorActions(ActionList prospectorActions) {
        this.prospectorActions = prospectorActions;
    }

    public void notifySystemAgent() {
    	//We concatenate both digger actions and prospector actions
    	ActionList agentActions = new ActionList(diggerActions.getAgentActions());
    	agentActions.addAgentActions(prospectorActions.getAgentActions());
    	
        log("Sending received actions and stats to the almighty System Agent. God bless him.");
        ACLMessage msg = prepareMessage(ACLMessage.INFORM);
        msg.addReceiver(systemAgent);
        try {
            getContentManager().fillContent(msg, agentActions);
            send(msg);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Unable to fill message content: " + agentActions);
        }
    }

    public void broadCastGameHasEnded() {
        ACLMessage message = prepareMessage(ACLMessage.INFORM);
        message.addReceiver(diggerCoordinatorAgent);
        message.addReceiver(prospectorCoordinatorAgent);
        try {
            getContentManager().fillContent(message, new GameHasEnded());
            send(message);
        } catch (Codec.CodecException | OntologyException e) {
            e.printStackTrace();
            log("Error filling message content");
        }
    }
}
