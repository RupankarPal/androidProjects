package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// FIX: Added 'extends SQLiteOpenHelper' so database operations work
public class SettingEntity extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AppConfig.db";
    private static final int DATABASE_VERSION = 1;

    // Table and Column names
    public static final String TABLE_SETTINGS = "settings";
    public static final String COLUMN_KEY = "secret_key";
    public static final String COLUMN_VALUE = "secret_value";

    public SettingEntity(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_SETTINGS + " ("
                + COLUMN_KEY + " TEXT PRIMARY KEY, "
                + COLUMN_VALUE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    // --- WRITE OPERATION ---
    public void insertSecret(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_KEY, key);
        values.put(COLUMN_VALUE, value);

        // Conflict resolution replaces the old value if the key somehow exists
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // --- READ OPERATION ---
    public String getSecretValue(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        String value = null;

        Cursor cursor = db.query(TABLE_SETTINGS,
                new String[]{COLUMN_VALUE},
                COLUMN_KEY + "=?",
                new String[]{key}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                value = cursor.getString(0); // Get value from the first column
            }
            cursor.close();
        }
        db.close();
        return value; // Returns null if the key is not found
    }
}
