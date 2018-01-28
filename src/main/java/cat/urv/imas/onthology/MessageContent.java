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
package cat.urv.imas.onthology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";

    /**
     * Message sent from Digger- or Prospector coordinator to inform the diggers / prospectors that there is nothing to negotiate this round.
     */
    public static final String INFORM_NO_NEGOTIATION = "No reason to negotiate this round lads.";

    /**
     * Message sent from Digger- or Prospector coordinator to inform the diggers / prospectors that there are topics to negotiate this round.
     */
    public static final String INFORM_NEGOTIATION = "Prepare for some juicy negotiations fellas!";
}
