package com.shingrus.wpdaily;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

/**
 * CursorAdapter for MainListView
 * Created by shingrus on 09/12/15.
 */
public class ImageCursorAdapter extends CursorAdapter {
    public static final int CACHE_SIZE = 30;
    private DateFormat df;
    private LayoutInflater inflater;

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

    private LruCache<Integer, Bitmap> bitmapLruCache;
    private static final String _log_tag = "WPD/ImageCursor";

    public ImageCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        inflater = LayoutInflater.from(context);
        df = DateFormat.getDateInstance();
        bitmapLruCache = new LruCache<>(CACHE_SIZE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View rowView = inflater.inflate(R.layout.item_layout, null, false);
        ViewHolder holder = new ViewHolder();
        holder.itemDateView = (TextView) rowView.findViewById(R.id.ItemDateId);
        holder.itemProviderView = (TextView) rowView.findViewById(R.id.ItemProviderId);
        holder.imageView = (ImageView) rowView.findViewById(R.id.ItemImageId);
        rowView.setTag(holder);
        return rowView;

    }

    public String getLinkPage(int position) {
        Cursor c = getCursor();
        if (c != null) {
            if (c.moveToPosition(position)) {
                return c.getString(linkPage_idx);
            }
        }
        return null;
    }

    class ViewHolder {
        TextView itemDateView, itemProviderView;
        ImageView imageView;
    }

    private static int image_idx = -1, date_idx = -1, provider_idx = -1, linkPage_idx = -1,
            id_idx = -1;

    @Override
    public void bindView(View view, Context ctx, Cursor c) {
        ViewHolder holder = (ViewHolder) view.getTag();

        if (image_idx == -1)
            image_idx = c.getColumnIndex(ImageStorage.IMAGES_COLUMN_IMAGE);
        if (date_idx == -1)
            date_idx = c.getColumnIndex(ImageStorage.IMAGES_COLUMN_DATE_INSERTED);
        if (provider_idx == -1)
            provider_idx = c.getColumnIndex(ImageStorage.IMAGES_COLUMN_PROVIDER);
        if (linkPage_idx == -1)
            linkPage_idx = c.getColumnIndex(ImageStorage.IMAGES_COLUMN_LINKPAGE);
        if (id_idx == -1)
            id_idx = c.getColumnIndex(ImageStorage.IMAGES_COLUMN_ID);

        byte[] image = c.getBlob(image_idx);
        if (image != null && image.length > 0) {
            ImageView iv = holder.imageView;//(ImageView) view.findViewById(R.id.ItemImageId);

            int date = c.getInt(date_idx);
            int id = c.getInt(id_idx);
            String prov = c.getString(provider_idx);
            long longdate = (long) date * 1000;


            Bitmap origBitmap = bitmapLruCache.get(id);
            if (origBitmap == null) {
                origBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                bitmapLruCache.put(id, origBitmap);
            }

            iv.setImageBitmap(origBitmap);
            holder.itemDateView.setText(df.format(new Date(longdate)));

            holder.itemProviderView.setText(prov);

        }
    }
}
