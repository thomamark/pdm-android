package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

/**
 * A maze through which a Panda must traverse in order
 * to collect pandas
 */
public class Maze {

    private MazePoint[][] myMaze;
    private static final int defaultMazeWidth = 32;
    private static final int defaultMazeHeight = 16;

    public Maze() {
        this.myMaze = new MazePoint[defaultMazeHeight][defaultMazeWidth];
    }

    public Bitmap getBitMap(int row, int col) {
        return myMaze[row][col].getBitmap();
    }

    public MazePoint.PointType getType(int row, int col) {
        return myMaze[row][col].getType();
    }

    public int getGoal(int row, int col) {
        if(myMaze[row][col].getType() == MazePoint.PointType.GOAL) {
            return ((GoalPoint) myMaze[row][col]).getGoal();
        } else {
            return -1;
        }
    }

    public MazePoint[][] getPoints() {
        return myMaze;
    }

}