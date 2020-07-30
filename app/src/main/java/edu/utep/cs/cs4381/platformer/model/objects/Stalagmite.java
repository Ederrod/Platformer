package edu.utep.cs.cs4381.platformer.model.objects;

import java.util.Random;

import edu.utep.cs.cs4381.platformer.model.objects.GameObject;

public class Stalagmite extends GameObject {
    public Stalagmite(float worldStartX, float worldStartY, char type) {
        final float HEIGHT = 3;
        final float WIDTH = 2;
        setHeight(HEIGHT);
        setWidth(WIDTH);
        setType(type);
        setBitmapName("stalagmite");
        setActive(false);
        Random rand = new Random();
        if (rand.nextInt(2) == 0) {
            setWorldLocation(worldStartX, worldStartY, -1);
        } else {
            setWorldLocation(worldStartX, worldStartY, 1);
        }
    }

    public void update(long fps, float gravity) {
    }
}