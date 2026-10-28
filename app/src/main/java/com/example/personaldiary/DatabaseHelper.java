package com.example.personaldiary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "PersonalDiary.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE diary (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "date TEXT NOT NULL)";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS diary");
        onCreate(db);
    }

    public boolean addEntry(String title, String content, String date) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("date", date);

        return db.insert("diary", null, values) != -1;
    }

    public Cursor getAllEntries() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM diary ORDER BY id DESC",
                null
        );
    }

    public boolean updateEntry(int id, String title, String content, String date) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("date", date);

        return db.update(
                "diary",
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public boolean deleteEntry(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "diary",
                "id = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public Cursor searchEntries(String keyword) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM diary WHERE title LIKE ? OR content LIKE ? ORDER BY id DESC",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"}
        );
    }
}
