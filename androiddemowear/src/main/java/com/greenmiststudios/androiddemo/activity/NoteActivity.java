package com.greenmiststudios.androiddemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.Wearable;
import com.greenmiststudios.androiddemo.R;
import com.greenmiststudios.demolibrary.Note;
import icepick.State;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * User: geoffpowell
 * Date: 11/30/15
 */
public class NoteActivity extends BaseActivity {

    public static final String EXTRA_NOTE = "note";

    @Bind(R.id.title) TextView title;
    @Bind(R.id.note) TextView noteView;
    @Bind(R.id.image) ImageView image;
    @Bind(R.id.view_location) View location;
    @Bind(R.id.button_layout) View buttonLayout;
    @Bind(R.id.location_image) CircledImageView locationImage;
    @Bind(R.id.phone_image) CircledImageView photoImage;
    @Bind(R.id.delete) CircledImageView delete;
    @Bind(R.id.title_background) View titleBackground;
    @State Note note;
    @State Palette palette;

    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.note_activity;
        super.onCreate(savedInstanceState);
        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        if (note == null) {
            finish();
            return;
        }
        if (!note.imageExists()) return;
        new Thread(() -> {
            bitmap = loadBitmapFromAsset(note.getAsset());
            updateUI();
        }).start();
    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        super.onLayoutInflated(watchViewStub);
        noteView.setText(note.getNote());
        title.setText(note.getTitle());
        if (note.locationExists()) location.setVisibility(View.VISIBLE);
        if (note.imageExists()) updateUI();
        else {
            titleBackground.setLayoutParams(new BoxInsetLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, title.getHeight()));
            titleBackground.setY(title.getY());
        }
    }

    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        super.onApplyWindowInsets(v, insets);
        if (!isRound) {
            int vert = getResources().getDimensionPixelSize(R.dimen.square_vertical_padding);
            int horiz = getResources().getDimensionPixelSize(R.dimen.square_horizontal_padding);
            title.setPadding(horiz, vert, horiz, vert);
            noteView.setPadding(horiz, vert, horiz, vert);
        }
        return insets;
    }

    private void updateUI() {
        if (bitmap == null) return;
        palette = Palette.from(bitmap).generate();
        runOnUiThread(() -> {
            if (image != null) {
                titleBackground.setLayoutParams(new BoxInsetLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, title.getHeight()));
                titleBackground.setY(title.getY());
                image.setImageBitmap(bitmap);
                titleBackground.setBackgroundColor(getResources().getColor(R.color.overlay));
                updateUIAccentColor();
            }
        });
    }

    private void updateUIAccentColor() {
        if (palette == null) return;
        int color = palette.getMutedColor(getResources().getColor(R.color.colorAccent));
        buttonLayout.setBackgroundColor(color);
        locationImage.setImageTint(color);
        photoImage.setImageTint(color);
        delete.setImageTint(color);
    }

    private Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) throw new IllegalArgumentException("Asset must be non-null");
        ConnectionResult result = googleApiClient.blockingConnect(3000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) return null;
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(googleApiClient, asset).await().getInputStream();
        if (assetInputStream == null) {
            Log.w("NoteActivity", "Requested an unknown Asset.");
            return null;
        }
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @OnClick(R.id.delete_note)
    public void deleteNote() {
        sendMessage(DELETE_NOTE, (note.getID() + "").getBytes(), null);
        finish();
        Intent intent = new Intent(NoteActivity.this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.message_note_deleted));
        startActivity(intent);
    }

    @OnClick(R.id.open_phone)
    public void openOnPhone() {
        Log.d("NoteActivity", "Open on phone");
        sendMessage(OPEN_NOTE, (note.getID() + "").getBytes(), sendMessageResult -> {
            Log.d("RESULT", sendMessageResult.getRequestId() + " " + sendMessageResult.getStatus().getStatusMessage() + sendMessageResult.getStatus().toString());
            if (sendMessageResult.getStatus().isSuccess()) {
                Intent intent = new Intent(NoteActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
                startActivity(intent);
            }
        });
    }

    @OnClick(R.id.view_location)
    public void viewMap() {
        if (!note.locationExists()) return;
        Intent intent = new Intent(this, MapActivity.class);
        Location location = new Location("");
        location.setLongitude(note.getLongitude());
        location.setLatitude(note.getLatitude());
        intent.putExtra(MapActivity.EXTRA_NOTE, note);
        startActivity(intent);
    }
}
