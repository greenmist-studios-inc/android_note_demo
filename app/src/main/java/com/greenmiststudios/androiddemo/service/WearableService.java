package com.greenmiststudios.androiddemo.service;

import android.app.TaskStackBuilder;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.greenmiststudios.androiddemo.WearDataThread;
import com.greenmiststudios.androiddemo.activity.HomeActivity;
import com.greenmiststudios.androiddemo.activity.NoteActivity;
import com.greenmiststudios.androiddemo.activity.ExternalNoteActivity;
import com.greenmiststudios.androiddemo.event.RefreshEvent;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.demolibrary.Note;
import de.greenrobot.event.EventBus;

/**
 * User: geoffpowell
 * Date: 12/1/15
 */
public class WearableService extends WearableListenerService {

    private static final String TAG = "WearableService";
    private static final String OPEN_NOTE = "/OpenNote";
    public static final String DATA_NOTES = "/DataNotes";
    private static final String FETCH_DATA = "/FetchData";
    private static final String NEW_NOTE = "/NewNote";
    private static final String DELETE_NOTE = "/DeleteNote";

    @Override
    public void onCreate() {
        Log.d(TAG, "Service Created");
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d(TAG, "Message received " + event.getPath() + " " + new String(event.getData()));
        if(OPEN_NOTE.equals(event.getPath())) {
           long id = Long.parseLong(new String(event.getData()));
            Note note = DAO.getNote(this, id);
            if (note == null) return;
            Intent home = new Intent(getApplicationContext(), HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Intent intent = new Intent(getApplicationContext(), NoteActivity.class);
            intent.putExtra(NoteActivity.EXTRA_WEAR, true);
            intent.putExtra(NoteActivity.EXTRA_NOTE, note);
            TaskStackBuilder
                    .create(getApplicationContext())
                    .addNextIntent(home)
                    .addNextIntent(intent)
                    .startActivities();
        } else if (FETCH_DATA.equals(event.getPath())) {
            new WearDataThread(this).start();
        } else if (NEW_NOTE.equals(event.getPath())) {
            Intent intent = new Intent(this, ExternalNoteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEND);
            intent.addCategory(ExternalNoteActivity.CATEGORY_VOICE);
            intent.putExtra(Intent.EXTRA_TEXT, new String(event.getData()));
            startActivity(intent);
        } else if (DELETE_NOTE.equals(event.getPath())) {
            long id = Long.parseLong(new String(event.getData()));
            Note note = DAO.getNote(this, id);
            if (note != null) DAO.deleteNote(note, this);
            new WearDataThread(this).start();
            EventBus.getDefault().post(new RefreshEvent());
        } else {
            super.onMessageReceived(event);
        }
    }

}
