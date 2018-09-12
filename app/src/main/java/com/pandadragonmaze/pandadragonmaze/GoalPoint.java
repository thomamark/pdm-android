package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class GoalPoint extends MazePoint {

    /**
     * A goal point with a destination
     */
    private int goal;

    /**
     * Create a goal point with the provided image
     *
     * @param row
     * @param col
     * @param img
     * @param engine
     * @param goal
     */
    public GoalPoint(int row, int col, Bitmap img, SnakeEngine engine, int goal) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
        this.goal = goal;
    }

    /**
     * Create a goal point with the default goal image
     *
     * @param row
     * @param col
     * @param engine
     * @param goal
     */
    public GoalPoint(int row, int col, SnakeEngine engine, int goal) {
        this(row, col, engine.goal, engine, goal);
    }

    /**
     * Return the goal for this goal point
     *
     * @return the destination of this goal point
     */
    public int getGoal() {
        return this.goal;
    }

    /**
     * Move the drakon to the new goal.
     */
    public void isOn() {
        this.engine.loadLevel(this.goal);
    }
}
