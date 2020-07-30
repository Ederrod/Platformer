package edu.utep.cs.cs4381.platformer.model.objects;

import edu.utep.cs.cs4381.platformer.model.objects.GameObject;

public class Brick extends GameObject {

    public Brick(float worldStartX, float worldStartY, char type) {
        setTraversable();
        final float HEIGHT = 1;
        final float WIDTH = 1;
        setHeight(HEIGHT);
        setWidth(WIDTH);
        setType(type);
        setBitmapName("brick");
        setWorldLocation(worldStartX, worldStartY, 0);
        setRectHitbox();
    }

    public void update(long fps, float gravity) {
    }
}