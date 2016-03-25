package com.lextech.demolibrary;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.google.android.gms.wearable.Asset;
import nl.qbusict.cupboard.annotation.Ignore;

import java.io.File;
import java.util.Date;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class Note implements Parcelable {

    private Long _id;
    private String title;
    private String note;
    private String imagePath;
    private Double longitude;
    private Double latitude;
    private Date created;
    private Integer noteOrder;

    @Ignore
    private transient Asset asset;

    public Note() {
    }

    public Note(String title, String note, String imagePath) {
        this.title = title;
        this.note = note;
        this.imagePath = imagePath;
        created = new Date();
    }

    public Note(String title, String note, String imagePath, double longitude, double latitude) {
        this.title = title;
        this.note = note;
        this.imagePath = imagePath;
        this.longitude = longitude;
        this.latitude = latitude;
        created = new Date();
    }

    protected Note(Parcel in) {
        long id = in.readLong();
        _id = id == 0 ? null : id;
        title = in.readString();
        note = in.readString();
        imagePath = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        created = new Date(in.readLong());
        noteOrder = in.readInt();
        asset = in.readParcelable(Asset.class.getClassLoader());
    }

    public static Note from(Cursor cursor) {
        Note note = new Note(
                cursor.getString(cursor.getColumnIndex("title")),
                cursor.getString(cursor.getColumnIndex("note")),
                cursor.getString(cursor.getColumnIndex("imagePath")),
                cursor.getLong(cursor.getColumnIndex("longitude")),
                cursor.getLong(cursor.getColumnIndex("latitude")));
        note._id = cursor.getLong(cursor.getColumnIndex("_id"));
        note.created = new Date(cursor.getLong(cursor.getColumnIndex("created")));
        note.noteOrder = cursor.getInt(cursor.getColumnIndex("noteOrder"));
        return note;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    @Override
    public int describeContents() {
        return 3;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getID());
        dest.writeString(getTitle());
        dest.writeString(getNote());
        dest.writeString(getImagePath());
        dest.writeDouble(getLatitude() == null ? 0 : getLatitude());
        dest.writeDouble(getLongitude() == null ? 0 : getLongitude());
        dest.writeLong(created.getTime());
        dest.writeInt(noteOrder == null ? 0 : noteOrder);
        dest.writeParcelable(asset, 0);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getID() {
        return _id == null ? 0 : _id;
    }

    public void setID(Long _id) {
        this._id = _id;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public int getNoteOrder() {
        return noteOrder;
    }

    public void setNoteOrder(int noteOrder) {
        this.noteOrder = noteOrder;
    }

    public Integer getNoteOrderObject() {
        return noteOrder;
    }

    public Asset getAsset() {
        return asset;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setNoteOrder(Integer noteOrder) {
        this.noteOrder = noteOrder;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public boolean imageExists() {
        return !TextUtils.isEmpty(imagePath) && (new File(imagePath).exists() || asset != null);
    }

    public boolean locationExists() {
        return getLatitude() != null && getLongitude() != null && getLatitude() != 0 && getLongitude() != 0;
    }


    @Override
    public String toString() {
        return "Note{" +
                "Title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }

}
