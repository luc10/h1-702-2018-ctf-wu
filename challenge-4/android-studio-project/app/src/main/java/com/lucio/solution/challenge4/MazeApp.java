package com.lucio.solution.challenge4;

import android.content.Context;

public class MazeApp {

    private static String PackageName = "com.hackerone.mobile.challenge4";

    /**
     * Runs the challenge app
     *
     * @param context
     */
    public static void run(Context context) {
        context.startActivity(
            context
                .getPackageManager()
                .getLaunchIntentForPackage(PackageName)
        );
    }

}
