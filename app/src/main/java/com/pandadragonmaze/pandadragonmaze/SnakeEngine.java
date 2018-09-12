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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
    private ArrayList<Level> levels;

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
    Bitmap headUp;
    Bitmap headDown;
    Bitmap headLeft;
    Bitmap headRight;

    // Current head image
    private Bitmap headImg;


    // Images for Dragon's body
    Bitmap bodyR2L;
    Bitmap bodyR2U;
    Bitmap bodyR2D;
    Bitmap bodyL2R;
    Bitmap bodyL2D;
    Bitmap bodyL2U;
    Bitmap bodyU2D;
    Bitmap bodyU2L;
    Bitmap bodyU2R;
    Bitmap bodyD2U;
    Bitmap bodyD2L;
    Bitmap bodyD2R;
    Bitmap tailD;
    Bitmap tailL;
    Bitmap tailR;
    Bitmap tailU;


    int headFrameHeight;
    int headFrameWidth;
    int bodyFrameHeight;
    int bodyFrameWidth;
    int pandaFrameHeight;
    int pandaFrameWidth;
    int goalFrameHeight;
    int goalFrameWidth;
    int wallFrameHeight;
    int wallFrameWidth;

    Bitmap wall_9slice_bc;
    Bitmap wall_9slice_mc;
    Bitmap wall_9slice_tl;
    Bitmap wall_block_center;
    Bitmap wall_stright_vertical;
    Bitmap wall_9slice_bl;
    Bitmap wall_9slice_ml;
    Bitmap wall_9slice_tr;
    Bitmap wall_block_left;
    Bitmap wall_9slice_bm;
    Bitmap wall_9slice_mr;
    Bitmap wall_block;
    Bitmap wall_block_right;
    Bitmap wall_9slice_br;
    Bitmap wall_9slice_tc;
    Bitmap wall_block_2x2;
    Bitmap wall_stright_horizontal;

    Bitmap panda;
    Bitmap goal;


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
        wallFrameWidth = blockWidth;
        wallFrameHeight = (int) (1.6 * (float) blockHeight);
        pandaFrameHeight = (int) (1.5 * (float) blockHeight);
        pandaFrameWidth = (int) (0.75 *  (float) blockWidth);
        goalFrameHeight = (int) (2.4 * (float) blockHeight);
        goalFrameWidth = blockWidth;

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

            // Pandas
            panda = BitmapFactory.decodeResource(this.getResources(), R.drawable.panda);
            panda = Bitmap.createScaledBitmap(panda, pandaFrameWidth, pandaFrameHeight,false);

            // Goal
            goal = BitmapFactory.decodeResource(this.getResources(), R.drawable.goal_1);
            goal = Bitmap.createScaledBitmap(goal, goalFrameWidth, goalFrameHeight,false);


            // Load Wall images
            wall_9slice_bc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bc);
            wall_9slice_bc = Bitmap.createScaledBitmap(wall_9slice_bc, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_mc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_mc);
            wall_9slice_mc = Bitmap.createScaledBitmap(wall_9slice_mc, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_tl = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tl);
            wall_9slice_tl = Bitmap.createScaledBitmap(wall_9slice_tl, wallFrameWidth, wallFrameHeight,false);
            wall_block_center = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_center);
            wall_block_center = Bitmap.createScaledBitmap(wall_block_center, wallFrameWidth, wallFrameHeight,false);
            wall_stright_vertical = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_stright_vertical);
            wall_stright_vertical = Bitmap.createScaledBitmap(wall_stright_vertical, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_bl = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bl);
            wall_9slice_bl = Bitmap.createScaledBitmap(wall_9slice_bl, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_ml = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_ml);
            wall_9slice_ml = Bitmap.createScaledBitmap(wall_9slice_ml, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_tr = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tr);
            wall_9slice_tr = Bitmap.createScaledBitmap(wall_9slice_tr, wallFrameWidth, wallFrameHeight,false);
            wall_block_left = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_left);
            wall_block_left = Bitmap.createScaledBitmap(wall_block_left, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_bm);
            wall_9slice_bm = Bitmap.createScaledBitmap(wall_9slice_bm, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_mr = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_mr);
            wall_9slice_mr = Bitmap.createScaledBitmap(wall_9slice_mr, wallFrameWidth, wallFrameHeight,false);
            wall_block = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block);
            wall_block = Bitmap.createScaledBitmap(wall_block, wallFrameWidth, wallFrameHeight,false);
            wall_block_right = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_right);
            wall_block_right = Bitmap.createScaledBitmap(wall_block_right, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_br = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_br);
            wall_9slice_br = Bitmap.createScaledBitmap(wall_9slice_br, wallFrameWidth, wallFrameHeight,false);
            wall_9slice_tc = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_9slice_tc);
            wall_9slice_tc = Bitmap.createScaledBitmap(wall_9slice_tc, wallFrameWidth, wallFrameHeight,false);
            wall_block_2x2 = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_block_2x2);
            wall_block_2x2 = Bitmap.createScaledBitmap(wall_block_2x2, wallFrameWidth, wallFrameHeight,false);
            wall_stright_horizontal = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall_stright_horizontal);
            wall_stright_horizontal = Bitmap.createScaledBitmap(wall_stright_horizontal, wallFrameWidth, wallFrameHeight,false);


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

        initLevels();

        // Start the game
        newGame();

    }

    private void initLevels() {
        levels = new ArrayList<Level>();

        //0
        levels.add(new Level("20f0e00010101010101010101010101010101040000000000000000000000010100000000000000000000000001010000000000010000000000000101000000000001000000000000010100000000000100000000000003010100000002000100020000000003010100000000000100000000000003010100000000000100000000000001010000000000010000000000000101000000000001000000000000010100000000000100000000000001010000000000000000000000000101000000000000000000000000010101010101010101010101010101", "Incessantly Growing Dragon awaits your command. \"Save all the pandas,\" IGD implores \"or none will be saved\"", "\"This commander is capable,\" IGD muses, \"but the path before us is seems long and filled with painful walls. Will this commander be up to the task?\"" ));
        //1
        levels.add(new Level("20f0e000101010101010101010101010101010400000000000000000000000101000000000000000000000000010100000101010000010101000001010000000000000000000000000103000001000000000000000001000302030000010000000200000000010003020300000100000000000000000100030201000000000000000000000000010100000000000000000000000001010000010101000001010100000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101010101010103080308010101010101"));
        //2
        levels.add(new Level("20f0e000101010101010101010101010101010400000000000000000000000101000000000000000000000000010100010101000101000101010001010001000000000000000001000103010001000000000000000001000103010001000002000000020001000103010001000000000000000001000101000100000000000000000100010100010101000101000101010001010000000000000000000000000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101010101010103090309010101010101"));
        //3
        levels.add(new Level("20f0e0001010101010101010101010101010104000100000000000100000001010000010001010100010001000101000000000002000000020100010100000100010101000000010001010000010001000000010000000101000201000102000001010101010100000100000001000000000003040100000100000001000101010001010000010001010100000000000101000101000002000001000100010100000000010101000100010201010101000001000100010001000101000000000100010000000000010101030a0101010101010101010101",  null, "\"Swift fingers, commander!\""));
        //4
        //levels.add(new Level("20f0e0001010101010101010101010101010104000000000000000000000001010000000000000000000000000101000000000000000000000000010102020000010100000100000201010200020001000100020202010101020002000100010001000002010303020200000100010002000001010102000000010001000100000201010200000001000100020000010101020000000101000001000002010100000000000000000000000001010000000000000000000000000101000000000000000000000000010101010101030b030b030b010101010101"));
        levels.add(new Level("20f0e0001010101010101010101010101010104000000000000000000000001010000000000000000000000000101000000000000000000000000010100000100020102000100000001010000020000000100020000000101000001000100010001000000010303000002000102010002000000010100000100010001000100000001010000020001000000020000000101000001000201020001000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010101010101030b030b030b010101010101"));

        //5
        levels.add(new Level("20f0e000101010101010101010101010101010000000000000000000000000101000101010201010102010100010100010000000000000000000001010001020000000000000000000101000100000000000000010101010100010001020101010201000003060100010000000000000001000001010001010101010100000100000101000000000000010000010002010100000101010001000101000101010000000201000100020100020101000000000100010000010000010100000000010401000000000001010101010101030c01010101010101"));

        //6
        levels.add(new Level("20f0e0001010101010101010101010101010100000000000000000000000001010000000201010101010200000101000000010000000000010000010100000001000200020001000001010000000100000000000100000103050000000100000000000100000101000000000100010001000000010100000000020002000200000001010000000102000200020100000101000000000100010001000000010100000000000000000000000001010400000000000000000000000101000000000000000000000000010101010101030d01010101030d010101", "\"I hear a whisper in the wind...no, I must concentrate on the Pandas first!\"", "\"The wind, it tells me that in a column of 3, the 1st will be false...how strange.\""));
        //7
        levels.add(new Level("20f0e0001010101010101010101010101010100000000000000000000000001010000000000000000000000000101000100000001000000010000010100020001000200010002000001010000000000000000000000000101000100000001000000010000010100000001000000010000000001010000000200000002000000000101000100000001000000010000010100000001000000010000000001010000000000000000000000000101000000000000040000000000010100000000000000000000000001010101010101030e030e010101010101"));
        //8
        levels.add(new Level("20f0e000101010101010301030101010101010101040000000000000000000000010100000000000000000000000001010000000101010101010000000101000000010200000201000000010100000001000000000100000001010000000100000000010000000101000000010100000101000000010100000000000000000000000001010001000000000000000001000101000000000000000000000000010100000000000202000000000001010000000000000000000000000101000000000000000000000000010101010101010101010101010101"));
        //9
        //levels.add(new Level("20f0e0001010101010103020302010101010101010000000000040000000000000101000000000000000000000000010100000001010101010100000001010000000000000000010000000101000000010201010101000000010100000001000000000100000001010000000101010201010000000101000000010000000001000000010100000001010201010100000001010000000000000000000000000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101010101010103100310010101010101", "\"I doubt my commander is so nimble. Perhaps we should leave these Pandas to starve?\"", "\"Impressive! The soil will have far fewer Panda carcasas to feed it than I thought!\""));
        levels.add(new Level("20f0e0001010101010103020302010101010101010000000000040000000000000101000000000000000000000000010100000001010101010100000001010000000000000000010000000101000000010201010101000000010100000001000000000100000001010000000101010201010000000101000000010000000001000000010100000001010201010100000001010000000000000000000000000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101010101010103100310010101010101"));
        //10
        levels.add(new Level("20f0e000101030301010101010101010101010104000000000000000000000001010001000000010000000100000101000000010002000100000000010100000000000000000000000001010101010101010101010101000101000000000000000000000000010100000000000000000000000001010001010101010101010100000101000000000000000000010000010100000000000000000001020001010101010101010101000101010101020000000000000000000000010100000000000000000000000001010101010101010101010103110101"));
        //11
        levels.add(new Level("20f0e0001010101010304030403040101010101010104000000000000000000000001010000000000000000000000000101000100000000000000010000010100010000000000000001000001010001000000020000000100000101020100000101010000010200030c01000100000002000000010000010100010000000000000001000001010001000000000000000100000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010101010101031203120312010101010101"));
        //12
        levels.add(new Level("20f0e000101010101010305010101010101010100000000000000000000000001010000000000000000000000000101000000000000000000000000030d01000000000000000000010000010100000000000000000002000001030b04000000000000000001000001010000000000000002000200000101000000000000000100010000010100000000000200020002000001010000000000010001000100000101000000000002000200020000030d010001020102010201020100000101000000000000000000000000010101010101010101010101010101", "\"Impossible. Only the wind can move in such ways...\"", "\"Masterfully done, commander!\""));
        //13
        levels.add(new Level("20f0e000101010101030601010101030601010101040000010001020201000100010100000001000100000100010001030c000000010000000000000100010100000001010100010101010001010000020000000000000000000101000100010101000102010000010100020000000000000000000001010001000102010001010102000101000002000000000000000000010100010101000102010001000001030c0000000000000000020000000101000102010001010100010000010100000000000000000000000001010101010101010314010101010101"));
        //14
        levels.add(new Level("20f0e00010101010101030703070101010101010100000000000000000000000001010001000000000000000001000101000001020000000002010000010100000001000000000100000001010000000001020001000000000101000000000001010200000004030f01000000000201010000000000030f0100000000010002010000000001010000020100000000010200000101000001000000000000010000010100010000000000000000010001010000000000000000000000000101000000000000000000000000010101010101010101010101010101", null, "\"Most excellent maneuvering!\" IGD gushes, \"Truly, someone should write a poem or something about my commander!\""));
        //15
        levels.add(new Level("20f0e00010101010101010101010101010101000000000000000000000000010100000000000000000000000001010000010001000001020100000101000002000000000000000000010100000100010000010001000001030e0000000000000400000000000310030e00000000000000000000000003100100000000000000000000000001010000010001000001000100000101000000000000000000020000010100000102010000010001000001010000000000000000000000000101000000000000000000000000010101010101010101010101010101"));
        //16
        levels.add(new Level("20f0e000101010101010309030901010101010101000000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010100000000000000000000000001030f0000000001000100000000000311030f00000000010201000000000003110100000000010401000000000001010000000001010100000000000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010101010101010101010101010101", null, "\"This may not be the last time we see such illusions...\""));
        //levels.add(new Level("20f0e000101010101010309030901010101010101000000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010100000000000000000000000001030f0000000001000100000000000311030f00000000010201000000000003110100000000010401000000000001010000000001010100000000000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010101010101010101010101010101" ));
        //17
        levels.add(new Level("20f0e000101010101010101010101030a0101010000000000000000010000000101000000000000000001000000010100000000000100000100000001010204000002010200010200000101010101000001000101010100010310000001000001000001020000010310000001000001000001000000010100000100000100000100000001010000010000010002010000000101000001000001000101000000010100000100000100000200000001010000000000010000000000000101000000000001000000000000010101010101010101010101010101"));
        //18
        levels.add(new Level("20f0e000101010101030b030b030b010101010101010000020100000401020000000101000100010000000100010100010100010001010101010001010001010001000000000000000000000101000101010101010101000000010100000000000000000100000001010101010101010100010001000101020001000003630100010001000313010000010001010100010001000101000001000000000001000100010100000101010101010100010001010000000000000000000000000101000000000000000000000000010101010101010101010101010101", "\"The portal out of this dungeon is here, but shall we leave without all Pandas?\""));
        //19
        levels.add(new Level("20f0e000101010101010101010101010101010000000000000000000000000101000000000101000000000000010100000000010200000101000001010001010000000000010200000101000102000001010000000101010100000000000102000000010003140100000101000000000000010003140312000001020000000000000100031401000000000000010100000101010100000001010001020000000001010000000102000000000000000101000000000000000000000004010100000000000000000000000003140101010101010101010101010101"));
        //20
        levels.add(new Level("20f0e0001010101010101030d0101010101010100000000000004000000000001010000000000000000000000000101000102020201020202010000010100020201020102010202000001010002020002010202020200000103130001020202010202020100000103130002020102020201020200000103130002020202020202020200000101000102020201020202010000010100020201020202010202000001010000000000000000000000000101000000000000000000000000010313000000000000000000000000010101010101010101010101010101", "\"Finally, a worthy challenge for my commander!\"", "\"Grandmaster! I am forever your dragon to command!\""));
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
        if (isPlaying) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void newGame() {

//            // create toy maze for testing
//            maze = new Maze(this);
//            maze.addPoint(new WallPoint(5,5, wall_block, this));
//            maze.addPoint(new PandaPoint(2,15, panda, this));
//            maze.addPoint(new GoalPoint(13,26, goal, this, 2));


        loadLevel(0);

        // Initialize heading
        prevHeading = Heading.START;

        spawnBob();

        score = 0;

        nextFrameTime = System.currentTimeMillis();
        isPlaying = true;
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
        try {
            maze = Maze.load(levels.get(level).getOriginalMazeString(), this);
////                    "20f0e00010101010101010101010101010101040000000000000000000000010100000000000000000000000001010000000000000000000000000101000000000000000000000000010100000000000000000000000003010100000000000000000000000003010100000000000000000000000003010100010101010100000000000001010001000000010000000000000101000100000001000000000000010100000000000000000000000001010001000000010000000000000101000100000001000000000000010101010101010101010101010101",
//                    "20f0e000101010101010101010101010101010400000000000000000000000101000000000000000000000000010100000101010000010101000001010000000000000000000000000103000001000000000000000001000302030000010000000200000000010003020300000100000000000000000100030201000000000000000000000000010100000000000000000000000001010000010101000001010100000101000000000000000000000000010100000000000000000000000001010000000000000000000000000101010101010103080308010101010101",
//                    this);
            Pair start = maze.getStart();
            dragonRow = ((Integer) start.first).intValue();
            dragonCol = ((Integer) start.second).intValue();
        } catch (Exception e) {

            System.out.println(e.toString());
            System.exit(1);
        }

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
