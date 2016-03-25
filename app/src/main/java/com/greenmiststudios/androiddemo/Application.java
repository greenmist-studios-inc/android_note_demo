package com.greenmiststudios.androiddemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.greenmiststudios.androiddemo.helper.BitmapCache;
import com.greenmiststudios.androiddemo.service.FloatingNoteService;
import de.greenrobot.event.EventBus;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;

import static com.greenmiststudios.androiddemo.helper.PreferenceManager.*;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class Application extends android.app.Application implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static BitmapCache cache;
    private static GoogleApiClient googleApiClient;
    public static int activityOpenCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        CupboardFactory.setCupboard(new CupboardBuilder().useAnnotations().build());
        cache = new BitmapCache();
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApiIfAvailable(Wearable.API)
                .build();
        googleApiClient.connect();
        Wearable.MessageApi.addListener(getGoogleApiClient(), messageEvent -> Log.d("APP", messageEvent.toString()));

        EventBus.builder()
                .sendNoSubscriberEvent(false)
                .eventInheritance(false)
                .installDefaultEventBus();


        if (getBooleanValue(this, KEY_FLOATING_NOTES) || (Build.VERSION.SDK_INT < 23 && !containsKey(this, KEY_FLOATING_NOTES))) {
            set(this, KEY_FLOATING_NOTES, true);
            startService(new Intent(this, FloatingNoteService.class));
        }

    }

    @Override
    public void onTerminate() {
        if (googleApiClient != null && googleApiClient.isConnected()) googleApiClient.disconnect();
        super.onTerminate();
    }

    public static BitmapCache getCache() {
        if (cache == null) cache = new BitmapCache();
        return cache;
    }

    public static GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Application", "Google services API Connected");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(3000);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(100);
        LocationServices.FusedLocationApi.requestLocationUpdates(getGoogleApiClient(), locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LocationServices.FusedLocationApi.removeLocationUpdates(getGoogleApiClient(), this);
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("Application", "Google services API Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Application", "Google services API Connection Failed");
    }


}
