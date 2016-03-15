package com.shingrus.wpdaily;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

/**
 * Created by shingrus on 16/02/16.
 * GoPro provider
 * Gets photos from
 * https://gopro.com/channel/photo-of-the-day
 */
public class GoProProvider implements WallpaperProvider {
    private static final String PROVIDER = "GoPro";
    private static final String whereToGetImages = "https://api.gopro.com/v2/channels/feed/playlists/photo-of-the-day.json?platform=web&page=1&per_page=1";
    private final String _log_tag = "WPD/GoproProvider";
    //full link = https://gopro.com/channel/photo-of-the-day/winter-finally-arrived-in-the-alps

    @Override
    public String getWallpaperProvider() {
        return PROVIDER;
    }

    @Override
    public ImageDescription GetLastWallpaperLink() throws IOException {
        ImageDescription retVal = null;


        URL url = new URL(whereToGetImages);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(SetWallPaper.TIMEOUT_CONNECT);
        conn.setReadTimeout(SetWallPaper.TIMEOUT_READ);
        InputStream in = conn.getInputStream();
        int contentLength = conn.getContentLength();
        ByteArrayOutputStream baos;
        if (contentLength != -1) {
            baos = new ByteArrayOutputStream(contentLength);
        } else {
            baos = new ByteArrayOutputStream(2048 * 1024); //1.5 Mb
        }

        //use here byte buffer
        int count;
        byte[] buf = new byte[100 * 1024];
        while ((count = in.read(buf)) != -1) {
            baos.write(buf, 0, count);
        }

        in.close();


        String result = new String(baos.toByteArray());
        try {

            JSONObject js = new JSONObject(result);


            JSONArray media = js.getJSONArray("media");


            String permaLink = js.getString("permalink");
            JSONObject mediaObj = media.getJSONObject(0);
            JSONObject xlimage = mediaObj.getJSONObject("thumbnails").getJSONObject("xl");
            String imageUrl = xlimage.getString("image");

            String pageLink = mediaObj.getString("permalink");
            pageLink = "http://gopro.com/channel/" + permaLink + "/" + pageLink;
            retVal = new ImageDescription(imageUrl);
            retVal.setLinkPage(pageLink);


        } catch (JSONException e) {
            Log.w(_log_tag, "JS exception at provider:" + PROVIDER);

        }
        return retVal;
    }
}
