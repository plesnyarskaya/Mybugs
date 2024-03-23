package com.example.mybugs.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.mybugs.R;
import com.example.mybugs.activity.MainActivity;
import com.example.mybugs.activity.Game;
import com.example.mybugs.objects.Bug;
import com.example.mybugs.utils.Assets;
import com.example.mybugs.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressLint("ViewConstructor")
public class CustomView extends View
{
    private final List<Bug> bugs = new ArrayList<>();
    private final Paint paint = new Paint();

    private List<ImageView> healthImages;

    public void setHealthImages(List<ImageView> healthImages) {
        this.healthImages = healthImages;
    }

    public static Context context;

    public static final int screenWidth = 900;
    public static final int screenHeight = 1300;


    @SuppressLint("ClickableViewAccessibility")
    public CustomView(Game game)
    {
        super(game.getApplicationContext());
        context = game.getApplicationContext();
        this.init();
        AtomicInteger cnt = new AtomicInteger();
        Assets.init(context);
        setFocusable(true);
        setOnTouchListener((v, event) ->
        {
            int actionMask = event.getAction();
            if (MotionEvent.ACTION_DOWN == actionMask) {
                for (Bug bug : bugs) {
                    if (bug.isTouched(event.getX(), event.getY()))
                    {
                        handleInsectTouch(bug, game, cnt.incrementAndGet());
                        return true;
                    }
                }
                handleMissedInsect(game);
            }
            return true;
        });
    }


    private void handleInsectTouch(Bug bug, Game game, int cnt)
    {

        Log.i(bug.getName(), "KILLED");
        bug.setTouched(true);
        Assets.playSquishSound();
        new Handler().postDelayed(() ->
        {
            bugs.remove(bug);
            bug.interrupt();
            if (cnt > game.getScore())MainActivity.highScoreText.setText(String.valueOf(cnt));
        }, 1000);
    }

    private void handleMissedInsect(Game game) {
        if (!healthImages.isEmpty()) {
            healthImages.get(0).setImageResource(0);
            healthImages.remove(0);
            Assets.playThumpSound();
        } else {
            for (Bug bug : bugs) {
                bug.setX(9000);
                bug.setY(9000);
            }
            Assets.playGameOverSound();
            game.finish();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        super.onDraw(canvas);
        postInvalidate();
        for (Bug bug : bugs) {
            bug.draw(canvas, paint);

        }
    }

    public void init()
    {
        Pair<Integer, Integer> spiderPair =  new Pair<>(R.drawable.bug1, R.drawable.bug2);
        Pair<Integer, Integer> scorpPair =  new Pair<>(R.drawable.god1, R.drawable.god2);
        for (int i = 0; i < 6; i++) {
            float x = (float) (Math.random() * screenWidth);
            float y = (float) (Math.random() * screenHeight);
            float speedX = (float) (Math.random() * 10);
            float speedY = (float) (Math.random() * 10);
            Bug bug = new Bug(x, y, speedX, speedY, i, Assets.loadTextures(context, i < 3 ? scorpPair : spiderPair));
            bugs.add(bug);
            bug.start();
        }
    }
}

