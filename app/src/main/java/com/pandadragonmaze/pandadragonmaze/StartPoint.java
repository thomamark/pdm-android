package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class StartPoint extends MazePoint {


    /**
     * Create a Start point with the provided image
     * @param row
     * @param col
     * @param img
     * @param engine
     */

    public StartPoint(int row, int col, Bitmap img, SnakeEngine engine) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
    }

    /**
     * Create a Start point with the default image
     *
     * @param row
     * @param col
     * @param engine
     */
    public StartPoint(int row, int col, SnakeEngine engine) {
        this(row, col, engine.headDown, engine);
    }

    public void isOn() {
        // Do nothing when you're on a Start point
    }

    @Override
    public boolean addTo(Maze m) {
        m.setStart(this.row, this.col);
        return false;
    }

}