package cat.urv.imas.agent;

import jade.core.AID;

public class ProspectorAgent extends ImasAgent implements MovingAgentInterface {

    private int currentCol;
    private int currentRow;

    public ProspectorAgent() {
        super(AgentType.PROSPECTOR);
    }

    @Override
    public void setup() {
        super.setup();

        // TODO implement and add behaviours
        // set starting position
        String[] args = (String[]) getArguments();
        currentCol = Integer.parseInt(args[0]);
        currentRow = Integer.parseInt(args[1]);

        log("I am at ("+ currentCol +","+ currentRow +")!");

        // add behaviours
    }

    @Override
    public int stepsToPosition(int row, int col) {
        // easy approach: euclidean distance
        int yDistance = Math.abs (row - currentRow);
        int xDistance = Math.abs (col - currentCol);
        double distance = Math.sqrt((yDistance)*(yDistance) +(xDistance)*(xDistance));
        return (int) Math.ceil(distance);
    }
}
