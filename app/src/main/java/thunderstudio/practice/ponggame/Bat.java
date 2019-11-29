package thunderstudio.practice.ponggame;

import android.graphics.RectF;

class Bat {

    public final static int STATE_MOVING_LEFT = -1;
    final static int STATE_MOVING_RIGHT = 1;
    final static int STATE_MOVING_STOP = 0;

    private RectF mRectF;

    private float lenght;
    private float coorX;


    private int screenPixelsX;
    private int stateMoving;

    private float speedX ; // In pixels per second

    ////////// Contructor //////////
    Bat(int screenPixelsX, int screenPixelsY)
    {
        this.screenPixelsX = screenPixelsX;
        lenght = screenPixelsX/8;
        coorX = screenPixelsX/2;
        float height = screenPixelsY/40;

        mRectF = new RectF(coorX, screenPixelsY - height, coorX + lenght, screenPixelsY);

        speedX = screenPixelsX; // move all width in 1 second
    }

    Bat(int screenPixelsX, int screenPixelsY, int width, int height, float speedToX)
    {

    }

    ////////// Initialize methods //////////

    RectF getRect()
    {
        return mRectF;
    }

    public void setStateMoving(int state_moving)
    {
        stateMoving = state_moving;
    }

    public void update(long currentFPS)
    {
        if(stateMoving==STATE_MOVING_RIGHT)
        {
            mRectF.left = mRectF.left + speedX/currentFPS;
        }
        else if(stateMoving == STATE_MOVING_LEFT)
        {
            mRectF.left = mRectF.left - speedX/currentFPS;
        }

        if(mRectF.left<0)
        {
            mRectF.left = 0;
        }
        else if(mRectF.left > (screenPixelsX - lenght))
        {
            mRectF.left = screenPixelsX - lenght;
        }

        mRectF.right = mRectF.left + lenght;
    }
}
