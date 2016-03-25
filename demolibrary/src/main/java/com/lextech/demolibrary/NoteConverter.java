package com.lextech.demolibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: geoffpowell
 * Date: 12/3/15
 */
public class NoteConverter {

    public static List<Note> fromList(List<DataMap> list) {
        List<Note> notes = new ArrayList<>();
        for (DataMap dm : list) notes.add(from(dm));
        return notes;
    }

    public static ArrayList<DataMap> toList(List<Note> list, Context context) {
        ArrayList<DataMap> mapList = new ArrayList<>();
        for (Note n : list) mapList.add(to(n, context));
        return mapList;
    }

    private static DataMap to(Note note, Context context) {
        DataMap dataMap = new DataMap();
        dataMap.putLong("id", note.getID());
        dataMap.putString("title", note.getTitle());
        dataMap.putString("note", note.getNote());
        dataMap.putString("path", note.getImagePath());
        dataMap.putDouble("latitude", note.getLatitude() == null ? 0 : note.getLatitude());
        dataMap.putDouble("longitude", note.getLongitude() == null ? 0 : note.getLongitude());
        dataMap.putLong("created", note.getCreated().getTime());
        dataMap.putInt("noteorder", note.getNoteOrderObject() == null ? 0 : note.getNoteOrder());
        if (!TextUtils.isEmpty(note.getImagePath())) {
            try {
                Bitmap bitmap = Glide.with(context).load(note.getImagePath())
                        .asBitmap()
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                scaleBitmap(bitmap).compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                byteStream.close();
                dataMap.putAsset("bitmap", Asset.createFromBytes(byteStream.toByteArray()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dataMap;
    }

    private static Bitmap scaleBitmap(Bitmap bitmap) {
        int max = 400;
        float ratio = Math.min(
                (float) max / bitmap.getWidth(),
                (float) max / bitmap.getHeight());
        int width = Math.round(ratio * bitmap.getWidth());
        int height = Math.round(ratio * bitmap.getHeight());

        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private static Note from(DataMap dataMap) {
        Note note = new Note();
        note.setID(dataMap.getLong("id"));
        note.setTitle(dataMap.getString("title"));
        note.setNote(dataMap.getString("note"));
        note.setImagePath(dataMap.getString("path"));
        note.setLatitude(dataMap.getDouble("latitude"));
        note.setLongitude(dataMap.getDouble("longitude"));
        note.setCreated(new Date(dataMap.getLong("created")));
        note.setNoteOrder(dataMap.getInt("noteorder"));
        note.setAsset(dataMap.getAsset("bitmap"));
        return note;
    }
}
