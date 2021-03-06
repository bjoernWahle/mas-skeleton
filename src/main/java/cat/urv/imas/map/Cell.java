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
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;

import java.util.Objects;

/**
 * This class keeps all the information about a cell in the map.
 * Coordinates (row, col) are zero based. This means all values goes from
 * [0..n-1], both included, for each dimension.
 */
public abstract class Cell implements java.io.Serializable {

    /**
     * Cell type.
     */
    private final CellType type;
    /**
     * Row number for this cell, zero based.
     */
    private int row = -1;
    /**
     * Column number for this cell, zero based.
     */
    private int col = -1;

    /**
     * Builds a cell with a given type.
     *
     * @param type Initial cell type.
     * @param row row number.
     * @param col column number.
     */
    public Cell(CellType type, int row, int col) {
        this.type = type;
        this.row = row;
        this.col = col;
    }
    
    /**
     * Variable just used for testing the map division
     */
    public int prospectorDivision = 99;

    /* ********************************************************************** */
    /**
     * Gets the current row.
     *
     * @return the current row number in the map, in zero base.
     */
    public int getY() {
        return this.row;
    }

    /**
     * Gets the current column number in the map, in zero base.
     *
     * @return Column number in the map, in zero base.
     */
    public int getX() {
        return this.col;
    }

    /**
     * Gets the current cell type.
     *
     * @return Cell type.
     */
    public CellType getCellType() {
        return this.type;
    }

    /**
     * Tells whether this cell is considered empty.
     * @return true when empty.
     */
    public boolean isEmpty() {
        return true;
    }

    /* ********************************************************************** */
    /**
     * Gets a string representation of the cell.
     *
     * @return
     */
    @Override
    public String toString() {
        String str = "(cell-type " + this.getCellType() + " "
                + "(r " + this.getY() + ")"
                + "(c " + this.getX() + ")";
        str += this.toStringSpecialization();
        return str + ")";
    }

    /**
     * Allows subclasses to build a specific string.
     * @return string specialization for the cell.
     */
    public String toStringSpecialization() {
        return "";
    }

    /* ************ Map visualization ****************************************/

    /**
     * The cell will be asked to be drawn, using the given CellVisualizer API.
     * To do so, it also has to override when necessary the getMessage() method.
     * @param visual provides the API to draw any kind of cell.
     */
    public abstract void draw(CellVisualizer visual);

    /**
     * Tells the message to show in the map. Empty string to paint nothing.
     * @return The text to show in the map, located in the current cell.
     */
    public String getMapMessage() {
        return "";
    }

    public boolean adjacent(Cell cell, boolean diagonally) {
        int dx = Math.abs(col - cell.getX());
        int dy = Math.abs(row - cell.getY());
        if(diagonally) {
            return (dx<=1 && dy<=1 && dx + dy >=1);
        } else {
            return dx + dy == 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return row == cell.row &&
                col == cell.col &&
                type == cell.type;
    }

    @Override
    public int hashCode() {

        return Objects.hash(type, row, col);
    }

}
