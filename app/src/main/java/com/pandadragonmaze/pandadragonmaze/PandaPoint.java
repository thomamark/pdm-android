package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class PandaPoint extends MazePoint {

    /**
     * A Panda
     */

    public PandaPoint(int row, int col, Bitmap img, SnakeEngine engine) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.engine = engine;
    }

    public PandaPoint(int row, int col, SnakeEngine engine) {
        this(row, col, engine.panda, engine);
    }

    public void isOn() {
        this.engine.pickUp(this);
    }
}
