package com.greenmiststudios.androiddemo.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.greenmiststudios.androiddemo.event.FloatingNoteChangedEvent;
import com.greenmiststudios.androiddemo.helper.PreferenceManager;
import com.greenmiststudios.androiddemo.service.FloatingNoteService;
import de.greenrobot.event.EventBus;

import static com.greenmiststudios.androiddemo.helper.PreferenceManager.KEY_FLOATING_NOTES;

/**
 * User: geoffpowell
 * Date: 12/11/15
 */
@TargetApi(Build.VERSION_CODES.M)
public class RequestOverlayPermissionActivity extends AppCompatActivity {

    private static final int OVERLAY_PERMISSION_REQUEST = 1234;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQUEST) {
            if (Settings.canDrawOverlays(this)) {
                PreferenceManager.set(this, KEY_FLOATING_NOTES, true);
                startService(new Intent(this, FloatingNoteService.class));
                EventBus.getDefault().post(new FloatingNoteChangedEvent());
            }
        }
        finish();
    }
}
