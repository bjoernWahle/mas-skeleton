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
package cat.urv.imas.gui;

import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.map.PathCell;

import java.awt.*;

/**
 * Enabling draw each kind of cell.
 */
public interface CellVisualizer {

    public void updateGraphics(Graphics2D graphics);

    public void drawProspector(PathCell cell);

    public void drawDigger(PathCell cell);

    public void drawManufacturingCenter(ManufacturingCenterCell cell);

    public void drawEmptyPath(PathCell cell);

    public void drawField(FieldCell cell);

    public void drawAgents(PathCell aThis);
}
