package com.greenmiststudios.androiddemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;
import butterknife.Bind;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.adapter.NoteAdapter;
import com.greenmiststudios.demolibrary.Note;
import com.greenmiststudios.demolibrary.NoteConverter;

import java.util.List;

/**
 * User: geoffpowell
 * Date: 11/25/15
 */
public class HomeActivity extends BaseActivity implements WearableListView.ClickListener {

    @Bind(R.id.list) WearableListView listView;

    @Bind(R.id.title) TextView title;

    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.home_activity;
        super.onCreate(savedInstanceState);
        watchViewStub.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            watchViewStub.onApplyWindowInsets(windowInsets);
            if (windowInsets.isRound()) {
                title.setPadding(16, 32, 16, 8);
                title.invalidate();
            }
            return windowInsets;
        });
    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        super.onLayoutInflated(watchViewStub);
        adapter = new NoteAdapter();
        Note note = new Note("Location", "note", null);
        note.setLongitude(41.878114);
        note.setLatitude(-87.629798);
        listView.setAdapter(adapter);
        listView.scrollToPosition(0);
        listView.setClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        updateNotes();
        Wearable.DataApi.addListener(googleApiClient, dataEventBuffer -> {
            for (int i = 0; i < dataEventBuffer.getCount(); i++) {
                Log.d("Note", "data changed" + dataEventBuffer.get(i).getDataItem().getUri().toString());
                setNotesFromItem(dataEventBuffer.get(i).getDataItem());
            }
        });
    }

    private void updateNotes() {
        Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(dataItems -> {
            if (dataItems.getStatus().isSuccess() || dataItems.getCount() == 0)
                sendMessage("/FetchData", "".getBytes(), null);
            for (DataItem dataItem : dataItems) setNotesFromItem(dataItem);
        });
    }

    private void setNotesFromItem(DataItem dataItem) {
        DataMapItem item = DataMapItem.fromDataItem(dataItem);
        if (item.getUri().getPath().equals(DATA_NOTES)) {
            DataMap dataMap = item.getDataMap();
            List<Note> notes = NoteConverter.fromList(dataMap.getDataMapArrayList(DATA_NOTES));
            adapter.setNotes(notes);
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Note note = adapter.getNote(viewHolder.getAdapterPosition());
        if (viewHolder.getAdapterPosition() == 0) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Whats the note?");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            startActivityForResult(intent, 22);
        } else {
            if (note == null) return;
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra(NoteActivity.EXTRA_NOTE, note);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == 22 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenNote = results.get(0);
            Log.d("VoiceNoteActivity", "Note " + spokenNote + " created.");
            sendMessage(NEW_NOTE, spokenNote.getBytes(), null);
            Intent intent = new Intent(getApplicationContext(), ConfirmationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.message_note_created));
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

}
