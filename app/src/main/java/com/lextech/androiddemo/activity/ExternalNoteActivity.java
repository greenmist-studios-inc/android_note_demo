package com.lextech.androiddemo.activity;

import android.Manifest;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.helper.DAO;
import com.lextech.demolibrary.Note;
import com.lextech.demolibrary.NoteAction;

/**
 * User: geoffpowell
 * Date: 11/24/15
 */
public class ExternalNoteActivity extends AppCompatActivity {

    public static final String CATEGORY_VOICE = "com.google.android.voicesearch.SELF_NOTE";
    public static final String CATEGORY_APPEND = "com.lextech.androiddemo.APPEND";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(CATEGORY_APPEND)) {
            long id = getIntent().getLongExtra(NoteActivity.EXTRA_NOTE, 0);
            if (id == 0) {
                finishAndRemoveTask();
                return;
            }
            Note note = DAO.getNote(this, id);
            note.setNote(note.getNote() +"\n" + getIntent().getStringExtra(Intent.EXTRA_TEXT));
            DAO.save(note, this);
            Intent home = new Intent(getApplicationContext(), HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            intent.putExtra(NoteActivity.EXTRA_NOTE, note);
            intent.putExtra(NoteActivity.EXTRA_APPEND, true);
            TaskStackBuilder
                    .create(getApplicationContext())
                    .addNextIntent(home)
                    .addNextIntent(intent)
                    .startActivities();
        } else if (Intent.ACTION_SEND.equals(getIntent().getAction()) ||
                "com.google.android.gm.action.AUTO_SEND".equals(getIntent().getAction())) {
            boolean isVoiceNote = getIntent().hasCategory(CATEGORY_VOICE);
            String title = isVoiceNote ? "Voice Note" : "Text Note";
            if (!isVoiceNote) Toast.makeText(this, R.string.message_note_created, Toast.LENGTH_LONG).show();
            Note note = new Note(title, getIntent().getStringExtra(Intent.EXTRA_TEXT), null);
            note.setNoteOrder(-1);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
                String provider = locationManager.getBestProvider(criteria, true);
                if (TextUtils.isEmpty(provider)) return;
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    note.setLatitude(location.getLatitude());
                    note.setLongitude(location.getLongitude());
                }
            }
            Log.d("New Note", note.toString());
            DAO.save(note, this);
            DAO.save(new NoteAction(note), this);
        }
        finishAndRemoveTask();
    }

}
