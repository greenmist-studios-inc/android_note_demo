package com.lextech.androiddemo.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.adapter.FloatingNotesAdapter;
import com.lextech.androiddemo.event.CloseFloatingEvent;
import com.lextech.androiddemo.helper.DAO;
import de.greenrobot.event.EventBus;

/**
 * User: geoffpowell
 * Date: 12/14/15
 */
public class FloatingListView extends CardView implements View.OnTouchListener, View.OnClickListener {

    private ListView listView;
    private ImageButton imageButton;
    private FloatingNotesAdapter adapter;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private FrameLayout titleBar;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    public FloatingListView(Context context) {
        super(context);
    }

    public FloatingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatingListView(Context context, WindowManager.LayoutParams params) {
        super(context);
        setCardElevation(3);
        setRadius(5 * getResources().getDisplayMetrics().density);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.params = params;

        listView = new ListView(context);
        adapter = new FloatingNotesAdapter(DAO.getNotes(context));
        listView.setAdapter(adapter);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        listView.setPadding(0,72,0,0);
        addView(listView);

        titleBar = new FrameLayout(context);
        titleBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        titleBar.setBackgroundColor(getResources().getColor(R.color.colorAccent));

        imageButton = new ImageButton(context);
        imageButton.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP|Gravity.END));
        imageButton.setImageDrawable(getResources().getDrawable(R.drawable.minimize));
        imageButton.setPadding(8,8,8,8);
        imageButton.setBackground(null);
        imageButton.setOnClickListener(this);
        titleBar.addView(imageButton);
        addView(titleBar);

        titleBar.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(this, params);
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new CloseFloatingEvent());
    }

    public void refreshNotes() {
        adapter.setNotes(DAO.getNotes(getContext()));
    }


}
