package com.lextech.androiddemo.activity;

import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lextech.androiddemo.R;
import com.lextech.demolibrary.Note;
import icepick.State;

/**
 * User: geoffpowell
 * Date: 12/1/15
 */
public class MapActivity extends BaseActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    public static final String EXTRA_NOTE = "note";

    @State
    private
    Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        contentView = R.layout.map_activity;
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        super.onLayoutInflated(watchViewStub);
        note = getIntent().getParcelableExtra(EXTRA_NOTE);
        dismissOverlayView.setIntroText("Hold Map to exit");
        dismissOverlayView.showIntroIfNecessary();
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng latLng = new LatLng(note.getLatitude(), note.getLongitude());
        map.addMarker(new MarkerOptions().position(latLng)
                .title(note.getTitle()));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        map.addMarker(new MarkerOptions().position(latLng));
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);

        map.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        dismissOverlayView.show();
    }
}
