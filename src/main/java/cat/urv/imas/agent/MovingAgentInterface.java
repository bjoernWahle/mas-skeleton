package cat.urv.imas.agent;

interface MovingAgentInterface {

    /**
     * Returns the steps that agent needs to arrive at position (row, col).
     * Note that the returned value can be a approximation since the agent is not able to foresee how the environment
     * changes while moving to the destination.
     * @param row row of the destination cell
     * @param col col of the destination cell
     * @return number of steps needed to arrive at destination
     */
    int stepsToPosition(int row, int col);

}
