package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class GoalPoint extends MazePoint {

    /**
     * A goal point with a destination
     */
    private int goal;

    public GoalPoint(int row, int col, Bitmap img, int goal) {
        super(row, col, img, PointType.GOAL);
        this.goal = goal;
    }

    public int getGoal() {
        return this.goal;
    }
}
