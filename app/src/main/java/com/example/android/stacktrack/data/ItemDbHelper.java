package com.example.android.stacktrack.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.stacktrack.data.ItemContract.ItemEntry;

/**
 * Created by Astraeus on 10/7/2017.
 */

public class ItemDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";

    public static final int DATABASE_VERSION = 1;

    /**
     * The SQL syntax for creating a table
     */
    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + ItemEntry.TABLE_NAME + " ("
            + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ItemEntry.ITEM_NAME + " TEXT NOT NULL, "
            + ItemEntry.ITEM_PRICE + " INTEGER NOT NULL, "
            + ItemEntry.ITEM_QUANTITY + " INTEGER DEFAULT 0, "
            + ItemEntry.ITEM_SUPPLIER + " TEXT DEFAULT unknown, "
            + ItemEntry.ITEM_SUPPLIER_EMAIL + " TEXT DEFAULT unknown, "
            + ItemEntry.ITEM_IMAGE + " BLOB);";

    /**
     * The SQL syntax for deleting a table
     */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS" + ItemEntry.TABLE_NAME;

    /**
     * The constructor
     */
    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
