package com.example.android.stacktrack;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stacktrack.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    //TODO: Cand stachez produsele duplicat, pastreaza imaginea pusa la produsul initial


    @BindView (R.id.product_name_edit_text)
    EditText mNameEditText;
    @BindView (R.id.product_price_edit_text)
    EditText mPriceEditText;
    @BindView (R.id.quantity_detail_text_view)
    TextView mQuantityTextView;
    @BindView (R.id.product_supplier_edit_text)
    EditText mSupplierEditText;
    @BindView (R.id.product_supplier_email_edit_text)
    EditText mSupplierEmailEditText;
    @BindView (R.id.increment_quantity_button)
    Button mIncrementButton;
    @BindView (R.id.decrement_quantity_button)
    Button mDecrementButton;
    @BindView (R.id.add_image_button)
    ImageView mTakePhotoButton;
    @BindView (R.id.order_button)
    Button mOrderButton;

    private Uri mCurrentItemUri;
    private Uri mImageUri;

    private boolean mProductHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            mProductHasChanged = true;
            return false;
        }
    };

    private static final int PICK_PHOTO_REQUEST = 601;
    private static final int PERMISSION_READ_EXTERNAL_STORAGE = 1337;

    int mDisplayedQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_2);
        ButterKnife.bind(this);

        mCurrentItemUri = getIntent().getData();
        if(getSupportActionBar() != null) {
            if (mCurrentItemUri != null) {
                getSupportActionBar().setTitle("Edit product");
                getLoaderManager().initLoader(0, null, this);
            } else {
                getSupportActionBar().setTitle("Add product");
            }
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierEmailEditText.setOnTouchListener(mTouchListener);
        mTakePhotoButton.setOnTouchListener(mTouchListener);

        // Set the on click listener to the order button
        mOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));

                String[] emailAddress = new String[]{mSupplierEmailEditText.getText().toString()};
                String emailSubject = getString(R.string.order_product);
                String requiredProduct = mNameEditText.getText().toString();
                String productPrice = mPriceEditText.getText().toString();
                String productQuantity = mQuantityTextView.getText().toString();
                String emailMessage = getString(R.string.mail_body_new_batch) + requiredProduct + ".\n\n"
                        + getString(R.string.quantity_amount) + productQuantity + "\n"
                        + getString(R.string.cost_per_unit) + productPrice;

                emailIntent.putExtra(Intent.EXTRA_EMAIL, emailAddress);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, emailMessage);

                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
            }
        });

        // Set the on click listener on the increment button
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayedQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                mDisplayedQuantity += 1;
                mQuantityTextView.setText(String.valueOf(mDisplayedQuantity));
                mProductHasChanged = true;
            }
        });

        // Set the on click listener on the decrement button
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The displayed item quantity cannot have a negative value
                mDisplayedQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                if (mDisplayedQuantity > 0) {
                    mDisplayedQuantity -= 1;
                    mQuantityTextView.setText(String.valueOf(mDisplayedQuantity));
                    mProductHasChanged = true;
                }
            }
        });

        //When the ImageView is clicked, send the users to the gallery where they can pick
        //the image they want for the item in the inventory

        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                galleryIntent.setType("image/*");
                requestReadExternalStoragePermission();
                startActivityForResult(galleryIntent, PICK_PHOTO_REQUEST);
            }
        });

    }

    /**
     * Set what happens when the intent sends back the result for a specific intent
     *
     * @param requestCode = the request passed by the intent
     * @param resultCode  = the result from the activity
     * @param data        = the intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_REQUEST) {
            mImageUri = data.getData();
            Bitmap mImageBitmap = getImageFromUri(mImageUri);
            mTakePhotoButton.setImageBitmap(mImageBitmap);

        }
    }

    /**
     * Helper method that requests the read external storage permission at runtime
     */
    private void requestReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Create a Bitmap image from a given uri
     *
     * @param uri the image uri
     * @return Bitmap image
     */
    private Bitmap getImageFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }

        Bitmap imageBitmap = null;
        InputStream inputStream;

        // Create the bitmap image from an input stream and resize it so that loading
        // doesn't slow the app down.
        try {
            inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            imageBitmap = BitmapFactory.decodeStream(inputStream, null, options);

            mTakePhotoButton.setImageBitmap(imageBitmap);

            if(inputStream != null) {
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Couldn't find file with uri " + uri.toString());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Encountered an error while closing the input stream");
        }

        return imageBitmap;
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // The user clicked cancel, so exit the current activity
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    public void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.dialog_discard_message);
        alertBuilder.setPositiveButton(R.string.dialog_discard_button, discardButtonClickListener);
        alertBuilder.setNegativeButton(R.string.dialog_keep_editing_button, null);

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Makes the menu visible in the app bar
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_changes_menu_item:
                // Save the changes made into the database
                insertItem();
                finish();
                return true;

            case R.id.delete_item_menu_item:
                // Delete selected item from the database
                showDeleteConfirmationDialogue();
                return true;

            case android.R.id.home:
                if (!mProductHasChanged) {
                    // If there are no changes, return to the parent activity
                    NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardChangesClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardChangesClickListener);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void showDeleteConfirmationDialogue() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.delete_product_dialogue);
        alertBuilder.setPositiveButton(R.string.dialogue_delete_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // The user clicked delete, so delete the product
                deleteItem();
                finish();
            }
        });

        alertBuilder.setNegativeButton(R.string.dialogue_cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // The user clicked cancel, so go back to editor activity
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete the selected item from the database
     */
    public void deleteItem() {
        int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        if (rowsDeleted != 0) {
            Toast.makeText(this, R.string.product_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.failed_to_delete, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method that inserts a product into the database
     */
    private void insertItem() {
        String name = mNameEditText.getText().toString().trim();
        Integer price = null;
        if(!TextUtils.isEmpty(mPriceEditText.getText().toString())) {
            price = Integer.parseInt(mPriceEditText.getText().toString());
        }
        Integer quantity = Integer.parseInt(mQuantityTextView.getText().toString());
        String supplier = mSupplierEditText.getText().toString();
        String supplierEmail = mSupplierEmailEditText.getText().toString();
        String imageUriString = null;
        if (mImageUri != null) {
            imageUriString = mImageUri.toString();
        }

        if (TextUtils.isEmpty(name)
                || TextUtils.isEmpty(String.valueOf(price))
                || TextUtils.isEmpty(supplier)
                || TextUtils.isEmpty(supplierEmail)
                || TextUtils.isEmpty(imageUriString)) {
            Toast.makeText(getApplicationContext(), R.string.product_must_have_name, Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ItemEntry.ITEM_NAME, name);
        values.put(ItemEntry.ITEM_PRICE, price);
        values.put(ItemEntry.ITEM_QUANTITY, quantity);
        values.put(ItemEntry.ITEM_SUPPLIER, supplier);
        values.put(ItemEntry.ITEM_SUPPLIER_EMAIL, supplierEmail);
        if (imageUriString != null) {
            values.put(ItemEntry.ITEM_IMAGE, imageUriString);
        }

        if (mCurrentItemUri != null) {
            int updatedRows = getContentResolver().update(mCurrentItemUri, values, null, null);
            if (updatedRows != 0) {
                Toast.makeText(this, R.string.product_updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.product_update_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Check to see if there are any duplicate products in the inventory
            // If a product with the same name and price already exists in the inventory,
            // instead of adding another one, just add the 2 quantities.
            // Create a dialogue that will ask the user if he/she wants to add another item
            // or update the quantity of the old one
            Cursor cursor = getContentResolver().query(
                    ItemEntry.CONTENT_URI,
                    new String[]{ItemEntry._ID, ItemEntry.ITEM_NAME, ItemEntry.ITEM_PRICE, ItemEntry.ITEM_QUANTITY},
                    null,
                    null,
                    null);

            if (cursor != null && price != null) {
                while (cursor.moveToNext()) {
                    if (name.equals(cursor.getString(cursor.getColumnIndex(ItemEntry.ITEM_NAME)))
                            && price == cursor.getInt(cursor.getColumnIndex(ItemEntry.ITEM_PRICE))) {

                        int newQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.ITEM_QUANTITY));
                        int oldItemId = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));
                        quantity = quantity + newQuantity;
                        values.put(ItemEntry.ITEM_QUANTITY, quantity);
                        String selection = ItemEntry._ID + "=?";
                        String[] selectionArgs = new String[]{String.valueOf(oldItemId)};
                        getContentResolver().delete(ItemEntry.CONTENT_URI, selection, selectionArgs);
                        Toast.makeText(this, R.string.there_was_a_duplicate, Toast.LENGTH_LONG).show();

                    }
                }
                cursor.close();

            }
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
            if (newUri != null) {
                Toast.makeText(this, R.string.item_successfully_added, Toast.LENGTH_SHORT).show();
                getContentResolver().notifyChange(newUri, null);
            } else {
                Toast.makeText(this, R.string.failed_to_add_item, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.ITEM_NAME,
                ItemEntry.ITEM_PRICE,
                ItemEntry.ITEM_QUANTITY,
                ItemEntry.ITEM_SUPPLIER,
                ItemEntry.ITEM_SUPPLIER_EMAIL,
                ItemEntry.ITEM_IMAGE
        };
        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_QUANTITY);
            int productSupplierColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_SUPPLIER);
            int productSupplierEmailColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_SUPPLIER_EMAIL);
            int productImageColumnIndex = cursor.getColumnIndex(ItemEntry.ITEM_IMAGE);

            String productName = cursor.getString(productNameColumnIndex);
            int productPrice = cursor.getInt(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String productSupplier = cursor.getString(productSupplierColumnIndex);
            String productSupplierEmail = cursor.getString(productSupplierEmailColumnIndex);

            String imageUriString = cursor.getString(productImageColumnIndex);
            Bitmap imageBitmap = null;
            if (imageUriString != null) {
                Uri imageUri = Uri.parse(imageUriString);
                imageBitmap = getImageFromUri(imageUri);
            }

            mNameEditText.setText(productName);
            mPriceEditText.setText(String.valueOf(productPrice));
            mQuantityTextView.setText(String.valueOf(productQuantity));
            mSupplierEditText.setText(productSupplier);
            mSupplierEmailEditText.setText(productSupplierEmail);
            if (imageBitmap != null) {
                mTakePhotoButton.setImageBitmap(imageBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");
        mSupplierEditText.setText("");
        mSupplierEmailEditText.setText("");
        mTakePhotoButton.setImageResource(R.drawable.ic_camera_alt);
    }
}
