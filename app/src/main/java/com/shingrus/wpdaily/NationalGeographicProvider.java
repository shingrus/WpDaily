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
 * National Geographic Providers
 * Provides last photo of the day
 */
public class NationalGeographicProvider implements WallpaperProvider {
    private static final String whereGetImages = "http://feeds.nationalgeographic.com/ng/photography/photo-of-the-day?format=xml";
    private static final String MagicURLReplacementFrom = "360x270";
    private static final String MagicURLReplacementTo = "990x742";
    private static final String PROVIDER = "National Geographic";

    @Override
    public String getWallpaperProvider() {
        return PROVIDER;
    }

    @Override
    public final String toString() {
        return PROVIDER;
    }

    @Override
    public ImageDescription GetLastWallpaperLink() throws IOException {
        ImageDescription returnImage = null;

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
                                            String newUrl = xmlAttr.replace(MagicURLReplacementFrom, MagicURLReplacementTo);
                                            //retrunValue = new URL(newUrl);
                                            returnImage = new ImageDescription(newUrl);
                                            returnImage.setLinkPage(pageLink);
                                        }

                                        break;
                                    } else if (insideItem && name.contentEquals("link")) {
                                        insideLink = true;
                                    }
                                } else if (event == XmlPullParser.TEXT) {
                                    if (insideItem && insideLink) {
                                        pageLink = parser.getText();
                                        Log.d("XML", "NG pagelink: " + pageLink);
                                        insideLink = false;
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


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        return returnImage;
    }

    @Override
    public boolean isWallpaperSource() {
        return true;
    }
}
