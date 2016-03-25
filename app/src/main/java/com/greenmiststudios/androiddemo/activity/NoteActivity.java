package com.greenmiststudios.androiddemo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Transition;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.greenmiststudios.androiddemo.Application;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.WearDataThread;
import com.greenmiststudios.androiddemo.helper.AnimationHelper;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.androiddemo.helper.ImageHelper;
import com.greenmiststudios.androiddemo.helper.PermissionHelper;
import com.greenmiststudios.androiddemo.helper.listener.SimpleAnimationListener;
import com.greenmiststudios.androiddemo.helper.listener.SimpleTransitionListener;
import com.greenmiststudios.demolibrary.Note;
import com.greenmiststudios.demolibrary.NoteAction;
import icepick.State;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date: 11/10/15
 */
public class NoteActivity extends BaseActivity implements GoogleMap.OnMapClickListener {

    public static final String EXTRA_NOTE = "note";
    public static final String EXTRA_WEAR = "wear";
    public static final String EXTRA_APPEND = "append";
    private static final int REQUEST_IMAGE_CODE = 101;

    private Note note;

    @Bind(R.id.image)
    ImageView toolbarImage;
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout toolbarLayout;
    @Bind(R.id.appbar)
    AppBarLayout appBarLayout;
    @Bind(R.id.note)
    TextView noteView;
    @Bind(R.id.edit_note)
    EditText editNote;
    @Bind(R.id.edit_title)
    EditText editTitle;
    @Bind(R.id.edit_photo)
    ImageView editPhoto;
    @Bind(R.id.edit)
    FloatingActionButton edit;
    @Bind(R.id.map)
    MapView mapView;

    @State boolean editing;
    @State boolean started;
    @State Bitmap thumb;
    @State
    boolean photoChanged;
    private Palette palette;
    @State File tempGetFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.activity_note;
        super.onCreate(savedInstanceState);
        final Fade fade = new Fade();
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        fade.excludeTarget(R.id.image, true);
        getWindow().getEnterTransition().excludeTarget(android.R.id.navigationBarBackground, true);
        getWindow().setEnterTransition(fade);
        getWindow().setExitTransition(fade);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        if (note == null) finish();
        DAO.save(new NoteAction(note), this);
        toolbarLayout.setTitle(note.getTitle());
        editTitle.setText(note.getTitle());

        if (getIntent().hasExtra(EXTRA_APPEND)) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_note_updated), Snackbar.LENGTH_LONG).show();
        }

        //Use previous loaded image as a animation thumbnail
        if (note.imageExists()) {
            Bitmap bitmap = Application.getCache().getBitmap();
            if (bitmap != null) {
                thumb = bitmap;
                toolbarImage.setImageBitmap(bitmap);
                palette = new Palette.Builder(bitmap).generate();
                updatePallete();
            } else {
                new Thread(() -> {
                    try {
                        final Bitmap bmp = Glide.with(NoteActivity.this)
                                .load(note.getImagePath())
                                .asBitmap()
                                .dontTransform()
                                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                                .get();
                        if (bmp == null) return;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toolbarImage.setImageBitmap(bmp);
                                palette = new Palette.Builder(bmp).generate();
                                updatePallete();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            appBarLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        noteView.setText(note.getNote());
        editNote.setText(note.getNote());
        edit.setVisibility(started ? View.VISIBLE : View.GONE);

        if (getIntent().hasExtra(EXTRA_WEAR)) {
            edit.setVisibility(View.VISIBLE);
            getWindow().getEnterTransition().addListener(new EnterListener());
        } else {
            getWindow().getSharedElementEnterTransition().addListener(new EnterListener());
        }

        if (!note.locationExists()) return;
        mapView.onCreate(savedInstanceState);
        mapView.setVisibility(View.VISIBLE);
        mapView.getMapAsync(googleMap -> {
            LatLng latLng = new LatLng(note.getLatitude(), note.getLongitude());
            googleMap.getUiSettings().setMapToolbarEnabled(false);
            googleMap.getUiSettings().setZoomGesturesEnabled(false);
            googleMap.getUiSettings().setRotateGesturesEnabled(false);
            googleMap.getUiSettings().setTiltGesturesEnabled(false);
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .position(latLng));
            googleMap.setOnMapClickListener(NoteActivity.this);
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Uri gmmIntentUri = Uri.parse("geo:" + note.getLatitude() + "," + note.getLongitude() + "?z=10");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void updatePallete() {
        if (palette == null || palette.getLightVibrantSwatch() == null || palette.getDarkVibrantSwatch() == null)
            return;
        toolbarLayout.setCollapsedTitleTextColor(palette.getDarkVibrantSwatch().getTitleTextColor());
        toolbarLayout.setExpandedTitleColor(palette.getDarkVibrantSwatch().getTitleTextColor());
        toolbarLayout.setContentScrimColor(palette.getDarkVibrantSwatch().getRgb());
        edit.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(getResources().getColor(R.color.colorAccent))));
    }

    @Override
    protected void onResume() {
        if (mapView != null && note.locationExists()) mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mapView != null && note.locationExists()) mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mapView != null && note.locationExists()) mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        if (mapView != null && note.locationExists()) mapView.onLowMemory();
        super.onLowMemory();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.edit_photo)
    public void editPhoto() {
        if (!PermissionHelper.checkPermissions(this, R.string.error_permissions, Manifest.permission.READ_EXTERNAL_STORAGE)) return;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        tempGetFilePath = note.getImagePath() == null ? new File(Environment.getExternalStorageDirectory(), "IMG_" + timeStamp + ".jpg") : new File(note.getImagePath());
        startActivityForResult(ImageHelper.getImageIntents(tempGetFilePath, this), REQUEST_IMAGE_CODE);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.edit)
    public void editNote() {
        if (edit.getAnimation() != null && !edit.getAnimation().hasEnded()) return;
        if (editing) {
            edit.startAnimation(AnimationHelper.createScaleInOutColor(edit, palette != null && palette.getVibrantSwatch() != null ? palette.getVibrantSwatch().getRgb() : getResources().getColor(R.color.colorAccent), this, true));
            editNote.setVisibility(View.GONE);
            noteView.setVisibility(View.VISIBLE);
            editTitle.setVisibility(View.GONE);
            toolbarLayout.setTitleEnabled(true);
            note.setNote(editNote.getText().toString());
            noteView.setText(note.getNote());
            note.setTitle(editTitle.getText().toString());
            toolbarLayout.setTitle(note.getTitle());
            DAO.save(note, this);
            new WearDataThread(this).start();
            final InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(noteView.getWindowToken(), 0);
        } else {
            appBarLayout.setExpanded(true);
            editNote.setVisibility(View.VISIBLE);
            editTitle.setVisibility(View.VISIBLE);
            noteView.setVisibility(View.GONE);
            toolbarLayout.setTitleEnabled(false);
            toolbar.setTitle("");
            edit.startAnimation(AnimationHelper.createScaleInOutColor(edit, getResources().getColor(R.color.green), this, false));
        }
        editing = !editing;
        Animation animation = AnimationUtils.loadAnimation(this, !editing ? R.anim.fade_out : R.anim.fade_in);
        SimpleAnimationListener listener = new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (editing) editPhoto.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                editPhoto.setVisibility(editing ? View.VISIBLE : View.GONE);
                super.onAnimationEnd(animation);
            }
        };
        animation.setAnimationListener(listener);
        editPhoto.startAnimation(animation);

    }

    @SuppressWarnings("unused")
    @OnClick(R.id.trash)
    public void delete() {
        DAO.deleteNote(note, this);
        finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (edit.getAnimation() == null || edit.getAnimation().hasEnded()) {
                finishAfterTransition();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finishAfterTransition() {
        if (note.imageExists()) {
            if (photoChanged) thumb = ((GlideBitmapDrawable) toolbarImage.getDrawable()).getBitmap();
            Application.getCache().setBitmap(thumb);
            appBarLayout.setExpanded(true, true);
            if (thumb != null) toolbarImage.setImageBitmap(thumb);
        }
        Animation animation = AnimationUtils.loadAnimation(NoteActivity.this, R.anim.scale_in);
        animation.setDuration(500);
        animation.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                edit.setVisibility(View.GONE);
                NoteActivity.super.finishAfterTransition();
                super.onAnimationEnd(animation);
            }
        });
        edit.startAnimation(animation);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_IMAGE_CODE:
                File file;
                if (data == null && tempGetFilePath.exists()) file = tempGetFilePath;
                else file = ImageHelper.getImage(this, data);
                if (file == null) return;
                if (!TextUtils.isEmpty(note.getImagePath())) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(note.getImagePath()).delete();
                }
                File saved = ImageHelper.saveFile(this, file.getPath());
                if (saved == null) return;
                note.setImagePath(saved.getPath());
                photoChanged = true;
                Glide.with(this)
                        .load(note.getImagePath())
                        .dontTransform()
                        .into(toolbarImage);
                DAO.save(note, this);
                break;
        }
    }


    private class EnterListener extends SimpleTransitionListener {

        @Override
        public void onTransitionEnd(Transition transition) {
            started = true;
            edit.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(NoteActivity.this, R.anim.scale_out);
            edit.startAnimation(animation);
            transition.removeListener(this);
            //Set toolbar image to full image
            if (!note.imageExists() || getIntent().hasExtra(EXTRA_WEAR)) return;
            Glide.with(NoteActivity.this)
                    .load(note.getImagePath())
                    .dontTransform()
                    .dontAnimate()
                    .placeholder(toolbarImage.getDrawable())
                    .into(toolbarImage);
        }
    }
}