package com.example.android.stacktrack.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.android.stacktrack.R;
import com.example.android.stacktrack.data.ItemContract.ItemEntry;

/**
 * Created by utilizator12 on 09/10/2017.
 */

public class ItemProvider extends ContentProvider {

    private static final String LOG_TAG = ItemProvider.class.getSimpleName();

    private ItemDbHelper mDbHelper;

    /**
     * The UriMatcher integer codes
     */
    private static final int ITEMS = 100;       // For the entire table
    private static final int ITEM_ID = 101;     // For a single item in the table

    /**
     * Create a new UriMatcher object
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Set the uri matcher
    static {
        // For the entire table
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);

        // For a single item in the table
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEM_ID, ITEM_ID);

    }

    @Override
    public boolean onCreate() {

        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query on the uri and return a cursor
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get a readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Declare the cursor
        Cursor cursor;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                // The cursor should return the entire table
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case ITEM_ID:
                // The cursor should return a single row in the table
                // based on the ID
                // SELECT * FROM items WHERE _id = ?
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unable to query invalid uri: " + uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemContract.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemContract.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Unable to insert item with uri: " + uri);
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // Verify if the fields have been completed with a value
        String name = values.getAsString(ItemEntry.ITEM_NAME);
        Integer price = values.getAsInteger(ItemEntry.ITEM_PRICE);
        String supplier = values.getAsString(ItemEntry.ITEM_SUPPLIER);
        String supplierEmail = values.getAsString(ItemEntry.ITEM_SUPPLIER_EMAIL);

        if (name == null || name.length() == 0) {
            Toast.makeText(getContext(), R.string.name_field_cannot_be_emtpy, Toast.LENGTH_LONG).show();
            return null;
        }


        if (price == null || price < 0 || TextUtils.isEmpty(price.toString())) {
            Toast.makeText(getContext(), R.string.price_field_cannot_be_empty, Toast.LENGTH_LONG).show();
            return null;
        }

        if (supplier == null || supplier.length() == 0) {
            Toast.makeText(getContext(), R.string.supplier_field_cannot_be_empty, Toast.LENGTH_LONG).show();
            return null;
        }


        if (supplierEmail == null || supplierEmail.length() == 0) {
            Toast.makeText(getContext(), R.string.supplier_email_field_cannot_be_empty, Toast.LENGTH_LONG).show();
            return null;
        }


        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(ItemEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to add item with uri: " + uri);
            return null;
        }

        Uri newItemUri = ContentUris.withAppendedId(uri, id);
        if (newItemUri != null && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return newItemUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get a writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Delete the entire table
                return db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            case ITEM_ID:
                // Delete a single row in the table based on its id
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return db.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot delete item with uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues,
                      @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ITEMS:
                // Update the entire table
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // Update a single item in the table based on its id
                // Define the selection and the selectionArgs
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update item with uri: " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Check the entry fields if they contain the keys
        if (values.containsKey(ItemEntry.ITEM_NAME)) {
            String name = values.getAsString(ItemEntry.ITEM_NAME);
            if (name == null || name.length() == 0) {
                Toast.makeText(getContext(), R.string.name_field_cannot_be_emtpy, Toast.LENGTH_LONG).show();
                return 0;
            }
        }

        if (values.containsKey(ItemEntry.ITEM_PRICE)) {
            Integer price = values.getAsInteger(ItemEntry.ITEM_PRICE);
            if (price == 0 || price < 0) {
                Toast.makeText(getContext(), R.string.price_field_cannot_be_empty, Toast.LENGTH_LONG).show();
                return 0;
            }

        }

        if (values.containsKey(ItemEntry.ITEM_SUPPLIER)) {
            String supplier = values.getAsString(ItemEntry.ITEM_SUPPLIER);
            if (supplier == null || supplier.length() == 0) {
                Toast.makeText(getContext(), R.string.supplier_field_cannot_be_empty, Toast.LENGTH_LONG).show();
                return 0;
            }
        }

        if (values.containsKey(ItemEntry.ITEM_SUPPLIER_EMAIL)) {
            String supplierEmail = values.getAsString(ItemEntry.ITEM_SUPPLIER_EMAIL);
            if (supplierEmail == null || supplierEmail.length() == 0) {
                Toast.makeText(getContext(), R.string.supplier_email_field_cannot_be_empty, Toast.LENGTH_LONG).show();
                return 0;
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        // Get a writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updatedRows = db.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        if (updatedRows != 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }
}
