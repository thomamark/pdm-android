package com.pandadragonmaze.pandadragonmaze;

import android.graphics.Bitmap;

public class PandaPoint extends MazePoint {
    /**
     * A Panda
     */

    public PandaPoint(int row, int col, Bitmap img) {
        super(row, col, img, PointType.PANDA);
    }
}
