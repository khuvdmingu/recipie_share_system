package com.hm.zwap.Model;

import java.util.ArrayList;
import java.util.List;

public class Recipies {
    public String title;
    public String description;
    public String img;
    public int good;
    public int soso;
    public int bad;
    public List<String> combination = new ArrayList<>();
    public String brandCode;

    public Recipies(String title, String description, String img, int good, int soso, int bad, List<String> combination, String brandCode ) {
        this.title = title;
        this.description = description;
        this.img = img;
        this.good = good;
        this.soso = soso;
        this.bad = bad;
        this.combination = combination;
        this.brandCode = brandCode;
    }
}
