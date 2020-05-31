package com.sharesmile.share.profile.model;

import java.util.HashMap;

public class CharityOverviewColors {
    public HashMap<String,Colors> colorsHashMap;

    public CharityOverviewColors() {
        colorsHashMap = new HashMap<>();
        colorsHashMap.put("Water Conservation",new Colors("0CE8FF","88F4FF"));
        colorsHashMap.put("Sanitation",new Colors("a6e28b","bbf0b8"));
        colorsHashMap.put("Livelihood Development",new Colors("8737B9","C267FA"));
        colorsHashMap.put("Environment",new Colors("5d9a56","9AD887"));
        colorsHashMap.put("Healthcare",new Colors("ab0c2d","ed154d"));
        colorsHashMap.put("Hunger",new Colors("F3775B","FEA38E"));
        colorsHashMap.put("Education",new Colors("182674","3366db"));
        colorsHashMap.put("Women Empowerment",new Colors("f37282","fdb2d3"));
        colorsHashMap.put("Child Rights",new Colors("d168fc","f39ff1"));
        colorsHashMap.put("Animal Welfare",new Colors("4d346c","664292"));
        colorsHashMap.put("Disaster Relief",new Colors("7c3422","be5e39"));
        colorsHashMap.put("Sports",new Colors("",""));
        colorsHashMap.put("Poverty",new Colors("f7b90c","fee214"));
    }

    public class Colors
    {
        public String darkColor;
        public String lightColor;

        public Colors(String darkColor, String lightColor) {
            this.darkColor = darkColor;
            this.lightColor = lightColor;
        }
    }
}

