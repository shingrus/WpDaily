package com.shingrus.wpdaily;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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
public class ImageCursorAdapter  extends CursorAdapter{
    DateFormat df;
    public ImageCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        df = DateFormat.getDateInstance();

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.item_layout, null, false);

    }

    @Override
    public void bindView(View view, Context ctx, Cursor c) {
        ImageView iv= (ImageView) view.findViewById(R.id.ItemImageId);
        TextView tv = (TextView) view.findViewById(R.id.ItemDateId);

        int date = c.getInt(c.getColumnIndex(ImageStorage.IMAGES_COLUMN_DATE_INSERTED));
        long longdate = (long)date *1000;

        byte[] image = c.getBlob(c.getColumnIndex(ImageStorage.IMAGES_COLUMN_IMAGE));
        iv.setImageBitmap(BitmapFactory.decodeByteArray(image,0,image.length));

        tv.setText(df.format(new Date(longdate)));

    }
}
