package edu.utep.cs.cs4381.platformer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import edu.utep.cs.cs4381.platformer.controller.InputController;
import edu.utep.cs.cs4381.platformer.model.Background;
import edu.utep.cs.cs4381.platformer.model.Location;
import edu.utep.cs.cs4381.platformer.model.RectHitbox;
import edu.utep.cs.cs4381.platformer.model.objects.Drone;
import edu.utep.cs.cs4381.platformer.model.objects.GameObject;
import edu.utep.cs.cs4381.platformer.model.LevelManager;
import edu.utep.cs.cs4381.platformer.model.LevelManager.Level;
import edu.utep.cs.cs4381.platformer.model.MachineGun;
import edu.utep.cs.cs4381.platformer.model.PlayerState;
import edu.utep.cs.cs4381.platformer.model.SoundManager;
import edu.utep.cs.cs4381.platformer.model.Viewport;
import edu.utep.cs.cs4381.platformer.model.objects.Player;
import edu.utep.cs.cs4381.platformer.model.objects.Teleport;

public class PlatformView extends SurfaceView implements Runnable {

    private boolean debugging = true;
    private volatile boolean running;
    private Thread gameThread = null;

    private Paint paint;
    private Canvas canvas;
    private SurfaceHolder holder;

    private Context context;
    private long startFrameTime;
    private long timeThisFrame;
    private long fps;

    private LevelManager lm;
    private Viewport vp;
    private InputController ic;
    private SoundManager sm;

    private PlayerState ps;


    public PlatformView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.context = context;
        holder = getHolder();
        paint = new Paint();

        vp = new Viewport(screenWidth, screenHeight);
        sm = SoundManager.instance(context);

        ps = new PlayerState();

        loadLevel(Level.MOUNTAIN, 1, 16);
    }

    @Override
    public void run() {
        while (running) {
            startFrameTime = System.currentTimeMillis();
            update();
            draw();
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("error", "failed to pause thread");
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (lm != null) {
            ic.handleInput(event, lm, sm, vp);
        }

        return true;
    }

    private void loadLevel(Level level, float px, float py) {
        ic = new InputController(vp.getScreenWidth(), vp.getScreenHeight());

//        PointF location = new PointF(px, py);
        ps.saveLocation(new PointF(px, py));
        lm = new LevelManager(
                context,
                vp.getPixelsPerMetreX(),
                vp.getScreenWidth(),
                ic,
                level,
                px,
                py
        );

        vp.setWorldCenter(
                lm.getGameObjects().get(lm.getPlayerIndex()).getWorldLocation().x,
                lm.getGameObjects().get(lm.getPlayerIndex()).getWorldLocation().y
        );

        // reload the players current fire rate from the player state
        lm.getPlayer().bfg.setFireRate(ps.getFireRate());

    }

    private void update() {
        for (GameObject go : lm.getGameObjects()) {
            if (go.isActive()) {
                boolean clipped = vp.clipObject(
                        go.getWorldLocation().x,
                        go.getWorldLocation().y,
                        go.getWidth(),
                        go.getHeight()
                );

                if (!clipped) {
                    go.setVisible(true);

                    int collision = lm.getPlayer().checkCollisions(go.getHitbox());
                    if(collision > 0) {
                        // Handle game object collisions that are not bullets.
                        switch (go.getType()) {
                            case 'c':
                                sm.play(SoundManager.Sound.COIN_PICKUP);
                                go.setActive(false);
                                go.setVisible(false);
                                ps.gotCredit();
                                if (collision != 2) {
                                    lm.getPlayer().restorePreviousVelocity();
                                }
                                break;
                            case 'e':
                                sm.play(SoundManager.Sound.EXTRA_LIFE);
                                go.setActive(false);
                                go.setVisible(false);
                                ps.addLife();
                                if (collision != 2) {
                                    lm.getPlayer().restorePreviousVelocity();
                                }
                                break;
                            case 'u':
                                sm.play(SoundManager.Sound.GUN_UPGRADE);
                                go.setActive(false);
                                go.setVisible(false);
                                lm.getPlayer().bfg.upgradeRateOfFire();
                                ps.increaseFireRate();
                                break;
                            case 'd':
                                PointF loc;
                                sm.play(SoundManager.Sound.PLAYER_BURN);
                                ps.loseLife();
                                loc = new PointF(ps.loadLocation().x, ps.loadLocation().y);
                                lm.getPlayer().setWorldLocationX(loc.x);
                                lm.getPlayer().setWorldLocationY(loc.y);
                                lm.getPlayer().setxVelocity(0);
                                break;
                            case 'g':
                                sm.play(SoundManager.Sound.PLAYER_BURN);
                                ps.loseLife();
                                loc = new PointF(ps.loadLocation().x, ps.loadLocation().y);
                                lm.getPlayer().setWorldLocationX(loc.x);
                                lm.getPlayer().setWorldLocationY(loc.y);
                                lm.getPlayer().setxVelocity(0);
                                break;
                            case 'f':
                                sm.play(SoundManager.Sound.PLAYER_BURN);
                                ps.loseLife();
                                loc = new PointF(ps.loadLocation().x, ps.loadLocation().y);
                                lm.getPlayer().setWorldLocationX(loc.x);
                                lm.getPlayer().setWorldLocationY(loc.y);
                                lm.getPlayer().setxVelocity(0);
                                break;
                            case 't':
                                Teleport teleport = (Teleport) go;
                                Location target = teleport.getTarget();
                                loadLevel(target.getLevel(), target.getX(), target.getY());
                                sm.play(SoundManager.Sound.TELEPORT);
                                break;
                            default:
                                if (collision == 1) {
                                    lm.getPlayer().setxVelocity(0);
                                    lm.getPlayer().setPressingRight(false);
                                }
                                if (collision == 2) {
                                    lm.getPlayer().isFalling = false;
                                }
                                break;
                        }
                    }

                    // Handle bullet collisions
                    for (int i = 0; i < lm.getPlayer().bfg.getNumBullets(); i++) {
                        RectHitbox r = new RectHitbox();
                        r.setLeft(lm.getPlayer().bfg.getBulletX(i));
                        r.setTop(lm.getPlayer().bfg.getBulletY(i));
                        r.setRight(lm.getPlayer().bfg.getBulletX(i) + .1f);
                        r.setBottom(lm.getPlayer().bfg.getBulletY(i) + .1f);
                        if (go.getHitbox().intersects(r)) {
                            lm.getPlayer().bfg.hideBullet(i);
                            // Handle the collision
                            switch (go.getType()) {
                                case 'g': // guard
                                    go.setWorldLocationX(go.getWorldLocation().x + 2 * (lm.getPlayer().bfg.getDirection(i)));
                                    sm.play(SoundManager.Sound.HIT_GUARD);
                                    break;
                                case 'd': // drone
                                    sm.play(SoundManager.Sound.EXPLODE);
                                    go.setWorldLocation(-100, -100, 0);
                                    break;
                                default:
                                    sm.play(SoundManager.Sound.RICOCHET);
                            }
                        }
                    }

                    if(lm.isPlaying()) {
                        go.update(fps, lm.getGravity());
                        if (go.getType() == 'd') {
                            Drone d = (Drone) go;
                            d.setWaypoint(lm.getPlayer().getWorldLocation());
                        }
                    }
                } else {
                    go.setVisible(false);
                }
            }
        }

        if (lm.isPlaying()) {
            vp.setWorldCenter(
                    lm.getPlayer().getWorldLocation().x,
                    lm.getPlayer().getWorldLocation().y
            );

            // has player fallen out of the map?
            if (lm.getPlayer().getWorldLocation().x < 0
                    || lm.getPlayer().getWorldLocation().x > lm.getMapWidth()
                    || lm.getPlayer().getWorldLocation().y > lm.getMapHeight()) {
                sm.play(SoundManager.Sound.PLAYER_BURN);
                ps.loseLife();
                PointF location = new PointF(ps.loadLocation().x, ps.loadLocation().y);
                lm.getPlayer().setWorldLocationX(location.x);
                lm.getPlayer().setWorldLocationY(location.y);
                lm.getPlayer().setxVelocity(0);
            }
            // check if game is over
            if (ps.getLives() == 0) {
                ps = new PlayerState();
                loadLevel(Level.CAVE, 1, 16);
            }

        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            paint.setColor(Color.argb(255, 0, 0, 255));
            canvas.drawColor(Color.argb(255, 0, 0, 255));
            // Drawing background
            drawBackground(0,-3);

            // Draw all game objects
            Rect toScreen2d = new Rect();
            for (int layer = -1; layer <= 1; layer++) {
                for (GameObject go : lm.getGameObjects()) {
                    if (go.isVisible() && go.getWorldLocation().z == layer) {
                        toScreen2d.set(
                                vp.worldToScreen(
                                        go.getWorldLocation().x,
                                        go.getWorldLocation().y,
                                        go.getWidth(),
                                        go.getHeight()
                                )
                        );

                        if (go.isAnimated()) {

                            if (go.getFacing() == GameObject.RIGHT) {
                                Matrix flipper = new Matrix();
                                flipper.preScale(-1, 1);
                                Rect r = go.getRectToDraw(System.currentTimeMillis());
                                Bitmap b = Bitmap.createBitmap(
                                        lm.getBitmapsArray()[lm.getBitmapIndex(go.getType())],
                                        r.left,
                                        r.top,
                                        r.width(),
                                        r.height(),
                                        flipper,
                                        true
                                );
                                canvas.drawBitmap(b, toScreen2d.left, toScreen2d.top, paint);
                            } else {
                                canvas.drawBitmap(
                                        lm.getBitmapsArray()[lm.getBitmapIndex(go.getType())],
                                        go.getRectToDraw(System.currentTimeMillis()),
                                        toScreen2d,
                                        paint
                                );
                            }
                        } else {
                            canvas.drawBitmap(
                                    lm.getBitmapsArray()[lm.getBitmapIndex(go.getType())],
                                    toScreen2d.left,
                                    toScreen2d.top,
                                    paint
                            );
                        }
                    }
                }
            }

            // Draw image objects in front of player
//            drawBackground(4,0);

            // Draw bullets
            paint.setColor(Color.argb(255,255,255,255));
            MachineGun bfg = lm.getPlayer().bfg; // Saving player machine gun in temp var for more readable code.
            for (int i = 0; i < bfg.getNumBullets(); i++) {
                toScreen2d.set(
                        vp.worldToScreen(
                                bfg.getBulletX(i),
                                bfg.getBulletY(i),
                                .25f,
                                .05f
                        )
                );
                canvas.drawRect(toScreen2d, paint);
            }

            // Drawing HUD
            drawHUD();

            // Draw buttons
            paint.setColor(Color.argb(80, 255,255,255));
            List<Rect> buttonsToDraw = ic.getButtons();
            for (Rect r : buttonsToDraw) {
                RectF rf = new RectF(r.left, r.top, r.right, r.bottom);
                canvas.drawRoundRect(rf, 15f, 15f, paint);
            }

            // Draw paused text
            if (!lm.isPlaying()) {
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(120);
                canvas.drawText("Paused", vp.getScreenWidth() / 2f, vp.getScreenHeight() / 2f, paint);
            }

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.argb(255,255,255,255));
            paint.setTextSize(120);
            canvas.drawText("Eder", vp.getScreenWidth() / 2f, vp.getScreenHeight() / 2f, paint);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(int start, int stop) {
        Rect fromRect1 = new Rect(), toRect1 = new Rect();
        Rect fromRect2 = new Rect(), toRect2 = new Rect();

        for (Background bg : lm.getBackgrounds()) {
            if (!vp.clipObject(-1, bg.getY(), 100, bg.getHeight())) {
                int startY = (int) (vp.getyCentre() - (vp.getViewportWorldCentreY() - bg.getY())* vp.getPixelsPerMetreY());
                int endY = (int) (vp.getyCentre() - (vp.getViewportWorldCentreY() - bg.getEndY()) * vp.getPixelsPerMetreY());

                fromRect1 = new Rect(0, 0, bg.getWidth() - bg.getXClip(), bg.getHeight());
                toRect1 = new Rect(bg.getXClip(), startY, bg.getWidth(), endY);
                fromRect2 = new Rect(bg.getWidth() - bg.getXClip(), 0, bg.getWidth(), bg.getHeight());
                toRect2 = new Rect(0, startY, bg.getXClip(), endY);
            }

            // draw backgrounds
            if (!bg.isReversedFirst()) {
                canvas.drawBitmap(bg.getBitmap(), fromRect1, toRect1, paint);
                canvas.drawBitmap(bg.getBitmapReversed(), fromRect2, toRect2, paint);
            } else {
                canvas.drawBitmap(bg.getBitmap(), fromRect2, toRect2, paint);
                canvas.drawBitmap(bg.getBitmapReversed(), fromRect1, toRect1, paint);
            }

            // calculate the next value for the background's clipping position by modifying xClip
            // and switching which background is drawn first, if necessary.
            bg.setXClip(bg.getXClip() - (int) (lm.getPlayer().getxVelocity() / (20 / bg.getSpeed())));
            if (bg.getXClip() >= bg.getWidth()) {
                bg.setXClip(0);
                bg.setReversedFirst(!bg.isReversedFirst());
            } else if (bg.getXClip() <= 0) {
                bg.setXClip(bg.getWidth());
                bg.setReversedFirst(!bg.isReversedFirst());
            }
        }
    }

    private void drawHUD() {
        int topSpace = vp.getPixelsPerMetreY() / 4;
        int iconSize = vp.getPixelsPerMetreX();
        int padding = vp.getPixelsPerMetreX() / 5;
        int centring = vp.getPixelsPerMetreY() / 6;
        paint.setTextSize(vp.getPixelsPerMetreY() / 2f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(0,0,iconSize * 7.0f, topSpace*2 + iconSize,paint);
        paint.setColor(Color.argb(255, 255, 255, 0));
        canvas.drawBitmap(lm.getBitmap('e'), 0, topSpace, paint);
        canvas.drawText("" + ps.getLives(), (iconSize * 1) + padding, iconSize - centring, paint);
        canvas.drawBitmap(lm.getBitmap('c'), iconSize * 2.5f + padding, topSpace, paint);
        canvas.drawText("" + ps.getCredits(), (iconSize * 3.5f) + padding * 2, iconSize - centring, paint);
        canvas.drawBitmap(lm.getBitmap('u'), iconSize * 5.0f + padding, topSpace, paint);
        canvas.drawText("" + ps.getFireRate(), iconSize * 6.0f + padding * 2, iconSize - centring, paint);

    }

}
