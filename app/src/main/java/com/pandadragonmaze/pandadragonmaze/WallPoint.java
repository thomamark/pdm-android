package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class WallPoint extends MazePoint {

    public WallPoint(int row, int col, Bitmap img, SnakeEngine engine) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
    }

    public void isOn() {
        // You die when you hit a wall :(
        this.engine.die();
    }

}