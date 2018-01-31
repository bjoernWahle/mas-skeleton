package cat.urv.imas.gui;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.FieldCell;
import cat.urv.imas.map.ManufacturingCenterCell;
import cat.urv.imas.map.PathCell;
import cat.urv.imas.onthology.MetalType;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Visualization of the map. There are several elements to depict, as buildings,
 * streets, the agents, and the rest of items.<br>
 * This class *should be* modified and improved in order to show as good as
 * possible all the changes in the simulation. We provide several high-level
 * methods which can be rewritten as needed.<br>
 *
 * <b>Company:</b> Universitat Rovira i Virgili (<a
 * href="http://www.urv.cat">URV</a>)
 */
public class MapVisualizer extends JPanel implements CellVisualizer {
	
	private final static boolean TEST_DIVISION_PROSPECTORS = false;
	
    /**
     * Margin area surrouding the city map.
     */
    private static final int INSET = 50;
    /**
     * Cell gap.
     */
    private static final int GAP = 5;
    /**
     * Light red color.
     */
    private static final Color LIGHT_RED = new Color(255, 102, 102);
    /**
     * Brown (field) color.
     */
    private static final Color BROWN = new Color(150, 100, 60);

    /**
     * Gold color
     */
    private static final Color GOLD = new Color(255, 215, 0);

    /**
     * Silver color
     */
    private static final Color SILVER = new Color(192, 192, 192);

    /**
     * City map.
     */
    private final Cell[][] map;
    /**
     * Cell width.
     */
    private final int dx;
    /**
     * Cell height.
     */
    private final int dy;
    /**
     * Dimensions for the cell's border.
     */
    private final Rectangle2D.Double cellBorder;
    /**
     * The way agents are shown into the map.
     */
    private final Ellipse2D.Double agentFigure;
    /**
     * Graphics when painting the city map.
     *
     * @see cat.urv.imas.gui.CellVisualizer
     */
    private Graphics2D temporaryGraphics;

    /**
     * Initializes values for a correct map painting.
     *
     * @param map
     */
    public MapVisualizer(Cell[][] map) {
        this.map = map;
        int nrows = map.length;
        int ncols = map[0].length;

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point start = new Point(INSET, INSET);
        Point end = new Point(Math.min(screenSize.width, 1280) - INSET * 2, Math.min(screenSize.height, 1024) - INSET * 2);

        dx = (end.x - start.x) / ncols;
        dy = ((end.y - start.y) / nrows) - 4;

        cellBorder = new Rectangle2D.Double(GAP + 10, GAP + 10, dx, dy);
        agentFigure = new Ellipse2D.Double(GAP + 10 + (dx / 4), GAP + 10 + (dy / 4), (dx / 2), (dy / 2));

    }

    /**
     * Show the whole city map.
     *
     * @param graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;

        // 0. initializing graphic element.
        clear(graphics);

        // 1. set graphics for painting
        this.updateGraphics(g2d);

        int y = 0;

        // 2. iterate cells and paint them.
        for (Cell[] row : map) {
            int x = 0;

            for (Cell cell : row) {
                // 2.0 draw axes
                if(y == 0) {
                    drawString(Integer.toString(x),Color.BLACK,dx/2, 10);
                }
                if(x == 0) {
                    drawString(Integer.toString(y),Color.BLACK,0, 40);
                }

                // 2.1. draw the border.
                g2d.draw(cellBorder);
                // 2.2. draw the cell
                cell.draw(this);
                // 2.3. move on
                g2d.translate(dx, 0);

                x++;
            }

            // 2.4. move on
            g2d.translate(-(dx * row.length), dy);
            y++;
        }

        // 5. unsetting graphics
        this.updateGraphics(null);
        // 6. painting reinforcement
        this.repaint();
    }

    protected void clear(Graphics g) {
        super.paintComponent(g);
    }

    protected Ellipse2D.Double getCircle() {
        return (agentFigure);
    }

    protected void drawAgent(Color fillingColor, String message, Color textColor) {
        temporaryGraphics.setPaint(fillingColor);
        temporaryGraphics.translate((dx / 6), (dy / 6));
        temporaryGraphics.fill(agentFigure);
        temporaryGraphics.translate(-(dx / 6), -(dy / 6));
        drawString(message, textColor, dx - 40, dy - 10);
    }

    /**
     * Just draws a cell with the given filling in color, and the given border
     * color.
     *
     * @param fillingColor cell's color
     * @param borderColor color for the cell's border
     */
    protected void drawCell(Color fillingColor, Color borderColor) {
        temporaryGraphics.setPaint(fillingColor);
        temporaryGraphics.fill(cellBorder);
        temporaryGraphics.setPaint(borderColor);
        temporaryGraphics.draw(cellBorder);
    }

    /**
     * Draws the message into the cell, in the given position within the cell.
     *
     * @param message text to show.
     * @param textColor color for the text
     * @param x some value related to dx
     * @param y some value realted to dy
     */
    protected void drawString(String message, Color textColor, int x, int y) {
        if (!message.equals("")) {
            temporaryGraphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = new Font("Serif", Font.PLAIN, 11);
            temporaryGraphics.setFont(font);
            temporaryGraphics.setPaint(textColor);
            temporaryGraphics.drawString(message, x, y);
        }
    }

    /* ****************** CellVisualizer implementation ********************* */
    @Override
    public void updateGraphics(Graphics2D graphics) {
        this.temporaryGraphics = graphics;
    }

    @Override
    public void drawProspector(PathCell cell) {
        drawEmptyPath(cell);
        drawAgent(Color.BLUE, cell.getMapMessage(), Color.BLACK);
    }

    @Override
    public void drawDigger(PathCell cell) {
        drawEmptyPath(cell);
        drawAgent(Color.RED, cell.getMapMessage(), Color.BLACK);
    }

    @Override
    public void drawEmptyPath(PathCell cell) {
    	if(!TEST_DIVISION_PROSPECTORS) {
    		drawCell(Color.LIGHT_GRAY.brighter(), Color.DARK_GRAY);
    	}else {
        	switch (cell.prospectorDivision) {
			case 0:
				//drawCell(Color.getHSBColor(175, 255, 245), Color.DARK_GRAY);
				drawCell(Color.LIGHT_GRAY.brighter(), Color.DARK_GRAY);
				break;
			case 1:
				//drawCell(Color.getHSBColor(255, 255, 245), Color.DARK_GRAY);
				drawCell(Color.GREEN, Color.DARK_GRAY);
				break;
			case 2:
				//drawCell(Color.getHSBColor(45, 255, 245), Color.DARK_GRAY);
				drawCell(Color.YELLOW, Color.DARK_GRAY);
				break;
			case 3:
				//drawCell(Color.getHSBColor(88, 255, 245), Color.DARK_GRAY);
				drawCell(Color.PINK, Color.DARK_GRAY);
				break;
			case 4:
				//drawCell(Color.getHSBColor(127, 255, 245), Color.DARK_GRAY);
				drawCell(Color.CYAN, Color.DARK_GRAY);
				break;
			case 5:
				//drawCell(Color.getHSBColor(26, 255, 245), Color.DARK_GRAY);
				drawCell(Color.BLUE, Color.DARK_GRAY);
				break;
			case 6:
				//drawCell(Color.getHSBColor(169, 255, 245), Color.DARK_GRAY);
				drawCell(Color.RED, Color.DARK_GRAY);
				break;
			case 7:
				//drawCell(Color.getHSBColor(217, 255, 245), Color.DARK_GRAY);
				drawCell(Color.MAGENTA, Color.DARK_GRAY);
				break;
			case 8:
				//drawCell(Color.getHSBColor(58, 255, 245), Color.DARK_GRAY);
				drawCell(Color.ORANGE, Color.DARK_GRAY);
				break;
			case 9:
				//drawCell(Color.getHSBColor(15, 255, 245), Color.DARK_GRAY);
				drawCell(Color.WHITE, Color.DARK_GRAY);
				break;
			default:
				drawCell(Color.GRAY.brighter(), Color.DARK_GRAY);
				break;
			}
        	
        }
        
    }

    @Override
    public void drawField(FieldCell cell) {
    	
        Color color = BROWN;
        if(!cell.getMapMessage().equals("")) {
            MetalType metalType = MetalType.fromShortString(cell.getMapMessage().substring(0,1));
            if (metalType == MetalType.GOLD) {
                color = GOLD;
            } else {
                color = SILVER;
            }
        }
        drawCell(color, Color.DARK_GRAY);
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
        
    }

    @Override
    public void drawManufacturingCenter(ManufacturingCenterCell cell) {
        drawCell(Color.RED, Color.GREEN);
        drawString(cell.getMapMessage(), Color.BLACK, dx - 40, dy);
    }

    @Override
    public void drawAgents(PathCell cell) {
    	try {
    		drawEmptyPath(cell);
            drawAgent(LIGHT_RED, cell.getMapMessage(), Color.BLACK);
    	}catch(NullPointerException e) {
    		e.printStackTrace();
    	}
        
    }

}
