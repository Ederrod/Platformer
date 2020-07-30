package edu.utep.cs.cs4381.platformer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;

import edu.utep.cs.cs4381.platformer.controller.InputController;
import edu.utep.cs.cs4381.platformer.model.levels.LevelCave;
import edu.utep.cs.cs4381.platformer.model.levels.LevelCity;
import edu.utep.cs.cs4381.platformer.model.levels.LevelData;
import edu.utep.cs.cs4381.platformer.model.levels.LevelForest;
import edu.utep.cs.cs4381.platformer.model.levels.LevelMountain;
import edu.utep.cs.cs4381.platformer.model.objects.*;

public class LevelManager {

    public enum Level {
        CAVE,
        CITY,
        FOREST,
        MOUNTAIN
    }

    private Level level;
    private int mapWidth;
    private int mapHeight;
    private Player player;
    private int playerIndex;
    private boolean playing;
    private float gravity;
    private LevelData levelData;
    private List<GameObject> gameObjects;
    private List<Rect> currentButtons;
    private Bitmap[] bitmapsArray;

    private List<Background> backgrounds;

    public LevelManager(Context context, int pixelsPerMetre, int screenWidth,
                        InputController ic, Level level, float px, float py) {
        this.level = level;
        switch (level) {
            case CAVE:
                levelData = new LevelCave();
                break;
            case CITY:
                levelData = new LevelCity();
                break;
            case FOREST:
                levelData = new LevelForest();
                break;
            case MOUNTAIN:
                levelData = new LevelMountain();
                break;
        }
        gameObjects = new ArrayList<>();
        bitmapsArray = new Bitmap[25];
        loadMapData(context, pixelsPerMetre, px, py);
        setWaypoints();
        playing = true;
        loadBackgrounds(context, pixelsPerMetre, screenWidth);
    }

    public int getBitmapIndex(char blockType) {
        int index = 0;
        switch (blockType) {
            case '.':
                index = 0;
                break;
            case '1':
                index = 1;
                break;
            case 'p':
                index = 2;
                break;
            case 'c':
                index = 3;
                break;
            case 'u':
                index = 4;
                break;
            case 'e':
                index = 5;
                break;
            case 'd':
                index = 6;
                break;
            case 'g':
                index = 7;
                break;
            case 'f':
                index = 8;
                break;
            case '2':
                index = 9;
                break;
            case '3':
                index = 10;
                break;
            case '4':
                index = 11;
                break;
            case '5':
                index = 12;
                break;
            case '6':
                index = 13;
                break;
            case '7':
                index = 14;
                break;
            case 'w':
                index = 15;
                break;
            case 'x':
                index = 16;
                break;
            case 'l':
                index = 17;
                break;
            case 'r':
                index = 18;
                break;
            case 's':
                index = 19;
                break;
            case 'm':
                index = 20;
                break;
            case 'z':
                index = 21;
                break;
            case 't':
                index = 22;
                break;
        }
        return index;
    }

    public Bitmap getBitmap(char blockType) {
        return bitmapsArray[getBitmapIndex(blockType)];
    }

    public Level getLevel() {
        return level;
    }

    public Bitmap[] getBitmapsArray() {
        return bitmapsArray;
    }

    public float getGravity() {
        return gravity;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getPlayerIndex() {
        return playerIndex;
    }

    public LevelData getLevelData() {
        return levelData;
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public List<Rect> getCurrentButtons() {
        return currentButtons;
    }

    public List<Background> getBackgrounds() {
        return backgrounds;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void switchPlayingStatus() {
        playing = !playing;
        if (playing) {
            gravity = 6;
        } else {
            gravity = 0;
        }
    }

    public void setWaypoints() {
        for (GameObject guard: gameObjects) {
            if (guard.getType() == 'g') {
                int feetTileIndex = -1; // index of the tile beneath the guard
                float leftEnd = -1, rightEnd = -1;  // left and right ends of the calculated route
                for (GameObject tile: gameObjects) {
                    feetTileIndex++;
                    if (tile.getWorldLocation().y == guard.getWorldLocation().y + 2
                            &&  tile.getWorldLocation().x == guard.getWorldLocation().x) {
                        leftEnd = gameObjects.get(feetTileIndex - 5).getWorldLocation().x;
                        for (int i = 1; i <= 5; i++) {
                            GameObject left = gameObjects.get(feetTileIndex - i);
                            if (left.getWorldLocation().x != guard.getWorldLocation().x - i
                                    || left.getWorldLocation().y != guard.getWorldLocation().y + 2
                                    || !left.isTraversable()) {
                                leftEnd = gameObjects.get(feetTileIndex - (i - 1)).getWorldLocation().x;
                                break; } }

                        rightEnd = gameObjects.get(feetTileIndex + 5).getWorldLocation().x;
                        for (int i = 1; i <= 5; i++) {
                            GameObject right = gameObjects.get(feetTileIndex + i);
                            if (right.getWorldLocation().x != guard.getWorldLocation().x + i
                                    || right.getWorldLocation().y != guard.getWorldLocation().y + 2
                                    || !right.isTraversable()) {
                                rightEnd = gameObjects.get(feetTileIndex + (i - 1)).getWorldLocation().x;
                                break; } }

                        ((Guard) guard).setWaypoints(leftEnd, rightEnd);
                    }
                }
            }
        }
    }

    private void loadMapData(Context context, int pixelsPerMeter, float px, float py) {
        int currentIndex = -1;
        int teleportIndex = -1;
        mapHeight = levelData.getTiles().size();
        mapWidth = levelData.getTiles().get(0).length();
        for (int i = 0; i < levelData.getTiles().size(); i++) {
            for (int j = 0; j < levelData.getTiles().get(i).length(); j++) {
                char c = levelData.getTiles().get(i).charAt(j);
                if (c != '.') {
                    currentIndex++;
                    switch (c) {
                        case '1':
                            gameObjects.add(new Grass(j, i, c));
                            break;
                        case 'p':
                            player = new Player(context, px, py, pixelsPerMeter);
                            gameObjects.add(player);
                            playerIndex = currentIndex;
                            break;
                        case 'c':
                            gameObjects.add(new Coin(j, i, c));
                            break;
                        case 'u':
                            gameObjects.add(new MachineGunUpgrade(j, i, c));
                            break;
                        case 'e':
                            gameObjects.add(new ExtraLife(j, i, c));
                            break;
                        case 'd':
                            gameObjects.add(new Drone(j, i, c));
                            break;
                        case 'g':
                            gameObjects.add(new Guard(context, j, i, c, pixelsPerMeter));
                            break;
                        case 'f':
                            gameObjects.add(new Fire(context, j, i, c, pixelsPerMeter));
                            break;
                        case '2':
                            gameObjects.add(new Snow(j, i, c));
                            break;
                        case '3':
                            gameObjects.add(new Brick(j, i, c));
                            break;
                        case '4':
                            gameObjects.add(new Coal(j, i, c));
                            break;
                        case '5':
                            gameObjects.add(new Concrete(j, i, c));
                            break;
                        case '6':
                            gameObjects.add(new Scorched(j, i, c));
                            break;
                        case '7':
                            gameObjects.add(new Stone(j, i, c));
                            break;
                        case 'w':
                            gameObjects.add(new Tree(j, i, c));
                            break;
                        case 'x':
                            gameObjects.add(new Tree2(j, i, c));
                            break;
                        case 'l':
                            gameObjects.add(new Lampost(j, i, c));
                            break;
                        case 'r':
                            gameObjects.add(new Stalactite(j, i, c));
                            break;
                        case 's':
                            gameObjects.add(new Stalagmite(j, i, c));
                            break;
                        case 'm':
                            gameObjects.add(new Cart(j, i, c));
                            break;
                        case 'z':
                            gameObjects.add(new Boulders(j, i, c));
                            break;
                        case 't':
                            teleportIndex++;
                            gameObjects.add(new Teleport(j, i, c, levelData.getLocations().get(teleportIndex)));
                            break;
                    }
                    if (bitmapsArray[getBitmapIndex(c)] == null) {
                        GameObject go = gameObjects.get(currentIndex);
                        bitmapsArray[getBitmapIndex(c)] = go.prepareBitmap(context,
                                go.getBitmapName(), pixelsPerMeter);
                    }
                }
            }
        }
    }

    private void loadBackgrounds(Context context, int pixelsPerMetre, int screenWidth) {
        backgrounds = new ArrayList<>();

        for (BackgroundData bgData : levelData.getBackgroundDataList()) {
            backgrounds.add(new Background(context, pixelsPerMetre, screenWidth, bgData));
        }
    }
}
