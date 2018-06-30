package com.lucio.solution.challenge4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MazeApp.run(this);
        MazeGame mazeGame = new MazeGame(this);

        // We need to sleep a while to ensure the app is running. This is not
        // so elegant but other methods require permission grant.
        Utils.sleep(3);
        mazeGame.start();

        Utils.sleep(3);
        mazeGame.exploit();
    }

}
