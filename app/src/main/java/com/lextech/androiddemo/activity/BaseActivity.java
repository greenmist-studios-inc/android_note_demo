package com.lextech.androiddemo.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.lextech.androiddemo.Application;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.event.AppStateEvent;
import de.greenrobot.event.EventBus;
import icepick.Icepick;
import icepick.State;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @LayoutRes
    int contentView;

    @State
    boolean hasEvents;

    @State
    boolean stickyEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        if (contentView == 0) {
            finish();
            return;
        }
        setContentView(contentView);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasEvents) {
            if (stickyEvents) EventBus.getDefault().registerSticky(this);
            else EventBus.getDefault().register(this);
        }
        if (Application.activityOpenCounter == 0) EventBus.getDefault().postSticky(new AppStateEvent(true));
        Application.activityOpenCounter++;
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this);
        Application.activityOpenCounter--;
        if (Application.activityOpenCounter == 0) EventBus.getDefault().postSticky(new AppStateEvent(false));
        super.onStop();
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        Application.activityOpenCounter++;
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Application.activityOpenCounter--;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        finishAfterTransition();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
}
