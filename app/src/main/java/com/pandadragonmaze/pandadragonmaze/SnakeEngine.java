//Code thanks to http://gamecodeschool.com/android/coding-a-snake-game-for-android/

package com.pandadragonmaze.pandadragonmaze;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;
import android.view.GestureDetector;

public class SnakeEngine extends SurfaceView implements Runnable {
    private static final String DEBUG_TAG = "Gestures";

    // Our game thread for the main game loop
    private Thread thread = null;

    // To hold a reference to the Activity
    private Context context;

    // for plaing sound effects
    private SoundPool soundPool;
    private int eat_bob = -1;
    private int snake_crash = -1;

    // For tracking movement Heading
    public enum Heading {UP, RIGHT, DOWN, LEFT}

    // Start by heading to the right
    private Heading heading = Heading.RIGHT;

    // Images for the Dragon's head
    private Bitmap headUp;
    private Bitmap headDown;
    private Bitmap headLeft;
    private Bitmap headRight;

    // Images for Dragon's body
    private Bitmap bodyR2L;

    // Current head image
    private Bitmap headImg;

    private int headFrameHeight;
    private int headFrameWidth;
    private int bodyFrameHeight;
    private int bodyFrameWidth;
    private static int SPRITE_SCALE = 1;


    // To hold the screen size in pixels
    private int screenX;
    private int screenY;

    // How long is the snake
    private int snakeLength;

    // Where is Bob hiding?
    private int bobX;
    private int bobY;

    // The size in pixels of a snake segment
    private int blockSize;
    private int blockHeight;
    private int blockWidth;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 32;
    private int numBlocksHigh;

    // Control pausing between updates
    private long nextFrameTime;
    // Update the game 10 times per second
    private final long FPS = 10;
    // There are 1000 milliseconds in a second
    private final long MILLIS_PER_SECOND = 1000;
    // We will draw the frame much more often

    // How many points does the player have
    private int score;

    // The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    // Everything we need for drawing
    // Is the game currently playing?
    private volatile boolean isPlaying;

    // A canvas for our paint
    private Canvas canvas;

    // Required to use canvas
    private SurfaceHolder surfaceHolder;

    // Some paint for our canvas
    private Paint paint;

    // For handling swipes
    private GestureDetectorCompat mDetector;

    public SnakeEngine(Context context, Point size) {
        super(context);

        this.context = context;

        screenX = size.x;
        screenY = size.y;

        // Work out how many pixels each block is
        blockSize = screenX / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        numBlocksHigh = screenY / blockSize;

        headFrameHeight = blockSize * SPRITE_SCALE;
        headFrameWidth = blockSize * SPRITE_SCALE;
        bodyFrameWidth = bodyFrameHeight = blockSize * SPRITE_SCALE;

        // Set the sound up
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        try {
            // Create objects of the 2 required classes
            // Use m_Context because this is a reference to the Activity
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the two sounds in memory
            descriptor = assetManager.openFd("pick_up.ogg");
            eat_bob = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("death_sound.ogg");
            snake_crash = soundPool.load(descriptor, 0);

            //Load head images
            headUp = BitmapFactory.decodeResource(this.getResources(), R.drawable.head_up);
            headUp = Bitmap.createScaledBitmap(headUp, headFrameWidth, headFrameHeight,false);

            headLeft = BitmapFactory.decodeResource(this.getResources(), R.drawable.head_left);
            headLeft = Bitmap.createScaledBitmap(headLeft,headFrameWidth, headFrameHeight,false);

            headRight = BitmapFactory.decodeResource(this.getResources(), R.drawable.head_right);
            headRight = Bitmap.createScaledBitmap(headRight,headFrameWidth, headFrameHeight,false);

            headDown = BitmapFactory.decodeResource(this.getResources(), R.drawable.head_down);
            headDown = Bitmap.createScaledBitmap(headDown,headFrameWidth, headFrameHeight,false);

            // Dragon starts by moving left
            headImg = headLeft;

            // Load (positive) body image
            bodyR2L = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_righttoleft);
            bodyR2L = Bitmap.createScaledBitmap(bodyR2L, bodyFrameWidth, bodyFrameHeight,false);


        } catch (IOException e) {
            // Error
        }

        // Initialize the drawing objects
        surfaceHolder = getHolder();
        paint = new Paint();

        // If you score 200 you are rewarded with a crash achievement!
        snakeXs = new int[200];
        snakeYs = new int[200];

        MyGestureListener mGestureListener = new MyGestureListener();
        mGestureListener.setEngine(this);
        mDetector = new GestureDetectorCompat(context, mGestureListener);

        // Start the game
        newGame();

    }

    @Override
    public void run() {
        while (isPlaying) {
            //Update 10 times a second
            if(updateRequired()) {
                update();
                draw();
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (java.lang.InterruptedException e){
            // Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void newGame() {
        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;

        spawnBob();

        score = 0;

        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnBob() {
        Random random = new Random();
        bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) +1;
        bobY = random.nextInt(numBlocksHigh -1) +1;
    }

    private void eatBob() {
        snakeLength++;
        spawnBob();
        score = score+1;
        soundPool.play(eat_bob, 1, 1, 0, 0, 1);
    }

    private void moveSnake() {
        for (int i = snakeLength; i>0; i--) {
            snakeXs[i] = snakeXs[i-1];
            snakeYs[i] = snakeYs[i-1];
        }

        switch (heading) {
            case UP:
                snakeYs[0]--;
                break;
            case RIGHT:
                snakeXs[0]++;
                break;
            case DOWN:
                snakeYs[0]++;
                break;
            case LEFT:
                snakeXs[0]--;
                break;
        }
    }

    private boolean detectDeath() {
        // Has the snake died?
        boolean dead = false;

        // Hit the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;

    }


    public void update() {
        // Did the head of the snake eat Bob?
        if (snakeXs[0] == bobX && snakeYs[0] == bobY) {
            eatBob();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);

            newGame();
        }
    }

    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            // Fill the screen with Game Code School blue
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Set the color of the paint to draw the snake white
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Scale the HUD text
            paint.setTextSize(90);
            canvas.drawText("Score:" + score, 10, 70, paint);

            // Draw the snake one block at a time
            for (int i = 1; i < snakeLength; i++) {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            //Draw the head
            canvas.drawBitmap(headImg, snakeXs[0]*blockSize, snakeYs[0]*blockSize, paint);

            // Set the color of the paint to draw Bob red
            paint.setColor(Color.argb(255, 255, 0, 0));

            // Draw Bob
            canvas.drawRect(bobX * blockSize,
                    (bobY * blockSize),
                    (bobX * blockSize) + blockSize,
                    (bobY * blockSize) + blockSize,
                    paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {

        // Are we due to update the frame
        if(nextFrameTime <= System.currentTimeMillis()){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            nextFrameTime =System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;
            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

//        if (this.mDetector.onTouchEvent(motionEvent)) {
//            return true;
//        }
//        return super.onTouchEvent(motionEvent);

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch(heading){
                        case UP:
                            heading = Heading.RIGHT;
                            headImg = headRight;
                            break;
                        case RIGHT:
                            heading = Heading.DOWN;
                            headImg = headDown;
                            break;
                        case DOWN:
                            heading = Heading.LEFT;
                            headImg = headLeft;
                            break;
                        case LEFT:
                            heading = Heading.UP;
                            headImg = headUp;
                            break;
                    }
                } else {
                    switch(heading){
                        case UP:
                            heading = Heading.LEFT;
                            headImg = headLeft;
                            break;
                        case LEFT:
                            heading = Heading.DOWN;
                            headImg = headDown;
                            break;
                        case DOWN:
                            heading = Heading.RIGHT;
                            headImg = headRight;
                            break;
                        case RIGHT:
                            heading = Heading.UP;
                            headImg = headUp;
                            break;
                    }
                }
        }
        return true;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        // Minimal x and y axis swipe distance.
        private int MIN_SWIPE_DISTANCE_X = 100;
        private int MIN_SWIPE_DISTANCE_Y = 100;

        // Maximal x and y axis swipe distance.
        private int MAX_SWIPE_DISTANCE_X = 1000;
        private int MAX_SWIPE_DISTANCE_Y = 1000;

        // Source activity that display message in text view.
        private SnakeEngine engine = null;

        SnakeEngine getEngine() {
            return engine;
        }

        void setEngine(SnakeEngine e) {
            this.engine = e;
        }

        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        /* This method is invoked when a swipe gesture happened. */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

//            Log.d(DEBUG_TAG, "onScroll: " + e1.toString() + e2.toString());

            // Get swipe delta value in x axis.
            float deltaX = e1.getX() - e2.getX();

            // Get swipe delta value in y axis.
            float deltaY = e1.getY() - e2.getY();

            // Get absolute value.
            float deltaXAbs = Math.abs(deltaX);
            float deltaYAbs = Math.abs(deltaY);

            Log.d(DEBUG_TAG, "onScroll: " + deltaX + ", " + deltaY);


            // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
            if((deltaXAbs >= MIN_SWIPE_DISTANCE_X) && (deltaXAbs <= MAX_SWIPE_DISTANCE_X))
            {
                if(deltaX > 0)
                {
                    this.engine.heading = Heading.LEFT;
                }else
                {
                    this.engine.heading = Heading.RIGHT;
                }
            }

            if((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y))
            {
                if(deltaY > 0)
                {
                    this.engine.heading = Heading.UP;
                }else
                {
                    this.engine.heading = Heading.DOWN;
                }
            }


            return true;
        }
    }
}
