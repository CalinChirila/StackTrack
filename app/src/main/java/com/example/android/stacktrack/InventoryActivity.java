package com.example.android.stacktrack;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stacktrack.data.ItemContract.ItemEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ItemCursorAdapter mItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Set the adapter to the list view
        ListView listView = (ListView) findViewById(R.id.list);
        mItemAdapter = new ItemCursorAdapter(this, null);
        TextView emptyView = (TextView) findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mItemAdapter);

        // Get an instance of the loader manager
        getLoaderManager().initLoader(0, null, this);

        // When a list item is clicked start the DetailsActivity
        // The DetailsActivity will have a new label = "Edit product"
        // The EditText fields will be populated automatically with the correct product details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);
                Uri itemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
                intent.setData(itemUri);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This makes the menu button appear on the app bar
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insert_dummy_data_menu_item:
                // Insert dummy data
                insertDummyData();
                break;

            case R.id.add_new_product_menu_item:
                // Start the DetailsActivity
                Intent intent = new Intent(InventoryActivity.this, DetailsActivity.class);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;

            case R.id.delete_all_entries_menu_item:
                // Delete all the items in the inventory
                showDeleteConfirmationDialogue();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialogue() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.dialog_delete_all_entries_message);
        alertBuilder.setPositiveButton(R.string.dialogue_delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteAllItems();
            }
        });

        alertBuilder.setNegativeButton(R.string.dialogue_cancel_button, null);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void deleteAllItems() {
        int deletedRows = getContentResolver().delete(ItemEntry.CONTENT_URI, null, null);
        if (deletedRows != 0) {
            Toast.makeText(this, R.string.all_items_have_been_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.failed_to_delete_items, Toast.LENGTH_SHORT).show();
        }
        getContentResolver().notifyChange(ItemEntry.CONTENT_URI, null);
    }

    private void insertDummyData() {

        ContentValues values = new ContentValues();
        values.put(ItemEntry.ITEM_NAME, "Powder milk");
        values.put(ItemEntry.ITEM_PRICE, 40);
        values.put(ItemEntry.ITEM_QUANTITY, 50);
        values.put(ItemEntry.ITEM_SUPPLIER, "Powder Supplier");
        values.put(ItemEntry.ITEM_SUPPLIER_EMAIL, "bakingpowder@crack.com");

        Uri newRowUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
        if (newRowUri != null) {
            Toast.makeText(this, R.string.dummy_data_added, Toast.LENGTH_SHORT).show();
            getContentResolver().notifyChange(newRowUri, null);
        } else {
            Toast.makeText(this, R.string.adding_dummy_data_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.ITEM_NAME,
                ItemEntry.ITEM_PRICE,
                ItemEntry.ITEM_IMAGE,
                ItemEntry.ITEM_QUANTITY
        };
        return new CursorLoader(this, ItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mItemAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemAdapter.swapCursor(null);
    }
}
