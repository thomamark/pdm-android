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
    private int height;
    private int width;
    private SnakeEngine engine;

    public Maze(SnakeEngine engine) {
        this.myMaze = new MazePoint[defaultMazeHeight][defaultMazeWidth];
        this.height = defaultMazeHeight;
        this.width = defaultMazeWidth;
        this.engine = engine;
    }

    public Maze(int height, int width, SnakeEngine engine) {
        this.myMaze = new MazePoint[height][width];
        this.height = height;
        this.width = width;
        this.engine = engine;
    }


    public Bitmap getBitMap(int row, int col) {
        return myMaze[row][col].getImg();
    }

//    public MazePoint.PointType getType(int row, int col) {
//        return myMaze[row][col].getType();
//    }

//    public int getGoal(int row, int col) {
//        if(myMaze[row][col].getType() == MazePoint.PointType.GOAL) {
//            return ((GoalPoint) myMaze[row][col]).getGoal();
//        } else {
//            return -1;
//        }
//    }

    public MazePoint[][] getPoints() {
        return myMaze;
    }

}