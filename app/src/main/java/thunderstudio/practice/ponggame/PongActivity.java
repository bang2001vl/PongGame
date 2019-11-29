package thunderstudio.practice.ponggame;

import android.app.Activity;
import android.os.Bundle;

public class PongActivity extends Activity {

    // Initialize variables

    int screenTotalPixelsX;
    int screenTotalPixelsY;
    private PongGame mPongGame;

    // Overide methods

    @Override
    protected void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);

        android.view.Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point point = new android.graphics.Point();
        display.getSize(point);

        screenTotalPixelsX = point.x;
        screenTotalPixelsY = point.y;

        mPongGame = new PongGame(this, screenTotalPixelsX, screenTotalPixelsY);

        setContentView(mPongGame);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mPongGame.resume();
    }

    @Override
    protected void onPause()
    {
        mPongGame.pause();

        super.onPause();
    }


    // Initialize new methods


}
