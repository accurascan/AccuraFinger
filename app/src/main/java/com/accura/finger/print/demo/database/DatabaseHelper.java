package com.accura.finger.print.demo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.accura.finger.print.sdk.model.FingerModel;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserManager.db";
    private static final String TABLE_USER = "user";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    //private static final String COLUMN_USER_PROFILE = "user_profile";
    private static final String COLUMN_USER_FINGER_INDEX = "finger_index";
    private static final String COLUMN_USER_FINGER_MIDDLE = "finger_middle";
    private static final String COLUMN_USER_FINGER_RING = "finger_ring";
    private static final String COLUMN_USER_FINGER_LITTLE = "finger_little";
    private static final String COLUMN_UNIQUE_ID = "unique_id";
    private static final String COLUMN_USER_SIFT = "user_sift";
    private static final String COLUMN_USER_INDEX_ARRAY = "index_finger_data";
    private static final String COLUMN_USER_MIDDLE_ARRAY = "middle_finger_data";
    private static final String COLUMN_USER_RING_ARRAY = "ring_finger_data";
    private static final String COLUMN_USER_LITTLE_ARRAY = "little_finger_data";
    private static final String COLUMN_USER_FINGER_SIDE_TYPE = "finger_type";
    private static final String COLUMN_USER_INDEX_TEMPLATE = "index_pers_template";
    private static final String COLUMN_USER_MIDDLE_TEMPLATE = "middle_pers_template";
    private static final String COLUMN_USER_RING_TEMPLATE = "ring_pers_template";
    private static final String COLUMN_USER_LITTLE_TEMPLATE = "little_pers_template";

    private String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_FINGER_SIDE_TYPE + " INTEGER,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_UNIQUE_ID + " LONG,"
            + COLUMN_USER_SIFT + " TEXT,"
            + COLUMN_USER_FINGER_INDEX + " TEXT,"
            + COLUMN_USER_FINGER_MIDDLE + " TEXT,"
            + COLUMN_USER_FINGER_RING + " TEXT,"
            + COLUMN_USER_FINGER_LITTLE + " TEXT,"
            + COLUMN_USER_INDEX_ARRAY + " TEXT,"
            + COLUMN_USER_MIDDLE_ARRAY + " TEXT,"
            + COLUMN_USER_RING_ARRAY + " TEXT,"
            + COLUMN_USER_LITTLE_ARRAY + " TEXT,"
            + COLUMN_USER_INDEX_TEMPLATE + " TEXT,"
            + COLUMN_USER_MIDDLE_TEMPLATE + " TEXT,"
            + COLUMN_USER_RING_TEMPLATE + " TEXT,"
            + COLUMN_USER_LITTLE_TEMPLATE + " TEXT"
            +")";

    private String DROP_USER_TABLE = "DROP TABLE IF EXISTS " + TABLE_USER;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Drop User Table if exist
        db.execSQL(DROP_USER_TABLE);
        // Create tables again
        onCreate(db);
    }

    public long addUser(FingerModel user, long uniqueID) {
        while (uniqueID < 0) {
            SecureRandom generator = new SecureRandom();
            int n = 1000000;
            uniqueID = generator.nextInt(n);
            if (getUserByUniqueID(uniqueID)) {
                continue;
            }
            break;
        }
        user.setUniqueID(uniqueID);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_FINGER_SIDE_TYPE, user.getFingerSideType());
        values.put(COLUMN_USER_NAME, user.getUserName());
        values.put(COLUMN_UNIQUE_ID, uniqueID);
        values.put(COLUMN_USER_FINGER_INDEX, user.getIndexFinger());
        values.put(COLUMN_USER_FINGER_MIDDLE, user.getMiddleFinger());
        values.put(COLUMN_USER_FINGER_RING, user.getRingFinger());
        values.put(COLUMN_USER_FINGER_LITTLE, user.getLittleFinger());
        values.put(COLUMN_USER_SIFT, user.getSIFT_IntArrayAsString());

        db.insert(TABLE_USER, null, values);
        db.close();
        return uniqueID;
    }


    public boolean getUserByUniqueID(long uniqueID) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_UNIQUE_ID,
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<FingerModel> userList = new ArrayList<FingerModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                COLUMN_UNIQUE_ID+"=?",        //columns for the WHERE clause
                new String[] {"'"+uniqueID+"'"},        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }
    public boolean getUserByName(String name) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE LOWER(user_name) = ?", new String[] { name.toLowerCase(Locale.ROOT) });
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }

    public List<FingerModel> getAllUser(FingerModel model, boolean byId) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_FINGER_SIDE_TYPE,
                COLUMN_USER_NAME,
                COLUMN_UNIQUE_ID,
                COLUMN_USER_FINGER_INDEX,
                COLUMN_USER_FINGER_MIDDLE,
                COLUMN_USER_FINGER_RING,
                COLUMN_USER_FINGER_LITTLE,
                COLUMN_USER_SIFT,
                COLUMN_USER_INDEX_ARRAY,
                COLUMN_USER_MIDDLE_ARRAY,
                COLUMN_USER_RING_ARRAY,
                COLUMN_USER_LITTLE_ARRAY,
                COLUMN_USER_INDEX_TEMPLATE,
                COLUMN_USER_MIDDLE_TEMPLATE,
                COLUMN_USER_RING_TEMPLATE,
                COLUMN_USER_LITTLE_TEMPLATE
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<FingerModel> userList = new ArrayList<FingerModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = (model == null ? null : COLUMN_UNIQUE_ID + (byId ? (" = ?") : (" != ?")));
        String[] args = (model == null ? null : new String[] {String.valueOf(model.getUniqueID())});
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                selection,        //columns for the WHERE clause
                args,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                FingerModel fingerModel = new FingerModel();
                fingerModel.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))));
                fingerModel.setFingerSideType(Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_SIDE_TYPE))));
                fingerModel.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)));
                fingerModel.setUniqueID(cursor.getLong(cursor.getColumnIndex(COLUMN_UNIQUE_ID)));
                fingerModel.setIndexFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_INDEX)));
                fingerModel.setMiddleFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_MIDDLE)));
                fingerModel.setRingFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_RING)));
                fingerModel.setLittleFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_LITTLE)));
                fingerModel.setSIFT_IntArrayAsString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_SIFT)));
                fingerModel.setPersistentIndexTemplateFromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_INDEX_TEMPLATE)));
                fingerModel.setPersistentMiddleTemplateFromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_MIDDLE_TEMPLATE)));
                fingerModel.setPersistentRingTemplateFromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_RING_TEMPLATE)));
                fingerModel.setPersistentLittleTemplateFromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_LITTLE_TEMPLATE)));
                if (!new File(fingerModel.getIndexFinger()).exists()) {
                    continue;
                }
                userList.add(fingerModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public List<FingerModel> getUserList() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
                COLUMN_UNIQUE_ID,
                COLUMN_USER_FINGER_INDEX,
                COLUMN_USER_INDEX_TEMPLATE
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<FingerModel> userList = new ArrayList<FingerModel>();
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder uniqueIdList= new StringBuilder();

        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                FingerModel fingerModel = new FingerModel();
                fingerModel.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)));
                fingerModel.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)));
                fingerModel.setUniqueID(cursor.getLong(cursor.getColumnIndex(COLUMN_UNIQUE_ID)));
                fingerModel.setIndexFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_INDEX)));
                fingerModel.setPersistentIndexTemplateFromString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_INDEX_TEMPLATE)));

                if (!new File(fingerModel.getIndexFinger()).exists() || fingerModel.getPersistentIndexTemplate() == null) {
                    continue;
                }
                if (!uniqueIdList.substring(0).contains(String.valueOf(fingerModel.getUniqueID()))) {
                    uniqueIdList.append(fingerModel.getUniqueID()).append(",");
                    userList.add(fingerModel);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }
    public List<FingerModel> getUserByUniqueID(FingerModel model) {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_FINGER_SIDE_TYPE,
                COLUMN_USER_NAME,
                COLUMN_UNIQUE_ID,
                COLUMN_USER_FINGER_INDEX,
                COLUMN_USER_FINGER_MIDDLE,
                COLUMN_USER_FINGER_RING,
                COLUMN_USER_FINGER_LITTLE,
                COLUMN_USER_SIFT
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<FingerModel> userList = new ArrayList<FingerModel>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                COLUMN_UNIQUE_ID + " = ?",        //columns for the WHERE clause
                new String[] {String.valueOf(model.getUniqueID())},        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        if (cursor.moveToFirst()) {
            do {
                FingerModel fingerModel = new FingerModel();
                fingerModel.setId(Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))));
                fingerModel.setFingerSideType(Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_SIDE_TYPE))));
                fingerModel.setUserName(cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME)));
                fingerModel.setUniqueID(cursor.getLong(cursor.getColumnIndex(COLUMN_UNIQUE_ID)));
                fingerModel.setIndexFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_INDEX)));
                fingerModel.setMiddleFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_MIDDLE)));
                fingerModel.setRingFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_RING)));
                fingerModel.setLittleFinger(cursor.getString(cursor.getColumnIndex(COLUMN_USER_FINGER_LITTLE)));
                fingerModel.setSIFT_IntArrayAsString(cursor.getString(cursor.getColumnIndex(COLUMN_USER_SIFT)));
                if (!new File(fingerModel.getIndexFinger()).exists()) {
                    continue;
                }
                userList.add(fingerModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return userList;
    }

    public void updateFeatures(FingerModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getUserName());
        values.put(COLUMN_USER_INDEX_TEMPLATE, user.getPersistentIndexTemplateAsString());
        values.put(COLUMN_USER_MIDDLE_TEMPLATE, user.getPersistentMiddleTemplateAsString());
        values.put(COLUMN_USER_RING_TEMPLATE, user.getPersistentRingTemplateAsString());
        values.put(COLUMN_USER_LITTLE_TEMPLATE, user.getPersistentLittleTemplateAsString());
        // updating row
        db.update(TABLE_USER, values, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    public void deleteUser(FingerModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

    public int getSize() {
        // array of columns to fetch
        String[] columns = {
                COLUMN_USER_ID,
                COLUMN_USER_NAME,
        };
        // sorting orders
        String sortOrder =
                COLUMN_USER_ID + " ASC";
        List<FingerModel> userList = new ArrayList<FingerModel>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        int size = cursor.getCount();
        cursor.close();
        db.close();
        return size;
    }
}

