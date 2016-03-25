package com.greenmiststudios.androiddemo.view;

import android.content.Context;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.greenmiststudios.androiddemo.R;

/**
 * User: geoffpowell
 * Date: 11/30/15
 */
public class WearableFrameLayout extends FrameLayout implements WearableListView.OnCenterProximityListener {

    @Bind(R.id.image) CircledImageView imageView;

    @Bind(R.id.text_layout) LinearLayout textLayout;

    private int unselectedColor;
    private int selectedColor;

    public WearableFrameLayout(Context context) {
        this(context, null);
    }

    public WearableFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        selectedColor = getResources().getColor(R.color.colorAccent);
        unselectedColor = getResources().getColor(R.color.grey);
    }

    public void setUnselectedColor(int unselectedColor) {
        this.unselectedColor = unselectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            animate()
                    .alpha(1)
                    .translationX(20)
                    .setDuration(100)
                    .start();

            imageView.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .translationX(8)
                    .translationY(8)
                    .setDuration(100)
                    .start();

            textLayout.animate()
                    .translationX(8)
                    .translationY(8)
                    .setDuration(100)
                    .start();
        }
        imageView.setCircleColor(selectedColor);
        DrawableCompat.wrap(imageView.getImageDrawable()).setTint(getResources().getColor(R.color.white));
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            animate()
                    .alpha(0.8f)
                    .translationX(0)
                    .setDuration(100)
                    .start();
            imageView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
        }
        imageView.setCircleColor(unselectedColor);
        DrawableCompat.wrap(imageView.getImageDrawable()).setTint(getResources().getColor(R.color.black));
    }
}
