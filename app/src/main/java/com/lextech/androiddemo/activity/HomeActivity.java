package com.lextech.androiddemo.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import butterknife.Bind;
import butterknife.OnClick;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.lextech.androiddemo.Application;
import com.lextech.androiddemo.R;
import com.lextech.androiddemo.adapter.NotesAdapter;
import com.lextech.androiddemo.event.FloatingNoteChangedEvent;
import com.lextech.androiddemo.event.RefreshEvent;
import com.lextech.androiddemo.helper.DAO;
import com.lextech.androiddemo.helper.listener.SimpleTransitionListener;
import com.lextech.androiddemo.service.FloatingNoteService;
import com.lextech.demolibrary.Note;

import java.util.List;

import static com.lextech.androiddemo.helper.PreferenceManager.*;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    @Bind(R.id.list) RecyclerView recyclerView;

    @Bind(R.id.fab) FloatingActionButton fab;

    private NotesAdapter adapter;
    private NotesAdapter.ViewHolder selectedHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.activity_home;
        super.onCreate(savedInstanceState);
        hasEvents = true;
        stickyEvents = true;
        getWindow().setEnterTransition(new Slide());
        Fade fade = new Fade();
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setExitTransition(fade);

        adapter = new NotesAdapter(this);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper ith = new ItemTouchHelper(adapter.getCallback());
        ith.attachToRecyclerView(recyclerView);
        recyclerView.setOnTouchListener(adapter);


        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 3, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        Animation animation = AnimationUtils.loadAnimation(HomeActivity.this, R.anim.scale_out);
        animation.setStartOffset(300);
        fab.startAnimation(animation);

        getWindow().getSharedElementReenterTransition().addListener(new SimpleTransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                if (selectedHolder == null) return;
                Bitmap thumb = Application.getCache().getBitmap();
                if (thumb != null) {
                    selectedHolder.imageView.setVisibility(View.VISIBLE);
                    selectedHolder.imageView.setImageBitmap(thumb);
                }
                selectedHolder.title.setTransitionName("");
                if (!adapter.getItem(selectedHolder.getAdapterPosition()).imageExists()) return;
                selectedHolder.title.setAlpha(0);
                selectedHolder.title
                        .animate()
                        .alphaBy(1)
                        .setDuration(250)
                        .start();
            }
        });

        if (Build.VERSION.SDK_INT >= 23 && !containsKey(this, KEY_FLOATING_NOTES)) {
            showFloatingNoteDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DrawableCompat.wrap(menu.getItem(0).getIcon()).setTint(getResources().getColor(getBooleanValue(this, KEY_FLOATING_NOTES) ? R.color.white : R.color.overlay));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.floating_note) {
            if (!showFloatingNoteDialog()) {
                if (Build.VERSION.SDK_INT < 23 || containsKey(this, KEY_FLOATING_NOTES)) {
                    boolean state = getBooleanValue(this, KEY_FLOATING_NOTES);
                    set(this, KEY_FLOATING_NOTES, !state);
                    invalidateOptionsMenu();
                    if (!state) {
                        startService(new Intent(this, FloatingNoteService.class));
                    } else {
                        stopService(new Intent(this, FloatingNoteService.class));
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Note> notes = DAO.getNotes(this);
        adapter.setNotes(notes);
        if (selectedHolder != null) selectedHolder.card.setClickable(true);
    }

    @OnClick(R.id.fab)
    public void createNote(View view) {
        Intent intent = new Intent(this, NewNoteActivity.class);
        int[] location = new int[2];
        view.getLocationInWindow(location);
        location[0] += view.getWidth()/2;
        intent.putExtra(NewNoteActivity.EXTRA_FAB_LOCATION, location);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        selectedHolder = (NotesAdapter.ViewHolder) recyclerView.getChildViewHolder((View)v.getParent());
        selectedHolder.imageView.setTransitionName("cardImage");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                new Pair<>(selectedHolder.imageView, "cardImage"));
        selectedHolder.card.setClickable(false);
        int position = selectedHolder.getAdapterPosition();
        Note note = adapter.getItem(position);
        if (note.imageExists()) {
            Drawable drawable = selectedHolder.imageView.getDrawable();
            if (drawable != null) {
                if (drawable instanceof BitmapDrawable) Application.getCache().setBitmap(((BitmapDrawable)drawable).getBitmap());
                else if (drawable instanceof GlideBitmapDrawable) Application.getCache().setBitmap(((GlideBitmapDrawable)drawable).getBitmap());
            }
        } else {
            selectedHolder.imageView.setVisibility(View.GONE);
        }
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra(NoteActivity.EXTRA_NOTE, adapter.getItem(position));
        startActivity(intent, options.toBundle());
    }

    private boolean showFloatingNoteDialog() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            new AlertDialog.Builder(this, R.style.AppTheme_Alert)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_permission_overlay)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        set(getApplicationContext(), KEY_FLOATING_NOTES, false);
                        startActivity(new Intent(getApplicationContext(), RequestOverlayPermissionActivity.class));
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        set(getApplicationContext(), KEY_FLOATING_NOTES, false);
                        invalidateOptionsMenu();
                    })
                    .show();
            return true;
        }
        return false;
    }

    public void onEventMainThread(@SuppressWarnings("UnusedParameters") RefreshEvent event) {
        adapter.setNotes(DAO.getNotes(this));
    }

    public void onEventMainThread(@SuppressWarnings("UnusedParameters") FloatingNoteChangedEvent event) {
        invalidateOptionsMenu();
    }

}
