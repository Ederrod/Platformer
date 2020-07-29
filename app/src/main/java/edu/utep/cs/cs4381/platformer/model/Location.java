package edu.utep.cs.cs4381.platformer.model;

public class Location {
    String level;
    float x;
    float y;

    public Location(String level, float x, float y){
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public String getLevel() {
        return level;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}