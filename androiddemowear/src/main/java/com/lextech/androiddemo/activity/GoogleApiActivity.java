package com.lextech.androiddemo.activity;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Collection;
import java.util.HashSet;

/**
 * User: geoffpowell
 * Date: 12/4/15
 */
public class GoogleApiActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    static final String OPEN_NOTE = "/OpenNote";
    static final String DATA_NOTES = "/DataNotes";
    static final String NEW_NOTE = "/NewNote";
    static final String DELETE_NOTE = "/DeleteNote";

    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApiIfAvailable(Wearable.API)
                .build();
    }

    private Collection<String> getNodes() {
        if (googleApiClient == null || !googleApiClient.isConnected()) return new HashSet<>();
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    void sendMessage(final String path, final byte[] data, final ResultCallback<MessageApi.SendMessageResult> resultCallback) {
        new Thread(() -> {
            for (String node : getNodes()) {
                Log.d("RESULT", "Send message node: " + node);
                PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(googleApiClient, node, path, data);
                if (resultCallback != null) result.setResultCallback(resultCallback);
            }
        }).start();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("BaseActivity", "Google services API Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("BaseActivity", "Google services API Suspended");
        googleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("BaseActivity", "Google services API Connection Failed");
    }

}
