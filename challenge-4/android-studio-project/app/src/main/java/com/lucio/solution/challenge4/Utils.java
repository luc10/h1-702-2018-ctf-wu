package com.lucio.solution.challenge4;

import java.util.concurrent.TimeUnit;

public class Utils {

    public static void sleep(int seconds) {
        try   { Thread.sleep(TimeUnit.SECONDS.toMillis(seconds)); }
        catch (InterruptedException e) {}
    }

}
