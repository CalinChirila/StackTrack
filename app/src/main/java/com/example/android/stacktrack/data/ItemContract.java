package com.example.android.stacktrack.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Astraeus on 10/5/2017.
 */

public final class ItemContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.stacktrack";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * The different Paths
     */
    public static final String PATH_ITEMS = "items";
    public static final String PATH_ITEM_ID = "items/#";

    /**
     * The MIME types
     */
    public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
            + CONTENT_AUTHORITY + "/" + PATH_ITEMS;       // For the entire table

    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
            + CONTENT_AUTHORITY + "/" + PATH_ITEM_ID;    // For a single item in the table

    /**
     * The inventory items table
     */
    public static abstract class ItemEntry implements BaseColumns {

        public static final String TABLE_NAME = "items";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        public static final String _ID = BaseColumns._ID;
        public static final String ITEM_NAME = "name";
        public static final String ITEM_PRICE = "price";
        public static final String ITEM_QUANTITY = "quantity";
        public static final String ITEM_SUPPLIER = "supplier";
        public static final String ITEM_SUPPLIER_EMAIL = "email";
        public static final String ITEM_IMAGE = "image";
    }
}
