package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

/**
 * A point in a maze
 */
abstract public class MazePoint {

    // Variables
    int row;
    int col;
    Bitmap img;
    SnakeEngine engine;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public SnakeEngine getEngine() {
        return engine;
    }

    public void setEngine(SnakeEngine engine) {
        this.engine = engine;
    }

    abstract public void isOn() { }

}

