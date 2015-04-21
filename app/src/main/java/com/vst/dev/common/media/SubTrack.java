package com.vst.dev.common.media;


public class SubTrack {
    public String name;
    public String path;
    public int trackId;
    public SubTrackType from;
    public String language;


    public static enum SubTrackType {
        Local, Internal, Internet
    }
}
