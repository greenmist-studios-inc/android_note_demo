package com.greenmiststudios.androiddemo.service;

import android.animation.Animator;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.event.*;
import com.greenmiststudios.androiddemo.helper.PreferenceManager;
import com.greenmiststudios.androiddemo.helper.listener.SimpleAnimatorListener;
import com.greenmiststudios.androiddemo.view.FloatingListView;
import com.greenmiststudios.androiddemo.view.FloatingNote;
import de.greenrobot.event.EventBus;

/**
 * User: geoffpowell
 * Date: 12/11/15
 */
public class FloatingNoteService extends Service implements View.OnTouchListener {

    private WindowManager windowManager;
    private ImageView floatingButton;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams listParams;
    private WindowManager.LayoutParams noteParams;
    private WindowManager.LayoutParams deleteParams;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private GestureDetector gestureDetector;

    private FloatingListView listView;
    private FloatingNote floatingNote;
    private ImageView deleteView;

    private State state = State.BUTTON;

    private enum State { BUTTON, LIST, NOTE}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme);
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Toast.makeText(getApplicationContext(), R.string.error_overlay, Toast.LENGTH_LONG).show();
        } else {
            float density = getResources().getDisplayMetrics().density;

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            deleteView = new ImageView(this);
            deleteView.setImageResource(R.drawable.delete_floating_note);
            deleteView.setPadding(0,0,0,8);
            deleteView.setVisibility(View.GONE);

            deleteParams = new WindowManager.LayoutParams(
                    (int) (density * 54),
                    (int) (density * 54),
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            deleteParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL | Gravity.START;
            deleteParams.x = (getResources().getDisplayMetrics().widthPixels - (int) (density * 54))/2;
            deleteParams.y = getResources().getDisplayMetrics().heightPixels - 200;
            windowManager.addView(deleteView, deleteParams);

            floatingButton = new ImageView(this);
            floatingButton.setImageResource(R.drawable.share_icon);
            floatingButton.setElevation(5 * density);
            floatingButton.setClickable(true);

            gestureDetector = new GestureDetector(this, new SingleTapConfirm());

            params = new WindowManager.LayoutParams(
                    (int) (density * 48),
                    (int) (density * 48),
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            params.gravity = Gravity.TOP | Gravity.START;
            params.x = (int) PreferenceManager.getFloatValue(this, PreferenceManager.KEY_FLOATING_X);
            params.y = (int) PreferenceManager.getFloatValue(this, PreferenceManager.KEY_FLOATING_Y);
            if (params.y == 0) params.y = 100;

            floatingButton.setOnTouchListener(this);
            windowManager.addView(floatingButton, params);
            EventBus.getDefault().registerSticky(this);
        }
    }

    private boolean deleteOverlap() {
        Rect rect = new Rect(params.x, params.y, params.x + params.width, params.y + params.height);
        Rect delete = new Rect(deleteParams.x, deleteParams.y, deleteParams.x + deleteParams.width, deleteParams.y + deleteParams.height);
        return rect.intersect(delete);
    }

    private void animateDelete(final boolean hide) {
        deleteView.setScaleX(hide ? 1 : 0);
        deleteView.setScaleY(hide ? 1 : 0);
        deleteView.animate()
                .scaleYBy(hide ? -1 : 1)
                .scaleXBy(hide ? -1 : 1)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (!hide) deleteView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (hide) deleteView.setVisibility(View.GONE);
                    }
                })
                .start();
    }

    private void animateChangeView(View view, boolean open, boolean remove) {
        view.setScaleX(open ? 0 : 1);
        view.setScaleY(open ? 0 : 1);
        view.setAlpha(open ? 0 : 1);
        view.animate()
                .scaleYBy(open ? 1 : -1)
                .scaleXBy(open ? 1 : -1)
                .alpha(open ? 1 : -1)
                .setListener(new SimpleAnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (open) view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!open) {
                            view.setVisibility(View.GONE);
                        }
                        if (remove) windowManager.removeViewImmediate(view);
                    }
                })
                .start();
    }

    private void animateFadeOutView(View view, boolean open, boolean remove) {
        view.setAlpha(open ? 0 : 1);
        view.animate()
                .alpha(open ? 1 : -1)
                .setListener(new SimpleAnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (open) view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setAlpha(1);
                        if (!open) {
                            view.setVisibility(View.GONE);
                        }
                        if (remove) windowManager.removeViewImmediate(view);
                    }

                })
                .start();
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_UP:
                PreferenceManager.set(this, PreferenceManager.KEY_FLOATING_X, params.x);
                PreferenceManager.set(this, PreferenceManager.KEY_FLOATING_Y, params.y);
                animateDelete(true);
                if (deleteOverlap()) {
                    stopSelf();
                    PreferenceManager.set(this, PreferenceManager.KEY_FLOATING_NOTES, false);
                    PreferenceManager.set(this, PreferenceManager.KEY_FLOATING_X, 0);
                    PreferenceManager.set(this, PreferenceManager.KEY_FLOATING_Y, 0);
                    EventBus.getDefault().postSticky(new FloatingNoteChangedEvent());
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(floatingButton, params);
                if (deleteView.getVisibility() == View.GONE) animateDelete(false);
                return true;
        }
        return false;
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            animateFadeOutView(floatingButton, false, false);
            state = State.LIST;

            float density = getResources().getDisplayMetrics().density;
            listParams = new WindowManager.LayoutParams(
                    (int) (density * 128),
                    (int) (density * 172),
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            listParams.gravity = Gravity.TOP|Gravity.START;
            listParams.x = params.x;
            listParams.y = params.y;
            listView = new FloatingListView(FloatingNoteService.this, listParams);
            windowManager.addView(listView, listParams);
            animateChangeView(listView, true, false);
            return true;
        }
    }

    public void onEvent(CloseFloatingEvent event) {
        if (state == State.LIST) {
            floatingButton.setVisibility(View.VISIBLE);
            params.x = listParams.x;
            params.y = listParams.y;
            windowManager.updateViewLayout(floatingButton, params);
            animateChangeView(listView, false, true);
            animateFadeOutView(floatingButton, true, false);
            state = State.BUTTON;
        } else if (state == State.NOTE) {
            floatingButton.setVisibility(View.VISIBLE);
            params.x = noteParams.x;
            params.y = noteParams.y;
            windowManager.updateViewLayout(floatingButton, params);
            animateChangeView(floatingNote, false, true);
            animateFadeOutView(floatingButton, true, false);
            state = State.BUTTON;
        }
    }

    public void onEvent(FloatingNoteSelectedEvent event) {
        animateFadeOutView(listView, false, false);
        state = State.NOTE;
        float density = getResources().getDisplayMetrics().density;
        noteParams = new WindowManager.LayoutParams(
                (int)(density * 142),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        noteParams.x = listParams.x;
        noteParams.y = listParams.y;
        noteParams.gravity = Gravity.TOP|Gravity.START;
        windowManager.removeViewImmediate(listView);
        floatingNote = new FloatingNote(this, event.id, noteParams);
        windowManager.addView(floatingNote, noteParams);
        animateChangeView(floatingNote, true, false);
    }

    public void onEvent(AppStateEvent event) {
       if (state == State.BUTTON) floatingButton.setVisibility(event.open ? View.GONE : View.VISIBLE);
       else if (state == State.LIST) listView.setVisibility(event.open ? View.GONE : View.VISIBLE);
       else if (state == State.NOTE) floatingNote.setVisibility(event.open ? View.GONE : View.VISIBLE);
    }

    public void onEvent(RefreshDataEvent event) {
        if (state == State.LIST) listView.refreshNotes();
        else if (state == State.NOTE) {
            if (event.deleted) onEvent(new CloseFloatingEvent());
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onDestroy();
        if (floatingButton != null) windowManager.removeView(floatingButton);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        super.onTaskRemoved(rootIntent);
    }


}