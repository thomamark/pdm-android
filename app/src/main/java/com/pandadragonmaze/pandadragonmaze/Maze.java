package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;
import android.util.Pair;

/**
 * A maze through which a Panda must traverse in order
 * to collect pandas
 */
public class Maze {

    private MazePoint[][] myPoints;
    private static final int defaultMazeWidth = 32;
    private static final int defaultMazeHeight = 16;

    /* Maze encoding variables */
    private static final int ENC_W = 2; //num of digits per point in a string
    private static final int DEFAULT_BASE = 16; //Default digit base for string output (currently hexadecimal)
//    private static final int WI = 0; // Index in Width
    private static final int SIZEI = 1;

    private int height;
    private int width;
    private int maxPandas;
    private SnakeEngine engine;

    private int entryRow = -1;
    private int entryCol = -1;

    private int startRow = -1;
    private int startCol = -1;

    public Maze(SnakeEngine engine) {
        this(defaultMazeHeight, defaultMazeWidth, engine);
    }

    public Maze(int height, int width, SnakeEngine engine) {
        this.myPoints = new MazePoint[height][width];
        this.height = height;
        this.width = width;
        this.engine = engine;
        this.maxPandas = 0;
    }

    public Bitmap getBitMap(int row, int col) {
        return (myPoints[row][col] == null) ? null : myPoints[row][col].getImg();
    }

    public boolean hasPoint(int r, int c) {
        return myPoints[r][c] != null;
    }

    public MazePoint[][] getPoints() {
        return myPoints;
    }

    public void addPoint(MazePoint p) {
        if (p.addTo(this)) {
            myPoints[p.getRow()][p.getCol()] = p;
        }
    }

    public void addPanda(){
        this.maxPandas += 1;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setStart(int r, int c) {
        this.startRow = r;
        this.startCol = c;
    }

    public void setEntry(int r, int c) {
        this.entryRow = r;
        this.entryCol = c;
    }

    public boolean hasStart() {
        return (this.startRow != -1 && this.startCol != -1);
    }

    public boolean hasEntry() {
        return (this.entryRow != -1 && this.entryCol != -1);
    }

    /**
     * Returns Pair<Integer row, Integer col> of entry or null if not set
     * @return a Pair object with Integers row, column of entry point
     */
    public Pair<Integer, Integer> getEntry() {
        if (hasEntry()) {
            return new Pair(new Integer(this.entryRow), new Integer(this.entryCol));
        } else {
            return null;
        }
    }

    /**
     * Returns Pair<Integer row, Integer col> of start or null if not set
     * @return a Pair object with Integers row, column of entry point
     */
    public Pair<Integer, Integer> getStart() {
        if (hasStart()) {
            return new Pair(new Integer(this.startRow), new Integer(this.startCol));
        } else {
            return null;
        }
    }


    public void isOn(int r, int c) {
        if (hasPoint(r, c)) {
            myPoints[r][c].isOn();
        }
    }

    public String toString() {
        return "";
    }

    public static Maze load(String s, SnakeEngine engine) throws Exception {
        if (s.length() < 3) {
            throw new Exception("Maze sting must have at least 3 characters");
        }

//      try {

        //Parse the header
        int base = DEFAULT_BASE;
        int w = Integer.parseInt(s.substring(0, SIZEI), base);

        // Read #rows & # cols
        int rows   = Integer.parseInt(s.substring(SIZEI,SIZEI+w), base);
        int cols   = Integer.parseInt(s.substring(SIZEI+w, SIZEI+2*w), base);

        // Theme is an artifact of an earlier version and is ignored
        int themeId   = Integer.parseInt(s.substring(SIZEI+2*w, SIZEI+3*w), base);

        int headerLength = SIZEI+3*w;

        // Track which row/column we're on so we add points in the right places
        int r = 0;
        int c = 0;

        Maze myMaze = new Maze(rows, cols, engine);
        MazePoint nextPoint;

        for (int i = headerLength; i < s.length(); i += w){
            nextPoint =
                    MazePointFactory.getPoint(s.substring(i, s.length()), r, c, base, w, engine);
            if (nextPoint != null) {
                myMaze.addPoint(nextPoint);

                if (nextPoint instanceof GoalPoint) {
                    // took two characters to ingest, so advance the counter an extra char
                    i += w;
                }
            }

            //Keep track of which row/column we're on. Maze is loaded in columns-first order.
            c += 1;
            if (c == cols) {
                    c = 0;
                    r += 1;
                }
        }

        if (!(myMaze.hasStart())) {
            throw new Exception("Maze has no starting point defined!");
        }
//        } catch Exception(e) {
//            throw "Error loading maze: " + err;
//        }
//        this.fixWallImages();
        return myMaze;
    }


}