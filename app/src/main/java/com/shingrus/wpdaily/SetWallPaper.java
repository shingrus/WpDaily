package com.shingrus.wpdaily;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 */
public final class SetWallPaper {

    Context appContext;
    static SetWallPaper setWallPaper;
    static final String _log_tag = "SetWallPaper";

    private static String whereGetImages = "http://feeds.nationalgeographic.com/ng/photography/photo-of-the-day?format=xml";
    private static String MagicURLReplacementFrom = "360x270";
    private static String MagicURLReplacementTo = "990x742";


    private SetWallPaper(Context ctx) {
        this.appContext = ctx;
    }

    public static final synchronized  SetWallPaper getSetWallPaper(Context ctx) {
        if (ctx != null && setWallPaper == null) {
            setWallPaper = new SetWallPaper(ctx);
        }
        return setWallPaper;
    }

    public static final synchronized SetWallPaper getSetWallPaper() {
        return setWallPaper;
    }


    public Bitmap getImage(URL url) {
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
     * @return null if didn't find or url obj ad
     */
    public URL GetLastWallpaperLink() {
        URL retrunValue = null;

        try {
            URL listOfImages = new URL(whereGetImages);
            URLConnection connection = null;
            try {
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
                                boolean insideItem = false, insideEnclosure= false; //here is some magic


                                while (parser.next() != XmlPullParser.END_DOCUMENT){
                                    int event = parser.getEventType();
                                    if (event == XmlPullParser.START_TAG) {
                                        String name = parser.getName();
                                        if (name.equals("item")) {
                                            Log.d("XML", "Found item, go deeper");
                                            insideItem = true;
                                        }
                                        else if (insideItem && name.contentEquals("enclosure")) {
                                            insideEnclosure = true;
                                            String xmlAttr = parser.getAttributeValue(null, "url");
                                            if (xmlAttr != null) {
                                                String newUrl = xmlAttr.replace(MagicURLReplacementFrom,MagicURLReplacementTo);
                                                retrunValue = new URL(newUrl);

                                            }

                                            break;
                                        }
                                        else if (insideItem && insideEnclosure) {
                                            //we soppose to be inside tag enclosure, but didn't get text
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
