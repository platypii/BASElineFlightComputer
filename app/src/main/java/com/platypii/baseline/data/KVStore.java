package com.platypii.baseline.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class KVStore {

    private static KVStoreHelper helper;
    private static SQLiteDatabase database;
    private static boolean started = false;

    public static synchronized void start(Context appContext) {
        // Start database
        if (!started) {
            helper = new KVStoreHelper(appContext);
            database = helper.getReadableDatabase();
            Log.w("KVStore", "Database started");
            started = true;
        } else {
            Log.e("KVStore", "Already started");
            if (database == null) {
                database = helper.getReadableDatabase();
            }
        }
    }

    public static String getString(String key) {
        if(started) {
            final String[] params = {key};
            final Cursor cursor = database.rawQuery("SELECT value FROM kvstore WHERE key = ?", params);
            String value = null;
            if(cursor != null) {
                if(cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    value = cursor.getString(0);
                }
                cursor.close();
            }
            return value;
        } else {
            Log.e("KVStore", "Get attempted on uninitialized database");
            return null;
        }
    }

    public static void put(String key, String value) {
        if(started) {
            final String[] params = {key, value};
            final Cursor cursor = database.rawQuery("INSERT INTO kvstore (key,value) VALUES (?,?)", params);
            cursor.close();
        } else {
            Log.e("KVStore", "Put attempted on uninitialized database");
        }
    }

}
