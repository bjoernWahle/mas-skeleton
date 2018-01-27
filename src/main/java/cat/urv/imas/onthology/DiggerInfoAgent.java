/**
 * IMAS base code for the practical work.
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.onthology;

import cat.urv.imas.agent.AgentType;
import jade.core.AID;

/**
 * Agent information for digger agents.
 */
public class DiggerInfoAgent extends InfoAgent {

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Maximum units of metal
     */
    public int maxCapacity;
    /**
     * Current units of metal
     */
    public int capacity = 0;

    public DiggerInfoAgent() {
        super(AgentType.DIGGER);
    }

    public DiggerInfoAgent(AgentType type, int maxCapacity) {
        super(type);
        this.maxCapacity = maxCapacity;
    }

    public DiggerInfoAgent(AgentType type, AID aid) {
        super(type, aid);
    }

    public DiggerInfoAgent(AgentType type, AID aid, int maxCapacity) {
        super(type, aid);
        this.maxCapacity =maxCapacity;
    }

    /**
     * String representation of this isntance.
     *
     * @return string representation.
     */
    @Override
    public String toString() {
        return "(info-agent (agent-type " + this.getType() + ")"
                + ((null != this.getAID()) ? (" (aid " + this.getAID() + ")") : "")
                + " (capacity " + capacity + ")"
                + " (maxCapacity "+ maxCapacity + ")"
                + ")";
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String getMapMessage() {
        return type.getShortString()+ this.aid.getLocalName().substring(this.aid.getLocalName().length()-1)+"("+capacity+"/"+maxCapacity+")";
    }
}
