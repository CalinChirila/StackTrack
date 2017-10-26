package com.example.android.stacktrack;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.stacktrack.data.ItemContract.ItemEntry;

/**
 * Created by utilizator12 on 09/10/2017.
 */

class ItemCursorAdapter extends CursorAdapter {

    ItemCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    private static class ViewHolder {
        TextView mItemNameTextView;
        TextView mItemPriceTextView;
        TextView mItemCurrentQuantityTextView;
        ImageView mItemImageView;
        Button sellItem;
    }

    /** Inflate a new blank list item view*/
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.mItemNameTextView = view.findViewById(R.id.product_name_text_view);
        holder.mItemPriceTextView = view.findViewById(R.id.product_price_text_view);
        holder.mItemCurrentQuantityTextView = view.findViewById(R.id.current_quantity_text_view);
        holder.mItemImageView = view.findViewById(R.id.product_image_view);
        holder.sellItem = view.findViewById(R.id.sell_button);

        view.setTag(holder);

        return view;
    }

    /** Bind the data to the list item layout*/
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        String productName = cursor.getString(cursor.getColumnIndex(ItemEntry.ITEM_NAME));
        Integer productPrice = cursor.getInt(cursor.getColumnIndex(ItemEntry.ITEM_PRICE));
        final Integer currentQuantity = cursor.getInt(cursor.getColumnIndex(ItemEntry.ITEM_QUANTITY));

        ViewHolder holder = (ViewHolder) view.getTag();

        holder.mItemNameTextView.setText(productName);
        holder.mItemPriceTextView.setText(productPrice.toString());
        holder.mItemCurrentQuantityTextView.setText(currentQuantity.toString());

        final int position = cursor.getInt(cursor.getColumnIndex(ItemEntry._ID));

         // When the sell button is pressed, decrease the product quantity by 1;
         // Display the new value;
         // Save the new value to the database.

        holder.sellItem.setOnClickListener(new View.OnClickListener(){
            // When the sell button is clicked, decrease the current product quantity by 1,
            // if it different than 0
            @Override
            public void onClick(View v){
                if(currentQuantity != 0){
                    // The uri for the current item
                    Uri itemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, position);

                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.ITEM_QUANTITY, currentQuantity - 1);

                    context.getContentResolver().update(itemUri, values, null, null);
                } else {
                    Toast.makeText(context, R.string.there_are_no_items, Toast.LENGTH_SHORT).show();
                }
            }

        });

    }
}
