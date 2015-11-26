package com.shingrus.wpdaily;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by shingrus on 24/11/15.
 * Main souce for updating wallpaper
 */
public final class SetWallPaper {

    Context appContext;
    static SetWallPaper setWallPaper;
    static final String _log_tag = "SetWallPaper";

    private static String whereGetImages = "http://feeds.nationalgeographic.com/ng/photography/photo-of-the-day?format=xml";
    private static String MagicURLReplacementFrom = "360x270";
    private static String MagicURLReplacementTo = "990x742";

    private static String LAST_IMAGE_URL_KEY = "last_image_url";


    private SetWallPaper(Context ctx) {
        this.appContext = ctx;
    }

    public static synchronized  SetWallPaper getSetWallPaper(Context ctx) {
        if (ctx != null && setWallPaper == null) {
            setWallPaper = new SetWallPaper(ctx);
        }
        return setWallPaper;
    }

    public static  synchronized SetWallPaper getSetWallPaper() {
        return setWallPaper;
    }


    private Bitmap getImage(URL url) {

        //TODO: check for cached images
        Bitmap bmp = null;
        if (url != null) {
            try {
                URLConnection conn = url.openConnection();
                InputStream in = conn.getInputStream();
                bmp = BitmapFactory.decodeStream(in);

            } catch (IOException e) {
                Log.d(_log_tag, "Can't connect to " + url);
            }
        }
        return bmp;
    }

    /**
     *
     * @param image - Bitmap image, that should be set as WP
     * @return true if success, false - overvise
     */
    private boolean setWallPaperImage(Bitmap image) {
        boolean retVal = false;
        if (image != null) {
            WallpaperManager wp = WallpaperManager.getInstance(appContext);
            try {
                wp.setBitmap(image);
                retVal = true;
            } catch (IOException e) {
                Log.e(_log_tag, "set image error" + e);
            }
        }
        return retVal;
    }

    private void setWallPaperImage(URL url) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String lastUrl = preferences.getString(LAST_IMAGE_URL_KEY, "");
        if (!lastUrl.equals(url.toString())) {
            if(setWallPaperImage(getImage(url))) {
                Log.d(_log_tag, "Set new image:" + url);

                SharedPreferences.Editor e = preferences.edit();
                e.putString(LAST_IMAGE_URL_KEY, url.toString());
                e.apply();
            }
        }
        else
            Log.d(_log_tag, "We already set this image: "+url);

    }

    public void updateWallPaperImage() {
        URL imageUrl = setWallPaper.GetLastWallpaperLink();
        if (imageUrl != null) {
            setWallPaperImage(imageUrl);
        }

    }

    /**
     * @return null if didn't find or url obj ad
     */
    private URL GetLastWallpaperLink() {
        URL retrunValue = null;

        try {
            URL listOfImages = new URL(whereGetImages);

            try {
                URLConnection connection;
                connection = listOfImages.openConnection();
                if ((connection instanceof HttpURLConnection)) {
                    HttpURLConnection httpcon = (HttpURLConnection) connection;
                    httpcon.setReadTimeout(10000 /* milliseconds */);
                    httpcon.setConnectTimeout(15000 /* milliseconds */);
                    httpcon.setDoInput(true);
                    httpcon.connect();
                    int responseCode = httpcon.getResponseCode();
                    switch (responseCode) {
                        case HttpURLConnection.HTTP_OK:

                            XmlPullParser parser = Xml.newPullParser();

                            try {
                                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                                //parser.require(XmlPullParser.START_TAG, null, "channel");
                                parser.setInput(connection.getInputStream(), null);
                                parser.nextTag();
                                boolean insideItem = false; //here is some magic


                                while (parser.next() != XmlPullParser.END_DOCUMENT){
                                    int event = parser.getEventType();
                                    if (event == XmlPullParser.START_TAG) {
                                        String name = parser.getName();
                                        if (name.equals("item")) {
                                            Log.d("XML", "Found item, go deeper");
                                            insideItem = true;
                                        }
                                        else if (insideItem && name.contentEquals("enclosure")) {
                                            String xmlAttr = parser.getAttributeValue(null, "url");
                                            if (xmlAttr != null) {
                                                String newUrl = xmlAttr.replace(MagicURLReplacementFrom,MagicURLReplacementTo);
                                                retrunValue = new URL(newUrl);

                                            }

                                            break;
                                        }

                                    }


                                }


                            } catch (XmlPullParserException e) {
                                Log.d("XML", "Xml parser", e);
                            }

                            break;
                        default:
                            Log.d("URL", "something went wrong, http status:" + responseCode);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return retrunValue;
    }


}
