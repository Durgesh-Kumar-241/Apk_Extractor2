package com.dktechub.apkextractor2;

import android.graphics.drawable.Drawable;

public class App {
    String name,pathTobinary;
    long size;
    Drawable icon;

    public App(String s, String sourceDir, Drawable loadIcon, long length) {
        this.name=s;
        this.pathTobinary=sourceDir;
        this.icon=loadIcon;
        this.size=length;
    }
}
