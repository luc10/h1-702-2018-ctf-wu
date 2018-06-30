package com.hackerone.mobile.challenge4;

import android.content.Context;

import java.io.Serializable;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1;

    public String cleanupTag;
    private Context context;
    public int levelsCompleted;
    public int playerX;
    public int playerY;
    public long seed;
    public StateController stateController;

    public GameState(StateController stateController) {
        this.stateController = stateController;
    }

    public GameState(String cleanupTag, StateController stateController) {
        this.cleanupTag = cleanupTag;
        this.stateController = stateController;
    }

}
