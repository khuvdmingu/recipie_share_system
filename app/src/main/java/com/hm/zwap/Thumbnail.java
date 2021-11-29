package com.hm.zwap;

import android.graphics.drawable.Drawable;

public class Thumbnail {
    public String name;
    public String description;
    public String img;
    public boolean section = false;

    public Thumbnail() {
    }

    public Thumbnail(String name, String description, String img) {
        this.name = name;
        this.section = section;
        this.description = description;
        this.img = img;
    }

}