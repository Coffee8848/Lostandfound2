package com.example.lostandfound2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LostFoundDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "lost_found.db";
    private static final int DB_VERSION = 2;

    private static final String TABLE_ITEMS = "items";
    private static final String COL_ID = "id";
    private static final String COL_POST_TYPE = "post_type";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_CATEGORY = "category";
    private static final String COL_LOCATION = "location";
    private static final String COL_IMAGE_PATH = "image_path";
    private static final String COL_POSTED_AT = "posted_at";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";

    public LostFoundDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_POST_TYPE + " TEXT NOT NULL, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_PHONE + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_LOCATION + " TEXT NOT NULL, " +
                COL_IMAGE_PATH + " TEXT NOT NULL, " +
                COL_POSTED_AT + " INTEGER NOT NULL, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL" +
                ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public long insertItem(LostItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TYPE, item.getPostType());
        values.put(COL_NAME, item.getName());
        values.put(COL_PHONE, item.getPhone());
        values.put(COL_DESCRIPTION, item.getDescription());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_LOCATION, item.getLocation());
        values.put(COL_IMAGE_PATH, item.getImagePath());
        values.put(COL_POSTED_AT, item.getPostedAtMillis());
        values.put(COL_LATITUDE, item.getLatitude());
        values.put(COL_LONGITUDE, item.getLongitude());
        return db.insert(TABLE_ITEMS, null, values);
    }

    public List<LostItem> getItems(String categoryFilter) {
        List<LostItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;
        if (categoryFilter != null && !"All".equals(categoryFilter)) {
            selection = COL_CATEGORY + " = ?";
            selectionArgs = new String[]{categoryFilter};
        }

        Cursor cursor = db.query(
                TABLE_ITEMS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COL_ID + " DESC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                String postType = cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH));
                long postedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_POSTED_AT));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE));

                items.add(new LostItem(id, postType, name, phone, description, category, location, imagePath, postedAt, latitude, longitude));
            }
            cursor.close();
        }
        return items;
    }

    public LostItem getItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ITEMS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        LostItem item = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String postType = cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH));
                long postedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_POSTED_AT));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE));

                item = new LostItem(id, postType, name, phone, description, category, location, imagePath, postedAt, latitude, longitude);
            }
            cursor.close();
        }
        return item;
    }

    public boolean deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int deletedRows = db.delete(TABLE_ITEMS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        return deletedRows > 0;
    }
}
