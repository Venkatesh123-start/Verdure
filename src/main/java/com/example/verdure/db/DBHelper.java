package com.example.verdure.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.verdure.model.Plant;
import com.example.verdure.util.Security;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "verdure.db";
    private static final int DATABASE_VERSION = 4; // bumped for image columns & users

    // Plants table
    private static final String TABLE_PLANTS = "plants";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_NOTES = "notes";
    private static final String COL_INTERVAL = "interval_days";
    private static final String COL_REMINDER = "reminder_time";
    private static final String COL_LAST = "last_watered";
    private static final String COL_IMAGE = "image_path";
    private static final String COL_HIST = "image_hist";

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String U_COL_ID = "id";
    private static final String U_COL_USERNAME = "username";
    private static final String U_COL_PASSWORD = "password_hash";
    private static final String U_COL_IS_ADMIN = "is_admin";
    private static final String U_COL_SEC_Q = "sec_q";
    private static final String U_COL_SEC_ANS = "sec_ans_hash";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPlants = "CREATE TABLE " + TABLE_PLANTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COL_NAME + " TEXT," +
                COL_NOTES + " TEXT," +
                COL_INTERVAL + " INTEGER," +
                COL_REMINDER + " TEXT," +
                COL_LAST + " TEXT," +
                COL_IMAGE + " TEXT," +
                COL_HIST + " TEXT" +
                ")";
        db.execSQL(createPlants);

        String createUsers = "CREATE TABLE " + TABLE_USERS + " (" +
                U_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                U_COL_USERNAME + " TEXT UNIQUE," +
                U_COL_PASSWORD + " TEXT," +
                U_COL_IS_ADMIN + " INTEGER DEFAULT 0," +
                U_COL_SEC_Q + " TEXT," +
                U_COL_SEC_ANS + " TEXT" +
                ")";
        db.execSQL(createUsers);

        // create default admin
        try {
            String adminUser = "admin";
            String adminPassHash = Security.sha256("admin123");
            String adminQ = "What is admin code?";
            String adminAnsHash = Security.sha256("admin");
            ContentValues cv = new ContentValues();
            cv.put(U_COL_USERNAME, adminUser);
            cv.put(U_COL_PASSWORD, adminPassHash);
            cv.put(U_COL_IS_ADMIN, 1);
            cv.put(U_COL_SEC_Q, adminQ);
            cv.put(U_COL_SEC_ANS, adminAnsHash);
            db.insert(TABLE_USERS, null, cv);
        } catch (Exception e) {
            Log.e(TAG, "Failed to insert default admin", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        // simple drop & recreate for tutorial (data loss warning)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ---------------- Plants ----------------

    // Insert
    public long insertPlant(Plant p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, p.getName());
        cv.put(COL_NOTES, p.getNotes());
        cv.put(COL_INTERVAL, p.getIntervalDays());
        cv.put(COL_REMINDER, p.getReminderTime());
        cv.put(COL_LAST, p.getLastWatered());
        cv.put(COL_IMAGE, p.getImagePath());
        cv.put(COL_HIST, p.getImageHist());
        long id = db.insert(TABLE_PLANTS, null, cv);
        db.close();
        return id;
    }

    // Update
    public int updatePlant(Plant p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, p.getName());
        cv.put(COL_NOTES, p.getNotes());
        cv.put(COL_INTERVAL, p.getIntervalDays());
        cv.put(COL_REMINDER, p.getReminderTime());
        cv.put(COL_LAST, p.getLastWatered());
        cv.put(COL_IMAGE, p.getImagePath());
        cv.put(COL_HIST, p.getImageHist());
        int res = db.update(TABLE_PLANTS, cv, COL_ID + "=?", new String[]{String.valueOf(p.getId())});
        db.close();
        return res;
    }

    // Update last watered quickly by id
    public int updateLastWatered(int id, String isoDatetime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LAST, isoDatetime);
        int res = db.update(TABLE_PLANTS, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return res;
    }

    // Delete
    public int deletePlant(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int res = db.delete(TABLE_PLANTS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return res;
    }

    // Get all
    public List<Plant> getAllPlants() {
        List<Plant> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PLANTS + " ORDER BY " + COL_NAME + " COLLATE NOCASE", null);
        if (c.moveToFirst()) {
            do {
                Plant p = new Plant();
                p.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
                p.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
                p.setNotes(c.getString(c.getColumnIndexOrThrow(COL_NOTES)));
                p.setIntervalDays(c.getInt(c.getColumnIndexOrThrow(COL_INTERVAL)));
                p.setReminderTime(c.getString(c.getColumnIndexOrThrow(COL_REMINDER)));
                p.setLastWatered(c.getString(c.getColumnIndexOrThrow(COL_LAST)));
                p.setImagePath(c.getString(c.getColumnIndexOrThrow(COL_IMAGE)));
                p.setImageHist(c.getString(c.getColumnIndexOrThrow(COL_HIST)));
                list.add(p);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    // Get by id
    public Plant getPlant(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_PLANTS + " WHERE " + COL_ID + "=?", new String[]{String.valueOf(id)});
        Plant p = null;
        if (c.moveToFirst()) {
            p = new Plant();
            p.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
            p.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
            p.setNotes(c.getString(c.getColumnIndexOrThrow(COL_NOTES)));
            p.setIntervalDays(c.getInt(c.getColumnIndexOrThrow(COL_INTERVAL)));
            p.setReminderTime(c.getString(c.getColumnIndexOrThrow(COL_REMINDER)));
            p.setLastWatered(c.getString(c.getColumnIndexOrThrow(COL_LAST)));
            p.setImagePath(c.getString(c.getColumnIndexOrThrow(COL_IMAGE)));
            p.setImageHist(c.getString(c.getColumnIndexOrThrow(COL_HIST)));
        }
        c.close();
        db.close();
        return p;
    }

    // ---------------- Users ----------------

    // Create user (returns id or -1)
    public long createUser(String username, String passwordHash, int isAdmin, String secQ, String secAnsHash) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(U_COL_USERNAME, username);
        cv.put(U_COL_PASSWORD, passwordHash);
        cv.put(U_COL_IS_ADMIN, isAdmin);
        cv.put(U_COL_SEC_Q, secQ);
        cv.put(U_COL_SEC_ANS, secAnsHash);
        long id = -1;
        try {
            id = db.insertOrThrow(TABLE_USERS, null, cv);
        } catch (Exception e) {
            Log.w(TAG, "createUser failed (maybe exists): " + e.getMessage());
        } finally {
            db.close();
        }
        return id;
    }

    // Check if username exists
    public boolean userExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_USERS + " WHERE " + U_COL_USERNAME + "=?", new String[]{username});
        boolean exists = c.moveToFirst();
        c.close();
        db.close();
        return exists;
    }

    // Validate login and return user id (or -1 if fail)
    public long validateUser(String username, String passwordHash) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + U_COL_ID + " FROM " + TABLE_USERS +
                " WHERE " + U_COL_USERNAME + "=? AND " + U_COL_PASSWORD + "=?", new String[]{username, passwordHash});
        long id = -1;
        if (c.moveToFirst()) {
            id = c.getLong(c.getColumnIndexOrThrow(U_COL_ID));
        }
        c.close();
        db.close();
        return id;
    }

    // Get isAdmin flag (0/1)
    public int getIsAdmin(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + U_COL_IS_ADMIN + " FROM " + TABLE_USERS + " WHERE " + U_COL_ID + "=?", new String[]{String.valueOf(userId)});
        int res = 0;
        if (c.moveToFirst()) {
            res = c.getInt(c.getColumnIndexOrThrow(U_COL_IS_ADMIN));
        }
        c.close();
        db.close();
        return res;
    }

    // Get security question for username (or null)
    public String getSecurityQuestion(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + U_COL_SEC_Q + " FROM " + TABLE_USERS + " WHERE " + U_COL_USERNAME + "=?", new String[]{username});
        String q = null;
        if (c.moveToFirst()) q = c.getString(c.getColumnIndexOrThrow(U_COL_SEC_Q));
        c.close();
        db.close();
        return q;
    }

    // Verify security answer hash and allow password change (return true if updated)
    public boolean verifySecAnsAndUpdatePassword(String username, String secAnsHash, String newPasswordHash) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT " + U_COL_ID + " FROM " + TABLE_USERS + " WHERE " + U_COL_USERNAME + "=? AND " + U_COL_SEC_ANS + "=?", new String[]{username, secAnsHash});
        boolean ok = false;
        if (c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndexOrThrow(U_COL_ID));
            ContentValues cv = new ContentValues();
            cv.put(U_COL_PASSWORD, newPasswordHash);
            int updated = db.update(TABLE_USERS, cv, U_COL_ID + "=?", new String[]{String.valueOf(id)});
            ok = updated > 0;
        }
        c.close();
        db.close();
        return ok;
    }
}
