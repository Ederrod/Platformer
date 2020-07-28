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
import edu.utep.cs.cs4381.platformer.model.Drone;
import edu.utep.cs.cs4381.platformer.model.GameObject;
import edu.utep.cs.cs4381.platformer.model.LevelManager;
import edu.utep.cs.cs4381.platformer.model.MachineGun;
import edu.utep.cs.cs4381.platformer.model.PlayerState;
import edu.utep.cs.cs4381.platformer.model.SoundManager;
import edu.utep.cs.cs4381.platformer.model.Viewport;

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
        loadLevel("LevelCave", 5, 2);
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

    private void loadLevel(String level, float px, float py) {
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
                        switch (go.getType()) {
                            case 'c':
                                sm.play(SoundManager.Sound.COIN_PICKUP);
                                go.setActive(false);
                                go.setVisible(false);
                                logGameObject(go);
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
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            paint.setColor(Color.argb(255, 0, 0, 255));
            canvas.drawColor(Color.argb(255, 0, 0, 255));

            Rect toScreen2d = new Rect();
            for (int layer = -1; layer <= 1; layer++) {
                for (GameObject go : lm.getGameObjects()) {
                    if (go.getType() == 'g') {
                        Log.d("PlatformView", "gaurd is visiable: " + go.isVisible());
                    }
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

            // Draw buttons
            paint.setColor(Color.argb(80, 255,255,255));

            List<Rect> buttonsToDraw = ic.getButtons();
            for (Rect r : buttonsToDraw) {
                RectF rf = new RectF(r.left, r.top, r.right, r.bottom);
                canvas.drawRoundRect(rf, 15f, 15f, paint);
            }

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

    private void logGameObject(GameObject go) {
        Log.w("PlatformView", "Type="+go.getType());
    }
}
