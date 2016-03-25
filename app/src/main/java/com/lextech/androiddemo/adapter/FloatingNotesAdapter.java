package com.lextech.androiddemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.event.FloatingNoteSelectedEvent;
import com.lextech.demolibrary.Note;
import de.greenrobot.event.EventBus;

import java.util.List;

/**
 * User: geoffpowell
 * Date: 12/14/15
 */
public class FloatingNotesAdapter extends BaseAdapter implements View.OnClickListener {

    private List<Note> notes;

    public FloatingNotesAdapter(List<Note> notes) {
        this.notes = notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int position) {
        return notes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_floating_note, parent, false);
        }
        Note note = getItem(position);
        ((TextView) convertView).setText(String.format("%s - %s", note.getTitle(), note.getNote()));
        convertView.setTag(note.getID());
        convertView.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new FloatingNoteSelectedEvent((long)v.getTag()));
    }
}
