package com.shingrus.wpdaily;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by shingrus on 24/11/15.
 * Main souce for updating wallpaper
 */
public final class SetWallPaper {

    Context appContext;
    static SetWallPaper setWallPaper;
    static final String _log_tag = "SetWallPaper";


    public static final int TIMEOUT_CONNECT = 10000;
    public static final int TIMEOUT_READ = 20000;


    private final List<WallpaperProvider> providers = new ArrayList<>();
    WallpaperProvider currentProvider = null;
//    int currentProviderPos;


//    private static final String LAST_IMAGE_URL_KEY = "last_image_url";

    public enum UpdateResult {
        SUCCESS,
        NETWORK_FAIL,
        FAIL,
        PROVIDER_FAIL, ALREADY_SET
    }


    private SetWallPaper(Context ctx) {
        this.appContext = ctx;
        initProviders();
    }

    public static synchronized SetWallPaper getSetWallPaper(Context ctx) {
        if (setWallPaper == null && ctx != null) {
            setWallPaper = new SetWallPaper(ctx);
        }
        return setWallPaper;
    }


    private byte[] getImage(URL url) {

        byte[] retVal = null;
        if (url != null) {
            try {
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(TIMEOUT_CONNECT);
                conn.setReadTimeout(TIMEOUT_READ);
                InputStream in = conn.getInputStream();
                int contentLength = conn.getContentLength();
                ByteArrayOutputStream baos;
                if (contentLength != -1) {
                    baos = new ByteArrayOutputStream(contentLength);
                } else {
                    baos = new ByteArrayOutputStream((1024 + 512) * 1024); //1.5 Mb
                }

                //use here byte buffer
                int count;
                byte[] buf = new byte[100 * 1024];
                while ((count = in.read(buf)) != -1) {
                    baos.write(buf, 0, count);
                }

                in.close();

                retVal = baos.toByteArray();


            } catch (IOException e) {
                Log.d(_log_tag, "Can't connect to " + url);
            }
        }
        return retVal;
    }

    /**
     * @param buffer - Buffer with image, that should be set as WP
     * @return true if success, false - overvise
     */
    private boolean setWallPaperImage(byte[] buffer) {
        boolean retVal = false;
        if (buffer != null) {
            WallpaperManager wp = WallpaperManager.getInstance(appContext);
            try {
                Bitmap image = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                wp.setBitmap(image);
                retVal = true;
            } catch (IOException e) {
                Log.d(_log_tag, "set image error" + e);
            }
        }
        return retVal;
    }


    /**
     * @param imageDescription - ImageDescription where to get  new image
     * @return true if new image found, false - if not
     */
    private synchronized UpdateResult setWallPaperImage(ImageDescription imageDescription) {
        UpdateResult retVal = UpdateResult.FAIL;
        ImageStorage storage = ImageStorage.getInstance(appContext);
        if (!storage.isUrlAlreadyDownloaded(imageDescription.getUrl())) {
            try {
                URL url = new URL(imageDescription.getUrl());

                byte[] imageBuf = getImage(url);

                if (setWallPaperImage(imageBuf)) {
                    Log.d(_log_tag, "Set new image:" + url);

                    storage.putImage(imageDescription.getUrl(), currentProvider.getWallpaperProvider(),
                            imageDescription.getLinkPage(), imageBuf);
                    retVal = UpdateResult.SUCCESS;
                }

            } catch (MalformedURLException e) {
                retVal = UpdateResult.PROVIDER_FAIL;
            }

        } else {
            retVal = UpdateResult.ALREADY_SET;
            Log.d(_log_tag, "We already set this image: " + imageDescription.getUrl());
        }
        return retVal;

    }

    public UpdateResult updateWallPaperImage() {
        UpdateResult retVal = UpdateResult.FAIL;
        try {
            //chose provider
            //need to keep last provider
            Random r = new Random();
            for (int count = providers.size(), i = r.nextInt(count); count >0; count--,i++) {
                currentProvider = providers.get(i%providers.size());
                Log.d(_log_tag,"Check provider:" + currentProvider+ "; i:"+i+";count: "+count);
                ImageDescription imageDescription = currentProvider.GetLastWallpaperLink();
                if (imageDescription != null) {
                    retVal = setWallPaperImage(imageDescription);
                    if (retVal==UpdateResult.SUCCESS || retVal == UpdateResult.NETWORK_FAIL)
                        break;
                }
                else {
                    retVal = UpdateResult.PROVIDER_FAIL;
                }
            }

        } catch (IOException e) {
            retVal = UpdateResult.NETWORK_FAIL;
            Log.d(_log_tag, "IO:Exception" + e);
        }

        return retVal;
    }

    private void initProviders() {
        providers.add(new VokrugSvetaProvider());
        providers.add(new NationalGeographicProvider());
    }


}
