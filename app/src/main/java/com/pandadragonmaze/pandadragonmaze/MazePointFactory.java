package com.pandadragonmaze.pandadragonmaze;

public class MazePointFactory {

    public static MazePoint getPoint(String s, int r, int c, int base, int pad_w, SnakeEngine engine) {

        int type = Integer.parseInt(s.substring(0, pad_w), base);

        switch (type) {
            case 0: // Blank
                //Blank points should keep the point undefined, so do nothing here
                return null;
            case 1: // Wall
                return new WallPoint(r, c, engine);
            case 2: // Panda
                return new PandaPoint(r, c, engine);
            case 3: // Goal
                // Next encoded digit specifies destination
                int goal = Integer.parseInt(s.substring(pad_w, 2*pad_w), base);
                return new GoalPoint(r, c, engine, goal);
            case 4: // Start
                return new StartPoint(r, c, engine);
        }

        //should never get here
        return null;
    }
}
