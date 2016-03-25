package com.lextech.androiddemo.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.lextech.androiddemo.event.RefreshDataEvent;
import com.lextech.demolibrary.Note;
import com.lextech.demolibrary.NoteAction;
import de.greenrobot.event.EventBus;
import nl.qbusict.cupboard.DatabaseCompartment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * User: geoffpowell
 * Date: 11/10/15
 */
public class DAO {

    public static void save(Object o, Context c) {
        DB db = new DB(c, true);
        db.get().put(o);
        db.close();
        EventBus.getDefault().post(new RefreshDataEvent(false));
    }

    public static List<Note> getNotes(Context c) {
        List<Note> notes;
        DB db = new DB(c, true);
        notes = new ArrayList<>(db.get().query(Note.class).orderBy("noteOrder").list());
        db.close();
        return notes;
    }

    public static Note getNote(Context c, long id) {
        DB db = new DB(c, true);
        Note note = db.get().query(Note.class).byId(id).get();
        db.close();
        return note;
    }

    public static void save(List<Note> notes, Context c) {
        DB db = new DB(c, true);
        db.get().put(notes);
        db.close();
        EventBus.getDefault().post(new RefreshDataEvent(false));
    }

    public static void deleteNote(Note note, Context c) {
        if (note.imageExists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(note.getImagePath()).delete();
        }
        DB db = new DB(c, true);
        db.get().delete(NoteAction.class, "noteID = ?", "" + note.getID());
        db.get().delete(note);
        db.close();
        EventBus.getDefault().post(new RefreshDataEvent(true));
    }

    public static void deleteNotes(List<Long> ids, Context c) {
        DB db = new DB(c, true);
        List<Note> notes = new ArrayList<>(db.get().query(Note.class).withSelection("_id IN (" + TextUtils.join(",", ids) + ")").list());
        for (Note note : notes) if (note.imageExists()) {
            //noinspection ResultOfMethodCallIgnored
            new File(note.getImagePath()).delete();
        }
        db.get().delete(Note.class, "_id IN (" + TextUtils.join(",", ids) + ")");
        db.close();
    }

    public static List<Note> getLastActiveNotes(Context c) {
        DB db = new DB(c, true);
        Cursor cursor = db.db.rawQuery("SELECT * FROM Note note WHERE note._id IN (SELECT noteID FROM NoteAction na ORDER BY na.noteID DESC LIMIT 10)", new String[]{});
        List<Note> notes = new ArrayList<>();
        while (cursor.moveToNext()) {
            Note note = Note.from(cursor);
            notes.add(note);
        }
        cursor.close();
        db.close();
        return notes;
    }

    public static void save(NoteAction o, Context c) {
        DB db = new DB(c, true);
        db.get().put(o);
        Cursor cursor = db.db.rawQuery("DELETE FROM NoteAction where _id NOT IN (SELECT _id from NoteAction ORDER BY _id DESC LIMIT 10)", new String[]{});
        cursor.moveToLast();
        cursor.close();
        db.close();
    }

    /**
     * Helper class to keep instance of SQLiteDatabase since
     * DatabaseCompartment does not have a getDB() method to close it.
     */
    private static class DB {

        private final SQLiteDatabase db;
        private final DatabaseCompartment dc;

        private DB(Context c) {
            this(c, false);
        }

        private DB(Context c, boolean write) {
            if (write) {
                this.db = new CupboardSQLiteOpenHelper(c).getWritableDatabase();
            } else {
                this.db = new CupboardSQLiteOpenHelper(c).getReadableDatabase();
            }
            dc = cupboard().withDatabase(db);
        }

        public DatabaseCompartment get() {
            return dc;
        }

        public void close() {
            db.close();
        }

    }
}