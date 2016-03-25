package com.greenmiststudios.androiddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.ProgressSpinner;
import android.util.Log;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.android.gms.common.ConnectionResult;
import com.greenmiststudios.androiddemo.R;

/**
 * User: geoffpowell
 * Date: 11/24/15
 */
public class VoiceNoteActivity extends GoogleApiActivity {

    @Bind(R.id.spinner)
    ProgressSpinner progressSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_activity);
        ButterKnife.bind(this);
        progressSpinner.setColors(new int[]{getResources().getColor(R.color.colorAccent)});
        progressSpinner.showWithAnimation();
        if (!Intent.ACTION_SEND.equals(getIntent().getAction()) &&
                !"com.google.android.gm.action.AUTO_SEND".equals(getIntent().getAction())) finish();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        String note = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Log.d("VoiceNoteActivity", "Note " + note + " created.");
        sendMessage(NEW_NOTE, note.getBytes(), null);
        Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.message_note_created));
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.error_note_created));
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        super.onConnectionSuspended(i);
        Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.error_note_created));
        startActivity(intent);
        finish();
    }
}
