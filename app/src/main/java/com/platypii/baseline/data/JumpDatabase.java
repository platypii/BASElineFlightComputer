package com.platypii.baseline.data;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class JumpDatabase {

    private static final String DATABASE_NAME = "Jumps.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Jumps";

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    public ArrayList<Jump> jumps;

    public int lastJumpNumber; // The current number of jumps (including in progress)
    private Jump currentJump = null; // Null unless a jump is in progress


    public JumpDatabase(Context context) {
        databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getWritableDatabase();
        
        // Load from database
        loadJumps();
    }

    private void loadJumps() {
        jumps = new ArrayList<Jump>();
        lastJumpNumber = 0;

        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        while(cursor.moveToNext()) {
            // Load each row from cursor
            Jump jump = new Jump(cursor);
            jumps.add(jump);
            if(lastJumpNumber < jump.jumpNumber)
                lastJumpNumber = jump.jumpNumber;
        }

        // Find the end of improperly terminated jumps
        for(int i = 0; i < jumps.size(); i++) {
            Jump jump = jumps.get(i);
            if(jump.jumpNumber >= 0 && jump.timeEnd == Long.MAX_VALUE) {
                // Improperly ended jump
                if(i < jumps.size() - 1) {
                    Jump next = jumps.get(i + 1);
                    endJump(jump, next.timeStart - 1);
                } else {
                    endJump(jump, System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * Begins a jump (after entering freefall)
     */
    public void startJump(long startTime) {
        assert currentJump == null;
        lastJumpNumber++;
        currentJump = new Jump("Jump " + lastJumpNumber, lastJumpNumber, startTime, Long.MAX_VALUE); // Create new jump
        jumps.add(currentJump); // Save local copy
        db.insert(TABLE_NAME, null, currentJump.getContentValues()); // Insert into db
    }

    public void deleteJump(Jump jump) {
        db.delete(TABLE_NAME, "jump_name = ?", new String[]{jump.jumpName});
        loadJumps();
    }

    /**
     * Ends a jump when back in ground mode
     */
    public void endJump(long endTime) {
        assert currentJump != null;
        endJump(currentJump, endTime);
    }
    private void endJump(Jump jump, long endTime) {
        if(jump != null) {
            // Update local jump
            jump.timeEnd = endTime;
            // Update database
            ContentValues values = new ContentValues();
            values.put("time_end", Long.toString(endTime));
            db.update(TABLE_NAME, values, "_id=?", new String[]{Integer.toString(jump.jumpNumber)});
        }
    }
    

    private class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
             super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w("SQLite", "Creating table " + TABLE_NAME);
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                       + "_id INTEGER PRIMARY KEY,"
                       + "jump_name TEXT,"
                       + "time_start INTEGER,"
                       + "time_end INTEGER);");
            
            // Add default jumps
            db.insert(TABLE_NAME, null, new Jump("History", -1, 0, Long.MAX_VALUE).getContentValues());
            // db.insert(TABLE_NAME, null, new Jump("Session", -1, MainActivity.startTime, Long.MAX_VALUE).getContentValues());
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

}
