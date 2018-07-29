package com.pandadragonmaze.pandadragonmaze;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Point;
import android.view.Display;

public class SnakeActivity extends Activity {

    // Declare an instance of com.pandadragonmaze.pandadragonmaze.SnakeEngine
    SnakeEngine snakeEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the pixel dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();

        // Initialize the result into a Point object
        Point size = new Point();
        display.getSize(size);

        // Create a new instance of the com.pandadragonmaze.pandadragonmaze.SnakeEngine class
        snakeEngine = new SnakeEngine(this, size);

        // Make snakeEngine the view of the Activity
        setContentView(snakeEngine);
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }
}
