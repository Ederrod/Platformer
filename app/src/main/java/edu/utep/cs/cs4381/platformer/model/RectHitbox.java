package edu.utep.cs.cs4381.platformer.model;

public class RectHitbox {
    public float top;
    public float left;
    public float bottom;
    public float right;
    public float height;

    public boolean intersects(RectHitbox rectHitbox){
        boolean hit = false;
        if(this.right > rectHitbox.left
                && this.left < rectHitbox.right ){
            // Intersecting on x axis
            if(this.top < rectHitbox.bottom
                    && this.bottom > rectHitbox.top ){
                // Intersecting on y as well
                // Collision
                hit = true;
            }
        }
        return hit;
    }


    public float getTop() {
        return top;
    }
    public void setTop(float top) {
        this.top = top;
    }

    public float getLeft() {
        return left;
    }
    public void setLeft(float left) {
        this.left = left;
    }

    public float getBottom() {
        return bottom;
    }
    public void setBottom(float bottom) {
        this.bottom = bottom;
    }

    public float getRight() {
        return right;
    }
    public void setRight(float right) {
        this.right = right;
    }

    public float getHeight() {
        return height;
    }
    public void setHeight(float height) {
        this.height = height;
    }
}