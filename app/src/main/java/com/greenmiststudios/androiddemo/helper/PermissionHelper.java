package com.greenmiststudios.androiddemo.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.greenmiststudios.demolibrary.R;

/**
 * User: geoffpowell
 * Date: 11/16/15
 */
public class PermissionHelper {

    public static final int REQUEST_CODE = 6;

    public static boolean checkPermissions(final Activity activity, @StringRes int error, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Snackbar.make(activity.findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG)
                        .setActionTextColor(activity.getResources().getColor(R.color.colorAccent))
                        .setAction(R.string.settings, v -> {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + activity.getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                        }).show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission},
                        REQUEST_CODE);
            }
            return false;
        } else {
            return true;
        }
    }
}
