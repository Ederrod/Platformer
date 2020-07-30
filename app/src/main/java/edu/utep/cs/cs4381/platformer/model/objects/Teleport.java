package edu.utep.cs.cs4381.platformer.model.objects;

import edu.utep.cs.cs4381.platformer.model.Location;
import edu.utep.cs.cs4381.platformer.model.objects.GameObject;

public class Teleport extends GameObject {

    Location target;

    public Teleport(float worldStartX, float worldStartY,
             char type, Location target) {
        final float HEIGHT = 2;
        final float WIDTH = 2;
        setHeight(HEIGHT); // 2 metres tall
        setWidth(WIDTH); // 1 metre wide
        setType(type);
        setBitmapName("door");
        this.target = new Location(target.getLevel(),
                target.getX(), target.getY());
        // Where does the tile start
        // X and y locations from constructor parameters
        setWorldLocation(worldStartX, worldStartY, 0);
        setRectHitbox();
    }

    public Location getTarget(){
        return target;
    }

    public void update(long fps, float gravity){
    }
}