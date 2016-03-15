package com.shingrus.wpdaily;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import android.os.Handler;

/**
 * Created by shingrus on 10/03/16.
 * Extends BaseAdapter
 */
public class ImagesAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    ArrayList<ImageDescription> mImagesCollection;
    private final DateFormat df;

    private static int  SHOW_LAST_IMAGES_NUMBER = 30;

    LruCache<Integer, Bitmap> imagesCache;
    Handler handler;
    private final static String _log_tag = "WPD/ImagesAdapter";
    ImageStorage storage = ImageStorage.getInstance();
    int screenWidthDp;

    public ImagesAdapter(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        screenWidthDp = configuration.screenWidthDp;
        inflater = LayoutInflater.from(context);
        mImagesCollection = new ArrayList<>(SHOW_LAST_IMAGES_NUMBER);
        df = DateFormat.getDateInstance();

        handler = new Handler();
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 4;

        imagesCache = new LruCache<Integer, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }


    public void swapItems(ArrayList<ImageDescription> newImages) {
        mImagesCollection = null;
        mImagesCollection = newImages;
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView itemDateView, itemProviderView;
        ImageView imageView;
    }

    @Override
    public int getCount() {
        return mImagesCollection.size();
    }

    @Override
    public ImageDescription getItem(int position) {
        return mImagesCollection.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mImagesCollection.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ImageDescription image = mImagesCollection.get(position);
        if (convertView == null) {
            holder = new ViewHolder();


//            convertView = inflater.inflate(R.layout.item_layout, null);
            convertView = inflater.inflate(R.layout.item_layout, parent, false);
            holder.itemDateView = (TextView) convertView.findViewById(R.id.ItemDateId);
            holder.itemProviderView = (TextView) convertView.findViewById(R.id.ItemProviderId);
            holder.imageView = (ImageView) convertView.findViewById(R.id.ItemImageId);
            convertView.setTag(holder);
            Log.d(_log_tag, "Load position = " + position);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int date = image.getInsertedAt();
        int id = image.getID();
        String prov = image.getProvider();
        long longdate = (long) date * 1000;

        Bitmap bmp = imagesCache.get(id);
        if (bmp == null) {
            holder.imageView.setImageResource(R.drawable.white_stub);
            loadMissedImage(position, holder.imageView, id);

        } else
            holder.imageView.setImageBitmap(bmp);
        Log.d(_log_tag, "Load position = " + position);
        holder.itemDateView.setText(df.format(new Date(longdate)));

        holder.itemProviderView.setText(prov);
        return convertView;
    }

    class ImageToLoad {
        private final ImageView view;
        private final int id;

        ImageToLoad(ImageView view, int id) {
            this.view = view;
            this.id = id;
        }
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth) {
        // Raw height and width of image
        final int width = options.outWidth;
        int inSampleSize = 1;

        if ( width > reqWidth) {

            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromData(final byte[] imageData,
                                                     int reqWidth) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);


        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth);


        int reqHeight;
        if (reqWidth < options.outWidth) {
            reqHeight = reqWidth * options.outHeight / options.outWidth;
        }
        else {
            reqHeight = options.outHeight;
        }

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap oldbmp = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

        Bitmap bmp = Bitmap.createScaledBitmap(oldbmp, reqWidth, reqHeight, true);
        Log.d(_log_tag, "oldbmp: " + oldbmp.getByteCount() + ", bmp: "+ bmp.getByteCount());
        oldbmp.recycle();
        return bmp;
    }

    private Map<View, Integer> viewToReneder = Collections.synchronizedMap(new WeakHashMap<View, Integer>());

    private void loadMissedImage(final int position, final ImageView v, final int id) {
        new AsyncTask<Void, Void, Bitmap>() {

            ImageToLoad newImage = new ImageToLoad(v, id);

            @Override
            protected void onPreExecute() {
                viewToReneder.put(v, id);
            }

            @Override
            protected Bitmap doInBackground(Void... params) {
                ImageDescription image = storage.getImageById(newImage.id);
                Bitmap retVal = null;
                if (image != null && image.getData() != null) {
                    byte[] data = image.getData();
//                    retVal = ima.decodeByteArray(data, 0, data.length);
                    retVal = decodeSampledBitmapFromData(data, screenWidthDp);
                }
                return retVal;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {

                if (viewToReneder.get(newImage.view) == null || newImage.id != viewToReneder.get(newImage.view)) {
                    Log.d(_log_tag, "Skip image");
                } else {
                    if (bmp != null) {
                        newImage.view.setImageBitmap(bmp);
                        imagesCache.put(newImage.id, bmp);
                    }
                    viewToReneder.remove(newImage.view);
                }


            }
        }.execute();
    }

}
