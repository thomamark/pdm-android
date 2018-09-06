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
    private boolean isStart = true;

    // The current maze
    private Maze maze;

    // For tracking movement Heading
    public enum Heading {UP, RIGHT, DOWN, LEFT, START}

    // Variables for the Dragon's current position
    private Heading heading = Heading.RIGHT;
    private int dragonRow;
    private int dragonCol;


    // Store the previous heading & position for image rendering
    private Heading prevHeading = Heading.START;
//    private int prevRow;
//    private int prevCol;

    // Images for the Dragon's head
    private Bitmap headUp;
    private Bitmap headDown;
    private Bitmap headLeft;
    private Bitmap headRight;

    // Current head image
    private Bitmap headImg;


    // Images for Dragon's body
    private Bitmap bodyR2L;
    private Bitmap bodyR2U;
    private Bitmap bodyR2D;
    private Bitmap bodyL2R;
    private Bitmap bodyL2D;
    private Bitmap bodyL2U;
    private Bitmap bodyU2D;
    private Bitmap bodyU2L;
    private Bitmap bodyU2R;
    private Bitmap bodyD2U;
    private Bitmap bodyD2L;
    private Bitmap bodyD2R;
    private Bitmap tailD;
    private Bitmap tailL;
    private Bitmap tailR;
    private Bitmap tailU;


    private int headFrameHeight;
    private int headFrameWidth;
    private int bodyFrameHeight;
    private int bodyFrameWidth;

    private Bitmap wall_9slice_bc;
    private Bitmap wall_9slice_mc;
    private Bitmap wall_9slice_tl;
    private Bitmap wall_block_center;
    private Bitmap wall_stright_vertical;
    private Bitmap wall_9slice_bl;
    private Bitmap wall_9slice_ml;
    private Bitmap wall_9slice_tr;
    private Bitmap wall_block_left;
    private Bitmap wall_9slice_bm;
    private Bitmap wall_9slice_mr;
    private Bitmap wall_block;
    private Bitmap wall_block_right;
    private Bitmap wall_9slice_br;
    private Bitmap wall_9slice_tc;
    private Bitmap wall_block_2x2;
    private Bitmap wall_stright_horizontal;

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

        // Blocks are half as tall as they are wide
//        numBlocksHigh = screenY / blockHeight;
        numBlocksHigh = NUM_BLOCKS_WIDE / 2;
        //TODO: differentiate between block width and height

        headFrameHeight = blockSize * SPRITE_SCALE;
        headFrameWidth = blockSize * SPRITE_SCALE;
        blockWidth = bodyFrameWidth = blockSize * SPRITE_SCALE;
        blockHeight = bodyFrameHeight = blockSize / 2 * SPRITE_SCALE;

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
            headImg = headRight;

            // Load (positive) body image
            bodyR2L = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_righttoleft);
            bodyR2L = Bitmap.createScaledBitmap(bodyR2L, bodyFrameWidth, bodyFrameHeight,false);
            bodyR2D = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_righttodown);
            bodyR2D = Bitmap.createScaledBitmap(bodyR2D, bodyFrameWidth, bodyFrameHeight,false);
            bodyR2U = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_righttoup);
            bodyR2U = Bitmap.createScaledBitmap(bodyR2U, bodyFrameWidth, bodyFrameHeight,false);
            bodyL2R = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_lefttoright);
            bodyL2R = Bitmap.createScaledBitmap(bodyL2R, bodyFrameWidth, bodyFrameHeight,false);
            bodyL2D = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_lefttodown);
            bodyL2D = Bitmap.createScaledBitmap(bodyL2D, bodyFrameWidth, bodyFrameHeight,false);
            bodyL2U = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_lefttoup);
            bodyL2U = Bitmap.createScaledBitmap(bodyL2U, bodyFrameWidth, bodyFrameHeight,false);
            bodyU2D = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_uptodown);
            bodyU2D = Bitmap.createScaledBitmap(bodyU2D, bodyFrameWidth, bodyFrameHeight,false);
            bodyU2L = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_uptoleft);
            bodyU2L = Bitmap.createScaledBitmap(bodyU2L, bodyFrameWidth, bodyFrameHeight,false);
            bodyU2R = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_uptoright);
            bodyU2R = Bitmap.createScaledBitmap(bodyU2R, bodyFrameWidth, bodyFrameHeight,false);
            bodyD2U = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_downtoup);
            bodyD2U = Bitmap.createScaledBitmap(bodyD2U, bodyFrameWidth, bodyFrameHeight,false);
            bodyD2L = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_downtoleft);
            bodyD2L = Bitmap.createScaledBitmap(bodyD2L, bodyFrameWidth, bodyFrameHeight,false);
            bodyD2R = BitmapFactory.decodeResource(this.getResources(), R.drawable.body_downtoright);
            bodyD2R = Bitmap.createScaledBitmap(bodyD2R, bodyFrameWidth, bodyFrameHeight,false);
            tailD = BitmapFactory.decodeResource(this.getResources(), R.drawable.tail_down);
            tailD = Bitmap.createScaledBitmap(tailD, bodyFrameWidth, bodyFrameHeight,false);
            tailL = BitmapFactory.decodeResource(this.getResources(), R.drawable.tail_left);
            tailL = Bitmap.createScaledBitmap(tailL, bodyFrameWidth, bodyFrameHeight,false);
            tailR = BitmapFactory.decodeResource(this.getResources(), R.drawable.tail_right);
            tailR = Bitmap.createScaledBitmap(tailR, bodyFrameWidth, bodyFrameHeight,false);
            tailU = BitmapFactory.decodeResource(this.getResources(), R.drawable.tail_up);
            tailU = Bitmap.createScaledBitmap(tailU, bodyFrameWidth, bodyFrameHeight,false);


            // Load Wall images
            wall_9slice_bc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bc);
            wall_9slice_bc = Bitmap.createScaledBitmap(wall_9slice_bc, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_mc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_mc);
            wall_9slice_mc = Bitmap.createScaledBitmap(wall_9slice_mc, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_tl = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tl);
            wall_9slice_tl = Bitmap.createScaledBitmap(wall_9slice_tl, bodyFrameWidth, bodyFrameHeight,false);
            wall_block_center = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_center);
            wall_block_center = Bitmap.createScaledBitmap(wall_block_center, bodyFrameWidth, bodyFrameHeight,false);
            wall_stright_vertical = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_stright_vertical);
            wall_stright_vertical = Bitmap.createScaledBitmap(wall_stright_vertical, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_bl = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bl);
            wall_9slice_bl = Bitmap.createScaledBitmap(wall_9slice_bl, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_ml = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_ml);
            wall_9slice_ml = Bitmap.createScaledBitmap(wall_9slice_ml, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_tr = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tr);
            wall_9slice_tr = Bitmap.createScaledBitmap(wall_9slice_tr, bodyFrameWidth, bodyFrameHeight,false);
            wall_block_left = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_left);
            wall_block_left = Bitmap.createScaledBitmap(wall_block_left, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bm);
            wall_9slice_bm = Bitmap.createScaledBitmap(wall_9slice_bm, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_mr = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_mr);
            wall_9slice_mr = Bitmap.createScaledBitmap(wall_9slice_mr, bodyFrameWidth, bodyFrameHeight,false);
            wall_block = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block);
            wall_block = Bitmap.createScaledBitmap(wall_block, bodyFrameWidth, bodyFrameHeight,false);
            wall_block_right = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_right);
            wall_block_right = Bitmap.createScaledBitmap(wall_block_right, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_br = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_br);
            wall_9slice_br = Bitmap.createScaledBitmap(wall_9slice_br, bodyFrameWidth, bodyFrameHeight,false);
            wall_9slice_tc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tc);
            wall_9slice_tc = Bitmap.createScaledBitmap(wall_9slice_tc, bodyFrameWidth, bodyFrameHeight,false);
            wall_block_2x2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_2x2);
            wall_block_2x2 = Bitmap.createScaledBitmap(wall_block_2x2, bodyFrameWidth, bodyFrameHeight,false);
            wall_stright_horizontal = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_stright_horizontal);
            wall_stright_horizontal = Bitmap.createScaledBitmap(wall_stright_horizontal, bodyFrameWidth, bodyFrameHeight,false);


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

        // create maze
        maze = new Maze(this);
        maze.addPoint(new WallPoint(5,5, wall_block, this));
        prevHeading = Heading.START;

        // load points
        // set dragon start point and direction

        dragonRow = numBlocksHigh /2;
        dragonCol = NUM_BLOCKS_WIDE / 2;

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

    private void addDragonBody(int prevRow, int prevCol, Heading prevHeading) {
        // Add the snake body to the Maze
        switch (prevHeading) {
            case START:
                maze.addPoint(new DragonPoint(prevRow, prevCol, getTailImg(heading), this));
                break;
            case LEFT:
                switch (heading) {
                    case LEFT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyR2L, this));
                        break;
                    case UP:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyR2U, this));
                        break;
                    case DOWN:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyR2D, this));
                        break;
                }
                break;
            case RIGHT:
                switch (heading) {
                    case RIGHT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyL2R, this));
                        break;
                    case UP:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyL2U, this));
                        break;
                    case DOWN:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyL2D, this));
                        break;
                }
                break;

            case UP:
                switch (heading) {
                    case RIGHT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyU2R, this));
                        break;
                    case LEFT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyU2L, this));
                        break;
                    case UP:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyD2U, this));
                        break;
                }
                break;

            case DOWN:
                switch (heading) {
                    case RIGHT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyD2R, this));
                        break;
                    case LEFT:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyD2L, this));
                        break;
                    case DOWN:
                        maze.addPoint(new DragonPoint(prevRow, prevCol, bodyU2D, this));
                        break;
                }
                break;
            default:
                // Should never get here, so throw in a bad image to make it obvious
                maze.addPoint(new DragonPoint(prevRow, prevCol, headDown, this));
        }
    }

    private void moveDragon() {

        // save the prior heading for future rendering
        this.prevHeading = heading;

        switch (heading) {
            case UP:
                dragonRow--;
                break;
            case RIGHT:
                dragonCol++;
                break;
            case DOWN:
                dragonRow++;
                break;
            case LEFT:
                dragonCol--;
                break;
        }
    }

    private boolean detectDeath() {
        // Has the snake died?
        boolean dead = false;

        // Hit the screen edge
        if (dragonCol == -1) dead = true;
        if (dragonCol >= NUM_BLOCKS_WIDE) dead = true;
        if (dragonRow == -1) dead = true;
        if (dragonRow >= numBlocksHigh) dead = true;

        return dead;

    }

    public void loadLevel(int level) {
        // TODO: add code to load a new level
    }

    public void pickUp(PandaPoint p) {
        // TODO: add code to pick up a panda
    }

    public void die() {
        soundPool.play(snake_crash, 1, 1, 0, 0, 1);

        newGame();
    }

    public void update() {
        // Did the head of the snake eat Bob?
        if (dragonCol == bobX && dragonRow == bobY) {
            eatBob();

        }

        addDragonBody(dragonRow, dragonCol, prevHeading);

        moveDragon();

        //TODO: remove when wall boundaries are put in place
        if (detectDeath()) {
            //start again
            soundPool.play(snake_crash, 1, 1, 0, 0, 1);
            newGame();
        }

        maze.isOn(dragonRow, dragonCol);

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

            //Draw the head
            canvas.drawBitmap(headImg, dragonCol*blockWidth, dragonRow*blockHeight, paint);

            // Draw the full maze every time
            // TODO: Optimize to only draw the moving parts
            Bitmap b = null;
            for (int r = 0; r < maze.getHeight(); r++) {
                for (int c = 0; c < maze.getWidth(); c++) {
                    b = maze.getBitMap(r, c);
                    if (b != null) {
                        canvas.drawBitmap(b, c*blockWidth, r*blockHeight, paint);
                    }
                }
            }

            // Set the color of the paint to draw Bob red
            paint.setColor(Color.argb(255, 255, 0, 0));

            // Draw Bob
            canvas.drawRect(bobX * blockWidth,
                    (bobY * blockHeight),
                    (bobX * blockWidth) + blockWidth,
                    (bobY * blockHeight) + blockHeight,
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

        // Save the prior heading before updating (used for proper rendering)
        // prevHeading = this.heading;

        // uncomment for joystick style controls
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

    /**
     * getTailImg
     * h = a Heading
     * @return proper tail image
     */

    public Bitmap getTailImg(Heading h) {
        switch (h) {
            case LEFT:
                return tailR;
            case RIGHT:
                return tailL;
            case UP:
                return tailU;
            case DOWN:
                return tailD;
            default:
                // should never get here, so put in a wonky image
                return headDown;
        }

    }


    /**
     * The sub class enables joystick-style controls
     */

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
