package thunderstudio.practice.ponggame;

import android.graphics.RectF;
import android.util.Log;

class Ball {

    private RectF mRectF;

    private int size; // Width = Height = Size (It's a square)



    private float speedX ; // In pixels per second
    private float speedY ; // In pixels per second
    private int speedSideX = 1;
    private int speedSideY = 1;

    ////////// Contructor //////////
    public Ball(int size)
    {
        this.size = size;
        mRectF = new RectF(100, 100, 100 +size, 100 +size);
    }

    /*
        Create a ball at particular location and speed
     */
    public Ball(int size, int x, int y, float speedToX, float speedToY)
    {
        this.size = size;
        speedX = speedToX;
        speedY = speedToY;
        mRectF = new RectF(x, y, x+size, y+size);
    }

    ////////// Initialize methods //////////
    /*
        The object will be auto-updated
        once time per fragment
        by updating-thread
     */
    public void update(long currentFPS)
    {
        mRectF.left = mRectF.left + speedSideX*speedX/currentFPS;
        mRectF.top = mRectF.top + speedSideY*speedY/currentFPS;

        mRectF.right = mRectF.left + size;
        mRectF.bottom = mRectF.top + size;
    }

    public void reset(int x, int y, int screenPixelsX)
    {
        mRectF.set( x, y, x+size, y+size);
        speedX = screenPixelsX / 3;
        speedY = speedX;
    }

    public void reset(int x, int y, float speedToX, float speedToY)
    {
        mRectF.set( x, y, x+size, y+size);
        speedX = speedToX;
        speedY = speedToY;
    }

    public void reverseSideXtoLeft()
    {
        speedSideX = -1;
    }
    public void reverseSideXtoRight()
    {
        speedSideX = 1;
    }

    public void reverseSideYtoTop()
    {
        speedSideY = -1;
        //Log.d("ThunderStudio", "reverseSideYtoTop has been called");
    }

    public void reverseSideYtoBottom()
    {
        speedSideY = 1;
        //Log.d("ThunderStudio", "reverseSideYtoBottom has been called");
    }

    public boolean checkHitBat(RectF batRectF, long currentFPS)
    {
        if( (mRectF.bottom > batRectF.top)
            &&( (mRectF.bottom >0)
                &&( ((mRectF.right < batRectF.right)&&(mRectF.left > batRectF.left))
                    ||  ((mRectF.left < batRectF.left)&&(mRectF.left + size > batRectF.left))
                    ||  ((mRectF.right > batRectF.right)&&(mRectF.right - size < batRectF.right))   )   )  )

        {
            boolean hitLeft = (batRectF.left + batRectF.width() / 2 - mRectF.left - size / 2) > 0; // Middle seem like hit on right side

            if (hitLeft)
            {
                reverseSideXtoLeft();
            } else
            {
                reverseSideXtoRight();
            }

            reverseSideYtoTop();

            while (mRectF.bottom > batRectF.top)
            {
                update(currentFPS);
            }

            Log.d("ThunderStudio", "Ball Hit Bat: ball: "+ mRectF.toShortString() + ", bat: " + batRectF.toShortString());
            Log.d("ThunderStudio", "Ball hit bat: { SpeedX = " + speedSideX*speedX +", SpeedY = " + speedSideY*speedY);

            return true;
        }
        else
        {
            return false;
        }
    }

    public void setSpeed(float toX, float toY)
    {
        speedX = toX;
        speedY = toY;
    }

    public void speedUp(float increaseX, float increaseY)
    {
        speedX = speedX + increaseX;
        speedY = speedX + increaseY;
    }

    public RectF getRectF()
    {
        return mRectF;
    }
}
