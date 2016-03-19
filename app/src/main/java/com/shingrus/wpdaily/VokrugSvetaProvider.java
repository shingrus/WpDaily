package com.shingrus.wpdaily;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by shingrus on 24/01/16.
 * Вокруг Света Provider
 * Provides last photo of the day
 */
public class VokrugSvetaProvider implements WallpaperProvider {
    private static final String PROVIDER = "Вокруг Света";
    private static final String whereGetImages = "http://www.vokrugsveta.ru/rss/photo_of_the_day.xml";


    @Override
    public final String toString() {
        return PROVIDER;
    }
    @Override
    public String getWallpaperProvider() {
        return PROVIDER;
    }

    @Override
    public ImageDescription GetLastWallpaperLink() throws IOException {
        ImageDescription retVal = null;
        try {
            URL listOfImages = new URL(whereGetImages);


            URLConnection connection;
            connection = listOfImages.openConnection();
            if ((connection instanceof HttpURLConnection)) {
                HttpURLConnection httpcon = (HttpURLConnection) connection;
                httpcon.setReadTimeout(SetWallPaper.TIMEOUT_READ);
                httpcon.setConnectTimeout(SetWallPaper.TIMEOUT_CONNECT);
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
                            boolean insideLink = false;
                            String pageLink = null;
                            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                                int event = parser.getEventType();
                                if (event == XmlPullParser.START_TAG) {
                                    String name = parser.getName();
                                    if (name.equals("item")) {
                                        Log.d("XML", "Found item, go deeper");
                                        insideItem = true;
                                    } else if (insideItem && name.contentEquals("enclosure")) {
                                        String xmlAttr = parser.getAttributeValue(null, "url");
                                        if (xmlAttr != null) {
                                            retVal = new ImageDescription(xmlAttr);
                                            retVal.setLinkPage(pageLink);
                                        }

                                        break;
                                    }
                                    else if (insideItem && name.contentEquals("link")) {
                                        insideLink = true;
                                    }

                                }
                                else if (insideItem && insideLink&& event == XmlPullParser.TEXT) {
                                    insideLink = false;
                                    pageLink = parser.getText();
                                    Log.d("XML", "VS pagelink: " + pageLink );
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


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public boolean isWallpaperSource() {
        return true;
    }
}
