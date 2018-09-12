package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * A point in a maze
 */
abstract public class MazePoint {

    public static enum types {
        BLANK(0),
        WALL(1),
        PANDA(2),
        GOAL(3),
        START(4);

        public int value;

        types(int v) {
            this.value = v;
        }

    }
//
//    // Mapping from point types to string representations
//    public static HashMap<String, Integer> pointMap;
//
//    static {
//        pointMap = new HashMap<String, Integer>();
//        pointMap.put("Blank", new Integer(0));
//        pointMap.put("Wall", new Integer(1));
//        pointMap.put("Panda", new Integer(2));
//        pointMap.put("Goal", new Integer(3));
//        pointMap.put("Start", new Integer(4));
//    }

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

    /**
     * Do something when this point is added to a Maze
     * @param m Maze to which this point is being added
     * @return true if this point is to be stored in the point array, false otherwise
     */
    public boolean addTo(Maze m) {
        return true;
    }

    abstract public void isOn();



}

