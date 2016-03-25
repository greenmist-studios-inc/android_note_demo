package com.greenmiststudios.androiddemo.activity;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.androiddemo.helper.ImageHelper;
import com.greenmiststudios.androiddemo.helper.PermissionHelper;
import com.greenmiststudios.androiddemo.helper.listener.SimpleAnimatorListener;
import com.greenmiststudios.demolibrary.Note;
import com.greenmiststudios.demolibrary.NoteAction;
import icepick.State;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.greenmiststudios.androiddemo.Application.getGoogleApiClient;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class NewNoteActivity extends BaseActivity {

    public static final String EXTRA_FAB_LOCATION = "fab_location";
    private static final String EXTRA_ANIM = "anim";
    private static final int REQUEST_IMAGE_CODE = 101;

    @Bind(android.R.id.content) View root;
    @Bind(R.id.save) FloatingActionButton save;
    @Bind(R.id.add) FloatingActionButton addImage;
    @Bind(R.id.title) EditText title;
    @Bind(R.id.note) EditText note;
    @Bind(R.id.image) ImageView imageView;
    @Bind(R.id.add_location) Button button;
    @State String imagePath;
    @State File tempGetFilePath;
    @State int[] location;
    @State Location noteLocation;

    @State boolean closing;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.activity_new;
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        title.setHintTextColor(getResources().getColor(R.color.white));
        note.setHintTextColor(getResources().getColor(R.color.black));
        location = getIntent().getIntArrayExtra(EXTRA_FAB_LOCATION);
        createRevealAnimation();
        if (imagePath != null) imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        if (noteLocation != null) getCurrentLocation();
    }

    private int getLargestUnit() {
        return Math.max(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
    }

    @OnClick(R.id.save)
    public void saveNote() {
        if (closing) return;
        if (TextUtils.isEmpty(title.getText().toString()) || TextUtils.isEmpty(note.getText().toString())) {
            new AlertDialog.Builder(this, R.style.AppTheme_Alert)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_data)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }
        Note newNote = new Note(title.getText().toString(), note.getText().toString(), imagePath);
        if (noteLocation != null) {
            newNote.setLatitude(noteLocation.getLatitude());
            newNote.setLongitude(noteLocation.getLongitude());
        }
        newNote.setNoteOrder(-1);
        DAO.save(newNote, this);
        DAO.save(new NoteAction(newNote), this);
        finishAfterTransition();
    }

    @OnClick(R.id.add)
    public void addImage() {
        if (!PermissionHelper.checkPermissions(this, R.string.error_permissions, Manifest.permission.READ_EXTERNAL_STORAGE)) return;
        startImageChooser();
    }

    @OnClick(R.id.add_location)
    public void addLocation() {
        if (!PermissionHelper.checkPermissions(this, R.string.error_location, Manifest.permission.ACCESS_FINE_LOCATION)) return;
        if (!PermissionHelper.checkPermissions(this, R.string.error_location, Manifest.permission.ACCESS_COARSE_LOCATION)) return;
        getCurrentLocation();
    }

    @SuppressWarnings("ResourceType")
    private void getCurrentLocation() {
        final LocationListener listener = new LocationListener();
        if (getGoogleApiClient() != null) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(getGoogleApiClient());
            if (location == null) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setMaxWaitTime(3000);
                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(100);
                LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), locationRequest, listener)
                    .setResultCallback(new ResolvingResultCallbacks<Status>(this, 0) {
                        @Override
                        public void onSuccess(Status status) {}

                        @Override
                        public void onUnresolvableFailure(Status status) {
                            Snackbar.make(findViewById(android.R.id.content), "Location could not be found.", Snackbar.LENGTH_LONG);
                        }
                    });
                button.setText(R.string.getting_location);
            } else {
                listener.onLocationChanged(location);
            }
            button.setBackground(null);
            button.setEnabled(false);
        } else {
            new AlertDialog.Builder(this, R.style.AppTheme_Alert)
                    .setTitle(R.string.error)
                    .setMessage(R.string.error_gs)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            button.setVisibility(View.GONE);
        }
    }

    private void startImageChooser() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        tempGetFilePath = new File(Environment.getExternalStorageDirectory(), "IMG_" + timeStamp + ".jpg");
        startActivityForResult(ImageHelper.getImageIntents(tempGetFilePath, this), REQUEST_IMAGE_CODE);
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
                if (!TextUtils.isEmpty(imagePath)) {
                    //noinspection ResultOfMethodCallIgnored
                    new File(imagePath).delete();
                }
                File saved = ImageHelper.saveFile(this, file.getPath());
                if (saved == null) return;
                imagePath = saved.getPath();
                Glide.with(this).load(imagePath).into(imageView);
                break;
        }
    }

    @Override
    public void finishAfterTransition() {
        if (!closing) createConcealAnimation();
    }

    private void createRevealAnimation() {
        if (location != null && location.length == 2 && getIntent().hasExtra(EXTRA_ANIM)) return;
        root.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                final View.OnAttachStateChangeListener onAttachStateChangeListener = this;
                final Animator animator = ViewAnimationUtils.createCircularReveal(root, location[0], location[1], 0, getLargestUnit());
                animator.setDuration(500);
                animator.addListener(new SimpleAnimatorListener() {
                    public void onAnimationEnd(Animator animation) {
                        root.removeOnAttachStateChangeListener(onAttachStateChangeListener);
                    }
                });
                animator.start();
                getIntent().putExtra(EXTRA_ANIM, true);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionHelper.REQUEST_CODE: {
                if (grantResults.length <= 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) return;
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissions[0])) {
                    getCurrentLocation();
                } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permissions[0])) {
                    startImageChooser();
                }
            }
        }
    }

    private void createConcealAnimation() {
        closing = true;
        //Close keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
        final Animator animator = ViewAnimationUtils.createCircularReveal(root, location[0], location[1], getLargestUnit(), 0);
        animator.setDuration(500);
        animator.addListener(new SimpleAnimatorListener() {
            public void onAnimationEnd(Animator animation) {
                root.setVisibility(View.GONE);
                NewNoteActivity.super.finishAfterTransition();
                closing = false;
            }
        });
        animator.start();
    }

    @SuppressWarnings("ResourceType")
    private class LocationListener implements com.google.android.gms.location.LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), this);
            if (location == null) return;
            noteLocation = location;
            button.setText(R.string.message_location_added);
            button.setBackground(null);
        }

    }
}
