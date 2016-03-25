package com.lextech.androiddemo.helper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.design.widget.FloatingActionButton;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.lextech.androiddemo.helper.listener.SimpleAnimationListener;
import com.lextech.demolibrary.R;

/**
 * User: geoffpowell
 * Date: 11/13/15
 */
public class AnimationHelper {

    public static Animation createScaleInOutColor(final FloatingActionButton view, final int color, final Context context, final boolean in) {
        final Animation animation2 = AnimationUtils.loadAnimation(context, R.anim.scale_out);
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale_in);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.getBackground().setColorFilter(color, PorterDuff.Mode.SRC);
                view.setImageResource(in ? R.drawable.edit: R.drawable.save);
                view.startAnimation(animation2);
            }
        });
        return animation;
    }

}
