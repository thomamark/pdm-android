package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class GoalPoint extends MazePoint {

    /**
     * A goal point with a destination
     */
    private int goal;

    public GoalPoint(int row, int col, Bitmap img, SnakeEngine engine, int goal) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
        this.goal = goal;
    }

    public int getGoal() {
        return this.goal;
    }

    public void isOn() {
        this.engine.loadLevel(this.goal);
    }
}
