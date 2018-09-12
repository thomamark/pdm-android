package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class WallPoint extends MazePoint {


    /**
     * Create a Wall point with the provided image
     * @param row
     * @param col
     * @param img
     * @param engine
     */

    public WallPoint(int row, int col, Bitmap img, SnakeEngine engine) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
    }

    /**
     * Create a Wall point with the default image
     *
     * @param row
     * @param col
     * @param engine
     */
    public WallPoint(int row, int col, SnakeEngine engine) {
        this(row, col, engine.wall_block, engine);
    }

    public void isOn() {
        // You die when you hit a wall :(
        this.engine.die();
    }

}