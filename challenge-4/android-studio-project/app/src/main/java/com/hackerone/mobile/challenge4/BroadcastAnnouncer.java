package com.hackerone.mobile.challenge4;

import java.io.Serializable;

public class BroadcastAnnouncer extends StateController implements Serializable {

    private static final long serialVersionUID = 1;
    private String destUrl;
    private String stringRef;
    private String stringVal;

    public BroadcastAnnouncer(String stringRef, String destUrl) {
        super("dummy");
        this.stringRef = stringRef;
        this.destUrl = destUrl;
    }

    public BroadcastAnnouncer(String stringVal, String stringRef, String destUrl) {
        super(stringVal);
        this.stringRef = stringRef;
        this.destUrl = destUrl;
    }

}

