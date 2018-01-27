package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import jade.content.Concept;
import jade.core.AID;

/**
 * <p>
 * <b>Copyright:</b> Copyright (c) 2013</p>
 * <p>
 * <b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)</p>
 */
public class InfoAgent implements Concept {

    /**
     * Type of this agent.
     */
    protected final AgentType type;
    /**
     * AID for the related agent.
     */
    protected AID aid;

    /**
     * Building new instance with only the type.
     *
     * @param type type of agent.
     */
    public InfoAgent(AgentType type) {
        this.type = type;
    }

    /**
     * Building new instance specifying its type and its AID.
     *
     * @param type agent type.
     * @param aid agent id.
     */
    public InfoAgent(AgentType type, AID aid) {
        this.type = type;
        this.aid = aid;
    }

    /**
     *
     * @param a
     * @return
     */
    @Override
    public boolean equals(Object a) {
        if (a instanceof InfoAgent | a instanceof DiggerInfoAgent | a instanceof ProspectorInfoAgent) {
            return ((InfoAgent) a).getAID().equals(this.aid);
        } else {
            return false;
        }
    }

    /**
     * Gets the hash code. To simplify it, just returns the hash code from itsç
     * type.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return type.hashCode();
    }

    /**
     * Gets agent id.
     *
     * @return agent id.
     */
    public AID getAID() {
        return this.aid;
    }

    /**
     * Sets the agent id.
     *
     * @param aid agent id.
     */
    public void setAID(AID aid) {
        this.aid = aid;
    }

    /**
     * Type of agent.
     *
     * @return type of agent.
     */
    public AgentType getType() {
        return this.type;
    }

    /**
     * String representation of this isntance.
     *
     * @return string representation.
     */
    @Override
    public String toString() {
        return "(info-agent (agent-type " + this.getType() + ")"
                + ((null != this.aid) ? (" (aid " + this.aid + ")") : "")
                + ")";
    }

    /**
     * Gets a short information about the type of the agent to show in the map.
     * @return short string with info about the agent type.
     */
    public String getMapMessage() {
        return type.getShortString()+ this.aid.getLocalName().substring(this.aid.getLocalName().length()-1);
    }
}
