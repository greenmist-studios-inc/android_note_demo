package com.lextech.androiddemo.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lextech.demolibrary.Note;
import com.lextech.demolibrary.NoteAction;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * User: Brent
 * Date: 7/17/15
 */
class CupboardSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;

    static {
        cupboard().register(Note.class);
        cupboard().register(NoteAction.class);
    }

    public CupboardSQLiteOpenHelper(Context context) {
        super(context, "demo.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
