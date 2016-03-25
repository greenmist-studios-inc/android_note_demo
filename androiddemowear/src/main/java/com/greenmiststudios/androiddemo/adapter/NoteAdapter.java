package com.greenmiststudios.androiddemo.adapter;

import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.view.WearableFrameLayout;
import com.greenmiststudios.demolibrary.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: geoffpowell
 * Date: 11/26/15
 */
public class NoteAdapter extends WearableListView.Adapter {

    private List<Note> notes = new ArrayList<>();

    public NoteAdapter() {
    }

    public void setNotes(List<Note> notes) {
        Collections.sort(notes, (lhs, rhs) -> lhs.getNoteOrder() - rhs.getNoteOrder());
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return notes.size() + 1;
    }

    public Note getNote(int position) {
        if (position > notes.size() || position <= 0) return null;
        return notes.get(position - 1);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        final ViewHolder viewHolder = (ViewHolder) holder;
        Note note = getNote(position);
        if (note == null) {
            ((WearableFrameLayout) viewHolder.itemView).setSelectedColor(
                    viewHolder.itemView.getResources().getColor(R.color.green));
            viewHolder.title.setText(R.string.add_note);
            viewHolder.title.setGravity(Gravity.CENTER_VERTICAL);
            viewHolder.note.setVisibility(View.GONE);
            viewHolder.title.setPadding(0,0,0,0);
            viewHolder.image.setImageDrawable(viewHolder.image.getResources().getDrawable(R.drawable.plus));
        } else {
            viewHolder.title.setText(note.getTitle());
            viewHolder.note.setText(note.getNote());
        }
    }

    public class ViewHolder extends WearableListView.ViewHolder {

        @Bind(R.id.title)
        public TextView title;

        @Bind(R.id.image)
        public CircledImageView image;

        @Bind(R.id.note)
        public TextView note;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
