package com.shingrus.wpdaily;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by shingrus on 14/12/15.
 * Shows an Image from stored images
 */
public class ShowImage extends AppCompatActivity {
    private Image image;
    private ImageView imageView;
    public static final String IMAGE_ID_KEY = "image_id";


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_image);
        imageView = (ImageView) findViewById(R.id.showImageImageId);
        Bundle extras = getIntent().getExtras();
        long id  = extras.getLong(IMAGE_ID_KEY);

        final ImageStorage storage = ImageStorage.getInstance();

        new AsyncTask<Long, Void, Image>() {

            @Override
            protected Image doInBackground(Long... id) {
                return storage.getImageById(id[0]);
            }


            @Override
            protected void onPostExecute(Image image) {
                if (image != null) {
                    ShowImage.this.image = image;
                    byte rawImage[] = image.getData();
                    Bitmap bmp = BitmapFactory.decodeByteArray(rawImage, 0,rawImage.length);
                    imageView.setImageBitmap(bmp);
                }
            }
        }.execute(id, null, null);


    }
}
