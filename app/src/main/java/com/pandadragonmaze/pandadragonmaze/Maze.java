package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

/**
 * A maze through which a Panda must traverse in order
 * to collect pandas
 */
public class Maze {

    private MazePoint[][] myPoints;
    private static final int defaultMazeWidth = 32;
    private static final int defaultMazeHeight = 16;

    private int height;
    private int width;
    private SnakeEngine engine;

    public Maze(SnakeEngine engine) {
        this.myPoints = new MazePoint[defaultMazeHeight][defaultMazeWidth];
        this.height = defaultMazeHeight;
        this.width = defaultMazeWidth;
        this.engine = engine;
    }

    public Maze(int height, int width, SnakeEngine engine) {
        this.myPoints = new MazePoint[height][width];
        this.height = height;
        this.width = width;
        this.engine = engine;
    }


    public Bitmap getBitMap(int row, int col) {
        return (myPoints[row][col] == null) ? null : myPoints[row][col].getImg();
    }

    public boolean hasPoint(int r, int c) {
        return myPoints[r][c] != null;
    }

//    public MazePoint.PointType getType(int row, int col) {
//        return myPoints[row][col].getType();
//    }

//    public int getGoal(int row, int col) {
//        if(myPoints[row][col].getType() == MazePoint.PointType.GOAL) {
//            return ((GoalPoint) myPoints[row][col]).getGoal();
//        } else {
//            return -1;
//        }
//    }

    public MazePoint[][] getPoints() {
        return myPoints;
    }

    public void addPoint(MazePoint p) {
        myPoints[p.getRow()][p.getCol()] = p;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void isOn(int r, int c) {
        if (hasPoint(r, c)) {
            myPoints[r][c].isOn();
        }
    }


}