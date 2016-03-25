package com.lextech.androiddemo.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowInsets;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.lextech.androiddemo.R;
import icepick.Icepick;
import icepick.State;

/**
 * User: geoffpowell
 * Date: 11/25/15
 */
public class BaseActivity extends GoogleApiActivity implements WatchViewStub.OnLayoutInflatedListener, View.OnApplyWindowInsetsListener {

    @LayoutRes
    int contentView;

    @Bind(R.id.dismiss_overlay)
    DismissOverlayView dismissOverlayView;

    WatchViewStub watchViewStub;

    @State boolean isRound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wearable_activity);
        if (contentView == 0) {
            finish();
            return;
        }
        watchViewStub = (WatchViewStub) findViewById(R.id.stub);
        watchViewStub.setRectLayout(contentView);
        watchViewStub.setRoundLayout(contentView);
        watchViewStub.setOnLayoutInflatedListener(this);
        watchViewStub.setOnApplyWindowInsetsListener(this);
    }

    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        watchViewStub.onApplyWindowInsets(insets);
        isRound = insets.isRound();
        return insets;
    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        ButterKnife.bind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
