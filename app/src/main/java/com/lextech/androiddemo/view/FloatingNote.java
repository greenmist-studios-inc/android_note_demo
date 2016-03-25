package com.lextech.androiddemo.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.*;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.event.CloseFloatingEvent;
import com.lextech.androiddemo.helper.DAO;
import com.lextech.demolibrary.Note;
import de.greenrobot.event.EventBus;

/**
 * User: geoffpowell
 * Date: 12/14/15
 */
public class FloatingNote extends FrameLayout implements View.OnTouchListener {

    @Bind(R.id.title)
    TextView title;
    @Bind(R.id.note)
    TextView noteView;
    @Bind(R.id.title_frame)
    FrameLayout titleFrame;
    @Bind(R.id.card)
    CardView card;
    @Bind(R.id.edit_title)
    EditText editTitle;
    @Bind(R.id.edit_note)
    EditText editNote;
    @Bind(R.id.header)
    View header;

    private View rootView;
    private Note note;

    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    public FloatingNote(Context context) {
        super(context);
    }

    public FloatingNote(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatingNote(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FloatingNote(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FloatingNote(Context context, long id, WindowManager.LayoutParams params) {
        super(context);
        this.params = params;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        note = DAO.getNote(context, id);
        if (note == null) return;
        rootView = LayoutInflater.from(context).inflate(R.layout.layout_floating_note, this, false);
        addView(rootView);
        ButterKnife.bind(this, rootView);
        title.setText(note.getTitle());
        noteView.setText(note.getNote());
        editTitle.setText(note.getTitle());
        editNote.setText(note.getNote());
        titleFrame.setOnClickListener(e -> {
                requestAllFocus();
                editTitle.setVisibility(VISIBLE);
                title.setVisibility(GONE);
                editTitle.requestFocus();
        });
        noteView.setOnClickListener(v -> {
            requestAllFocus();
            editNote.setVisibility(VISIBLE);
            noteView.setVisibility(GONE);
            editNote.requestFocus();
        });
        header.setOnTouchListener(this);
        setOnTouchListener((v, event) -> {
            // handle touching outside
            switch (event.getAction()) {
                case MotionEvent.ACTION_OUTSIDE:
                    clearAllFocus();
                    break;
            }
            return false;
        });
    }

    @OnFocusChange({R.id.edit_note, R.id.edit_title})
    public void focusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.edit_note && !hasFocus) {
            noteView.setVisibility(VISIBLE);
            editNote.setVisibility(GONE);
            noteView.setText(editNote.getText());
            note.setNote(noteView.getText().toString());
            DAO.save(note, getContext());
        } else if (view.getId() == R.id.edit_title && !hasFocus) {
            title.setVisibility(VISIBLE);
            editTitle.setVisibility(GONE);
            title.setText(editTitle.getText());
            note.setTitle(title.getText().toString());
            DAO.save(note, getContext());
        }
    }

    private void requestAllFocus() {
        if ((params.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE) != WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            return;
        params.flags = params.flags ^ WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(this, params);
    }

    private void clearAllFocus() {
        editTitle.clearFocus();
        editNote.clearFocus();
        params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(this, params);
    }

    @OnClick(R.id.minimize)
    void minimize() {
        clearAllFocus();
        EventBus.getDefault().post(new CloseFloatingEvent());
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    clearAllFocus();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
