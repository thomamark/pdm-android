package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

/**
 * A point in a maze
 */
public class MazePoint {

    public enum PointType {WALL, PANDA, GOAL, DRAGON};

    private int row;
    private int col;
    private Bitmap img;
    private PointType type;

    public MazePoint(int row, int col, Bitmap img) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.type = PointType.WALL;
    }

    public MazePoint(int row, int col, Bitmap img, PointType type) {
        this.row = row;
        this.col = col;
        this.img = img;
        this.type = type;
    }

    public Bitmap getBitmap() {
        return this.img;
    }

    public PointType getType() {
        return this.type;
    }

}

