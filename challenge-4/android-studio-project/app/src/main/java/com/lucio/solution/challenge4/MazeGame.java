package com.lucio.solution.challenge4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.Serializable;
import java.util.ArrayList;

public class MazeGame extends BroadcastReceiver {

    private static String MenuAction = "com.hackerone.mobile.challenge4.menu";

    public static String MoveAction  = "com.hackerone.mobile.challenge4.broadcast.MAZE_MOVER";

    //
    private Context context;

    //
    private MazeSolution solution;

    /**
     *
     * @param context
     */
    public MazeGame(Context context) {
        this.context = context;

        // Once initialized the class handles broadcast intents
        this.context
            .registerReceiver(
                this,
                new IntentFilter(MoveAction)
            );
    }

    /**
     * Starts the game
     */
    public void start() {
        context
            .sendBroadcast(
                new Intent(MenuAction)
                    .putExtra("start_game", true)
            );
    }

    /**
     * Makes a move
     */
    private void move(Direction direction) {
        char c = '-';

        switch (direction) {
            case Up:    c = 'k'; break;
            case Right: c = 'l'; break;
            case Down:  c = 'j'; break;
            case Left:  c = 'h'; break;
        }

        context
            .sendBroadcast(
                new Intent(MoveAction)
                    .putExtra("move", c)
            );
    }

    /**
     * Used internally by receiver
     */
    private void getMazeInfo() {
        context
            .sendBroadcast(
                new Intent(MoveAction)
                    .putExtra("get_maze", true)
            );
    }

    /**
     *
     */
    public void exploit() {
        getMazeInfo();
    }

    // Used internally from MazeGame
    private class MazeInfo implements MazeInfoInterface {

        private int      level;

        private Player   player;

        private Exit     exit;

        public MazeInfo(Serializable maze, ArrayList<Integer> positions) {
            this.level  = ((boolean[][])maze).length / 5;
            this.player = new Player(positions.get(0), positions.get(1));
            this.exit   = new Exit(positions.get(2), positions.get(3));
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public Exit getExit() {
            return exit;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // Ensure there're extras or exit
        if (intent.getExtras() == null) {
            return;
        }

        if (intent.hasExtra("walls")
                && intent.hasExtra("positions")) {

            MazeInfo mazeInfo = new MazeInfo(
                intent.getSerializableExtra("walls"),
                intent.getIntegerArrayListExtra("positions"));

            if ((solution = MazeSolver.getSolution(mazeInfo)) != null) {
                move(solution.poll());
            } else {
                // Time to exploit!
                MazePayload.send(this.context);
            }

        } else if (intent.hasExtra("move_result")) {
            if (solution.isEmpty()) {
                getMazeInfo();
            } else {
                move(solution.poll());
            }
        }
    }

}
