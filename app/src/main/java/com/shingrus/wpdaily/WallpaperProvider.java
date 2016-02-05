package com.shingrus.wpdaily;

import java.io.IOException;
import java.net.URL;

/**
 * Created by shingrus on 22/01/16.
 * Interface for the abstract wallpaper source
 */
interface WallpaperProvider {
    String getWallpaperProvider();
    ImageDescription GetLastWallpaperLink() throws IOException;
}
