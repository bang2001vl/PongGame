package thunderstudio.practice.ponggame;

import android.app.Activity;
import android.app.TaskInfo;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;

class PongGame extends SurfaceView implements Runnable{

    ////////// Initalize objects //////////

    // to draw
    private SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas;
    private Paint mPaint;
    // to play game
    private Ball mBall; // Thing we must keep alive
    private Bat mBat; // ball turn back when hit this thing, we move it to keep the ball on screen
    // to update information to draw mostly synchronous
    Thread mGameThread = null;
    private Random mRandom;
    // to play sound track
    private SoundPool mSoudPool;

    ////////// Initialize variables //////////

    // for game clock
    private long gameFPS = 60;
    private final int MILIS_IN_SECOND = 1000; // We will count frame in 1000 clock
    // for screen resolution
    private int screenTotalPixelsX;
    private int screenTotalPixelsY;
    // for text display style
    private int fontSizeDefault;
    private int fontMarginDefault;
    // for game currently situation
    private int gameScrore;
    private int gameLives;
    private boolean isPlaying;
    private boolean isPause;
    private boolean gameOver;
    // for drawing
    private int colorBackground = Color.argb(255,26,128,182); // Solid color
    // for debugging
    private boolean isDebugging = false;
    private final String TAG = "ThunderStudio ";
    // for sound strack
    private int soundHitBatID = -1;
    private int soundMissID = -1;
    private int soundBackGroundID = -1;
    private int soundGameOverID = -1;
    private int soundClickID = -1;


    ////////// Contructor //////////

    public PongGame(Context context, int screenPixelsX, int screenPixelsY)
    {
        super(context); // this is mandatory

        screenTotalPixelsX = screenPixelsX;
        screenTotalPixelsY = screenPixelsY;

        fontSizeDefault = screenTotalPixelsY/20; // We could write 20 line
        fontMarginDefault = fontSizeDefault; //

        mSurfaceHolder = getHolder(); // Notice it, this is new :>
        mPaint = new Paint();

        mBall = new Ball(fontSizeDefault/4);
        mBat = new Bat(screenPixelsX, screenPixelsY);

        mRandom = new Random();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            mSoudPool = new SoundPool.Builder().setMaxStreams(5).setAudioAttributes(audioAttributes).build();
        }
        else
        {
            mSoudPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try
        {
            soundHitBatID = mSoudPool.load(context, R.raw.hit_bat, 0);
            soundMissID = mSoudPool.load(context, R.raw.miss, 1);
            soundBackGroundID = mSoudPool.load(context, R.raw.back_ground, 0);
            // Sound Click
            // Sound Game Over
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR from PongGameContructor while try to get sound track file: " + e.getMessage());
        }

        Log.d(TAG, "In PongGame's Contructor " );
        Log.d(TAG, "Screen In Pixels: X = " + screenTotalPixelsX + ", Y = " + screenTotalPixelsY);
        Log.d(TAG, "Font Size = " + fontSizeDefault + ", Margin = " + fontMarginDefault);

        startNewGame();
    }

    ////////// Override methods //////////
    /*
        This code execute when
        we run a thread with
        parameter is object of this class.
        This code execute by the new thread,
        not by the thread which create this object.
     */
    @Override
    public void run()
    {
        //Random random = new Random();

        // Create a loop
        // which will never stop
        // when game still running
        while(isPlaying)
        {
            long timeStartFrame = System.currentTimeMillis();
            if(!isPause)
            {
                // Update all variables such as ball/bat posion, lives, score,.....
                update();

                detectCollition();
            }

            drawGame();

            long timeForThisFrame = System.currentTimeMillis() - timeStartFrame;

            if(timeForThisFrame > 0)
            {
                gameFPS = MILIS_IN_SECOND  / timeForThisFrame; // It means game FPS = 1/(time for this frame in Second)
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(!isDebugging)
        {
            if (!gameOver)
            {
                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:
                        if (event.getX() < screenTotalPixelsX / 3.0)
                        {
                            mBat.setStateMoving(Bat.STATE_MOVING_LEFT);
                        } else if (event.getX() > screenTotalPixelsX * 2 / 3.0)
                        {
                            mBat.setStateMoving(Bat.STATE_MOVING_RIGHT);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        mBat.setStateMoving(Bat.STATE_MOVING_STOP);
                        break;
                }
            } else
            {
                startNewGame();
            }
        }
        else
        {
            switch (event.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_UP:
                    Log.d(TAG, "Check Rect.Intersects: " + RectF.intersects(
                            new RectF(event.getX(), event.getY(), event.getX() + fontSizeDefault, event.getY() + fontSizeDefault), mBat.getRect()));
                    break;
            }
        }
        return true;
    }

    ////////// Innitialize methods //////////

    /*
        The player has failed last game or started game first time
     */
    private void startNewGame()
    {
        // reset round value
        gameScrore = 0;
        gameLives = 3;

        mBall.reset(mRandom.nextInt(screenTotalPixelsX - 2*fontSizeDefault) + fontSizeDefault, mRandom.nextInt((screenTotalPixelsY - 10*fontSizeDefault)), screenTotalPixelsX);

        mBall.reset(500,500, screenTotalPixelsX);
        isPause = false;
        gameOver = false;

        Log.d(TAG, "Start new game with { Score = " + gameScrore + ", Lives = " + gameLives + ", FPS = " + gameFPS + " }" );
    }

    /*
        Draw HUD(score, lives), ball and bat
     */
    private void drawGame()
    {
        // Log.d(TAG, "Drawing !");

        // Setting Canvas
        if(mSurfaceHolder.getSurface().isValid())
        {
            mCanvas = mSurfaceHolder.lockCanvas();

            if(gameOver)
            {
                mCanvas.drawColor(Color.RED);
                mPaint.setColor(Color.WHITE);

                mPaint.setTextSize(fontSizeDefault*3);
                float lenght = mPaint.measureText("Game Over !");
                mCanvas.drawText("Game Over !", (screenTotalPixelsX-lenght)/2, screenTotalPixelsY/2 - fontMarginDefault*3, mPaint);

                mPaint.setTextSize(fontSizeDefault);
                lenght = mPaint.measureText("Tap to play again :>");
                mCanvas.drawText("Tap to play again :>", (screenTotalPixelsX-lenght)/2, screenTotalPixelsY/2, mPaint);

                mPaint.setTextSize(fontSizeDefault*3);
                lenght = mPaint.measureText("Your Score: " + gameScrore);
                mCanvas.drawText("Your Score: " + gameScrore, (screenTotalPixelsX-lenght)/2, screenTotalPixelsY/2 - fontMarginDefault*4, mPaint);
            }

            else
            {
                // clear screen
                mCanvas.drawColor(colorBackground);

                // Draw HUD
                mPaint.setColor(Color.BLACK);
                mPaint.setTextSize(fontSizeDefault * 2);
                mCanvas.drawText("Score = " + gameScrore + ", Lives = " + gameLives, fontMarginDefault, fontSizeDefault * 2, mPaint);

                // Draw debugging runtime value
                if (isDebugging)
                {
                    printDebugInformation();
                }

                // Draw ball and bat
                mPaint.setColor(Color.WHITE);
                mCanvas.drawRect(mBall.getRectF(), mPaint);
                mCanvas.drawRect(mBat.getRect(), mPaint);
            }
            // Configured Canvas
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /*
        Update ball/bat position, score, lives
     */
    private void update()
    {
        mBall.update(gameFPS);
        mBat.update(gameFPS);
    }

    /*
        Detect the collision
        Has ball hit the bat
        Has ball his the edge of the screenm
     */
    private byte detectCollition()
    {
        if(mBall.checkHitBat(mBat.getRect(), gameFPS))
        {
            mBall.speedUp(mRandom.nextFloat()*30, mRandom.nextFloat()*30);
            gameScrore++;

            // Play sound
            mSoudPool.play(soundHitBatID, 1.0f, 1.0f, 5, 0, 1.0f);

            while (mBall.getRectF().bottom >= mBat.getRect().top)
            {
                mBall.update(gameFPS);
            }
        }
        else
        {
            boolean ballInScreen = true;

            if (mBall.getRectF().bottom > screenTotalPixelsY)
            {
                gameLives--;
                Log.d(TAG, "Ball: ball missed bat. Position: " + mBall.getRectF().toShortString());
                if (gameLives == 0)
                {
                    // Game over
                    gameOver = true;
                    isPause = true;
                    //pause();
                    //drawGame();

                    return -1;
                } else // Game not end yet
                {
                    mBall.reverseSideYtoTop();

                    if ((mBall.getRectF().right > screenTotalPixelsX))
                    {
                        mBall.reverseSideXtoLeft();
                        while (mBall.getRectF().bottom >= screenTotalPixelsY )
                        {
                            mBall.update(gameFPS);
                        }
                        mBall.reverseSideXtoRight();
                        // Now it go top-right from in-screen position
                    }
                    else
                    {
                        if (mBall.getRectF().left < 0)
                        {
                            mBall.reverseSideXtoRight();
                            while (mBall.getRectF().bottom >= screenTotalPixelsY)
                            {
                                mBall.update(gameFPS);
                            }
                            mBall.reverseSideXtoLeft();
                            // now it go top-left from in-screen position
                        }
                    }
                }

            }
            else
            {
                if ((mBall.getRectF().right > screenTotalPixelsX))
                {
                    mBall.reverseSideXtoLeft();
                }
                else if (mBall.getRectF().left < 0)
                {
                    mBall.reverseSideXtoRight();
                }
                if (mBall.getRectF().top < 0)
                {
                    mBall.reverseSideYtoBottom();
                    //Log.d(TAG, "Ball: hit top, has set ReverseSideYtoBottom");
                }
            }
            /*while (((mBall.getRectF().bottom > screenTotalPixelsY) || (mBall.getRectF().right > screenTotalPixelsX)
                    || (mBall.getRectF().left < 0) || (mBall.getRectF().top < 0)))
            {
                mBall.update(gameFPS);
            }*/


        }
        return 0;
    }

    /*
        Print addtional debugging information
     */
    private void printDebugInformation()
    {
        // Make sure that Canvas has been setting up
        mPaint.setTextSize(fontSizeDefault);
        mCanvas.drawText("FPS: " + gameFPS + ", Ball: X=" + mBall.getRectF().left + ", Y= " + mBall.getRectF().top, fontMarginDefault, fontSizeDefault*3, mPaint);

    }

    /*
        This code should excute by PongActivity
        when player quit game
     */
    public void pause()
    {
        // No playing anymore
        isPlaying = false;

        try
        {
            mGameThread.join();
            Log.d(TAG, "Stopped game thread");
        }
        catch (InterruptedException e)
        {
            Log.d(TAG + ": ERROR :", "Cannot join thread");
        }
    }

    /*
        This code should excute by PongActivity
        when player start a game
     */
    public void resume()
    {
        // Player has come back
        isPlaying = true;

        mGameThread = new Thread(this);

        mGameThread.start();
        Log.d(TAG, "Started game thread");

    }

    public void quit()
    {
        pause();
    }
}
