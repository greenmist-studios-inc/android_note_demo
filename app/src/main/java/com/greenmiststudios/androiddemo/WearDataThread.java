package com.greenmiststudios.androiddemo;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.wearable.*;
import com.greenmiststudios.androiddemo.helper.DAO;
import com.greenmiststudios.androiddemo.service.WearableService;
import com.greenmiststudios.demolibrary.NoteConverter;

/**
 * User: geoffpowell
 * Date: 11/26/15
 */
public class WearDataThread extends Thread {
    private static final String TAG = "WearDataThread";

    private final String path;
    private DataMap dataMap;
    private final Context context;

    public WearDataThread(Context context) {
        path = WearableService.DATA_NOTES;
        this.context = context;
    }

    public void run() {
        dataMap = new DataMap();
        dataMap.putDataMapArrayList(WearableService.DATA_NOTES, NoteConverter.toList(DAO.getNotes(context), context));
        if (Application.getGoogleApiClient() == null || !Application.getGoogleApiClient().isConnected()) return;
        PutDataMapRequest putDMR = PutDataMapRequest.create(path);
        putDMR.getDataMap().putAll(dataMap);
        PutDataRequest request = putDMR.asPutDataRequest();
        request.setUrgent();
        DataApi.DataItemResult result = Wearable.DataApi.putDataItem(Application.getGoogleApiClient(), request).await();

        if (result.getStatus().isSuccess()) Log.v(TAG, "DataMap: " + dataMap + " sent successfully to data layer ");
    }
}
