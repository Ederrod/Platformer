package edu.utep.cs.cs4381.platformer.model;

import edu.utep.cs.cs4381.platformer.model.LevelManager.Level;
public class Location {
    Level level;

    float x;
    float y;

    public Location(Level level, float x, float y){
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public Level getLevel() {
        return level;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}