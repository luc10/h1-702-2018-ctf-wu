package com.hackerone.mobile.challenge4;

import android.content.Context;

public abstract class StateController {

    private String location;

    /**
     * The no-arg constructor is required for top parent class
     * which doesn't implement Serializable interface
     */
    public StateController() {
        //
    }

    /**
     *
     * @param str
     */
    public StateController(String str) {
        this.location = str;
    }

    /**
     *
     * @param context
     * @return
     */
    Object load(Context context) {
        return null;
    }

    /**
     *
     * @param context
     * @param obj
     */
    void save(Context context, Object obj) {
        //
    }

    /**
     *
     * @return
     */
    String getLocation() {
        return this.location;
    }

}