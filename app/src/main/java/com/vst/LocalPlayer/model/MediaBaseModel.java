package com.vst.LocalPlayer.model;


public class MediaBaseModel {
    public long id;
    public int width;
    public int height;
    public String name;
    public String metaTitle;
    public String title;
    public String relativePath;
    public String devicePath;
    public String poster;

    public MediaBaseModel() {
    }

    public MediaBaseModel(long id, String path, String name, String title, String poster, String devicePath) {
        relativePath = path;
        this.name = name;
        this.devicePath = devicePath;
        this.title = title;
        this.poster = poster;
        this.id = id;
    }
}
