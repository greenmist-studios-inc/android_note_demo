package com.greenmiststudios.androiddemo.adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.WearDataThread;
import com.greenmiststudios.androiddemo.activity.BaseActivity;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.demolibrary.Note;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> implements View.OnTouchListener {

    private List<Note> notes;
    private View.OnClickListener listener;
    private final BaseActivity activity;
    private final DragDropCallback callback;
    private final LongClickCallback longClickCallback;
    private boolean dragStarted;
    private List<Long> notesSelected = new ArrayList<>();

    public NotesAdapter(BaseActivity activity) {
        setHasStableIds(true);
        callback = new DragDropCallback();
        longClickCallback = new LongClickCallback();
        this.activity = activity;
    }

    public void setNotes(List<Note> notes) {
        new WearDataThread(activity).start();
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public DragDropCallback getCallback() {
        return callback;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getID();
    }

    public Note getItem(int position) {
        if (position > notes.size()) return notes.get(notes.size());
        if (position < 0) return notes.get(0);
        return notes.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_note, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = notes.get(position);

        if (note.imageExists()) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(note.getImagePath())
                    .dontTransform()
                    .into(holder.imageView);
            holder.title.setBackgroundColor(holder.title.getResources().getColor(R.color.overlay));
            holder.titleFrame.setBackgroundColor(Color.TRANSPARENT);
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.title.setBackgroundColor(Color.TRANSPARENT);
            holder.titleFrame.setBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
        }

        holder.title.setText(note.getTitle());
        holder.note.setText(note.getNote());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.card.setForeground(new ColorDrawable(activity.getResources().getColor(notesSelected.contains(note.getID()) ? R.color.overlay2 : android.R.color.transparent)));
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        @Bind(R.id.image)
        public ImageView imageView;
        @Bind(R.id.title)
        public TextView title;
        @Bind(R.id.note)
        public TextView note;
        @Bind(R.id.title_frame)
        public FrameLayout titleFrame;
        @Bind(R.id.card)
        public CardView card;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setHapticFeedbackEnabled(true);
            card.setLongClickable(true);
            card.setOnLongClickListener(this);
            if (listener == null) return;
            card.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (longClickCallback.isCreated()) {
                Note note = getItem(getAdapterPosition());
                notifyItemChanged(this.getAdapterPosition());
                if (notesSelected.contains(note.getID())) {
                    notesSelected.remove(note.getID());
                } else {
                    notesSelected.add(note.getID());
                }
                if (notesSelected.size() == 0) longClickCallback.finishAction();
                else longClickCallback.refreshTitle();
            } else {
                listener.onClick(v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (!longClickCallback.isCreated()) {
                notesSelected.add(NotesAdapter.this.getItemId(getAdapterPosition()));
                notifyItemChanged(getAdapterPosition());
                activity.getToolbar().startActionMode(longClickCallback);
            }
            return true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if ((MotionEventCompat.getActionMasked(event) ==
                MotionEvent.ACTION_UP || MotionEventCompat.getActionMasked(event) ==
                MotionEvent.ACTION_CANCEL) && dragStarted) {
            for (Note note : notes) {
                note.setNoteOrder(notes.indexOf(note));
            }
            DAO.save(notes, activity);
            new WearDataThread(activity).start();
            dragStarted = false;
        }
        return false;
    }

    public class LongClickCallback implements android.view.ActionMode.Callback {

        private boolean created;
        private ActionMode actionMode;

        public boolean isCreated() {
            return created;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            activity.getMenuInflater().inflate(R.menu.action_menu, menu);
            created = true;
            actionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            refreshTitle();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete) {
                DAO.deleteNotes(notesSelected, activity);
                setNotes(DAO.getNotes(activity));
                finishAction();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            created = false;
            notesSelected.clear();
            notifyDataSetChanged();
        }

        public void finishAction() {
            if (actionMode != null) actionMode.finish();
        }

        public void refreshTitle() {
            if (actionMode == null) return;
            actionMode.setTitle(activity.getResources().getQuantityString(R.plurals.notes_selected, notesSelected.size(), notesSelected.size()));
        }
    }

    public class DragDropCallback extends ItemTouchHelper.Callback {

        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Log.d("TAG", "OnMove");
            Collections.swap(notes, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            longClickCallback.finishAction();
            dragStarted = true;
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

    }
}
