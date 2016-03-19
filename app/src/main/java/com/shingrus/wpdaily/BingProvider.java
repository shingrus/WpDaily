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
 * Created by shingrus on 27/01/16.
 * Bing provider
 */
public class BingProvider implements WallpaperProvider {
    private static final String PROVIDER = "Bing";
    private static final String whereGetImages = "http://www.bing.com/HPImageArchive.aspx?format=xml&idx=0&n=1&mkt=en-US";

    private static final String suffix = "_1366x768.jpg";

    @Override
    public String toString() {
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
                HttpURLConnection httpconn = (HttpURLConnection) connection;
                httpconn.setReadTimeout(SetWallPaper.TIMEOUT_READ);
                httpconn.setConnectTimeout(SetWallPaper.TIMEOUT_CONNECT);
                httpconn.setDoInput(true);
                httpconn.connect();
                int responseCode = httpconn.getResponseCode();
                switch (responseCode) {
                    case HttpURLConnection.HTTP_OK:
                        XmlPullParser parser = Xml.newPullParser();
                        try {
                            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            parser.setInput(connection.getInputStream(), null);
                            parser.nextTag();
                            boolean insideUrlbase = false;
                            boolean insideCopyright = false;
                            String urlBase = null, pageLink = null;
                            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                                int event = parser.getEventType();
                                if (event == XmlPullParser.START_TAG) {
                                    String name = parser.getName();
                                    if (name.equals("urlBase")) {
                                        Log.d("XML", "Found item, go deeper");
                                        insideUrlbase = true;
                                    } else if (name.equals("copyrightlink")) {
                                        insideCopyright = true;
                                    }
                                } else if (event == XmlPullParser.TEXT ) {
                                    if (insideCopyright && pageLink ==null) {
                                        pageLink = parser.getText();
                                    }
                                    else if (insideUrlbase && urlBase == null) {
                                        urlBase = listOfImages.getProtocol() +"://"+ listOfImages.getHost() + parser.getText() + suffix;
                                        Log.d("XML", "Bg: url:"+urlBase);
                                    }
                                    if (pageLink!=null && urlBase !=null) {
                                        retVal = new ImageDescription(urlBase);
                                        retVal.setLinkPage(pageLink);
                                        break;
                                    }
                                }
                            }
                        } catch (XmlPullParserException e) {
                            Log.d("XML", "Xml parser", e);
                        }

                        break;
                    default: {
                        Log.d("URL", "something went wrong, http status:" + responseCode);
                    }
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
