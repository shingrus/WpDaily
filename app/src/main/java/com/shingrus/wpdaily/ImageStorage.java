package com.shingrus.wpdaily;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * ImageDescription storage - Singleton object, has all necessary methods for database access
 * Created by shingrus on 06/12/15.
 */
public class ImageStorage {
    public static final String _log_tag = "ImageStorage";

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Images.db";
    private static final String IMAGES_TABLE_NAME = "Images";
    public static final String IMAGES_COLUMN_ID = "_id";
    public static final String IMAGES_COLUMN_URL = "url";
    public static final String IMAGES_COLUMN_IMAGE = "image";
    public static final String IMAGES_COLUMN_DATE_INSERTED = "inserted_at";
    public static final String IMAGES_COLUMN_LINKPAGE = "linkPage";
    public static final String IMAGES_COLUMN_PROVIDER = "provider";
    private static String KEEP_LAST_IMAGES_NUMBER = "30";
    private static final String IMAGES_LAST_IMAGES_LIMIT = KEEP_LAST_IMAGES_NUMBER;
    private static final String CREATE_IMAGES_TABLE = "CREATE TABLE '" + IMAGES_TABLE_NAME + "' (" +
            "'_id' INTEGER PRIMARY KEY AUTOINCREMENT," +
            "'url' TEXT UNIQ, " +
            "'inserted_at' INTEGER default (strftime('%s','now'))," +
            "'provider' TEXT default ''," +
            "'linkPage' TEXT default ''," +
            "'image' BLOB" +
            ")";


    private static ImageStorage sInstance;
    private Context mCtx;
    private ImageDBHelper mImageDBHelper;
    private static final String appFolder = File.separator + "Daily Wallpaper";

    private ImageStorage(Context context) {
        mCtx = context;
        mImageDBHelper = new ImageDBHelper(mCtx);
    }


    private static final String INSERT_IMAGE_STMNT = "INSERT INTO " + IMAGES_TABLE_NAME + " (" +
            IMAGES_COLUMN_URL + ","
            + IMAGES_COLUMN_PROVIDER + ","
            + IMAGES_COLUMN_IMAGE + ","
            + IMAGES_COLUMN_LINKPAGE + ") VALUES(?,?,?,?)";

    /*
    there is actuaaly ugly request, but it should works on very small databases
     */
    private static final String DROP_OLD_RECORDS = "DELETE FROM " + IMAGES_TABLE_NAME +
            " WHERE " + IMAGES_COLUMN_ID + " not in (SELECT " +
            IMAGES_COLUMN_ID + " FROM " +
            IMAGES_TABLE_NAME + " ORDER BY " + IMAGES_COLUMN_DATE_INSERTED + " Desc limit " + KEEP_LAST_IMAGES_NUMBER + ")";


    public boolean isUrlAlreadyDownloaded(ImageDescription imageDescription) {
        if (imageDescription!=null && imageDescription.getLinkPage()!=null) {
            SQLiteDatabase db = mImageDBHelper.getReadableDatabase();
            Cursor c = db.query(IMAGES_TABLE_NAME,
                    new String[]{IMAGES_COLUMN_ID},
                    IMAGES_COLUMN_LINKPAGE + " = ?",
                    new String[]{imageDescription.getLinkPage()},
                    null,
                    null,
                    null
            );
            if (c.getCount() > 0) {
                return true;
            }
            c.close();
        }
        return false;
    }

    /**
     * @param url      - ImageDescription url
     * @param provider - String name human readable of the image provider like Flickr, natgeo, gopro...
     * @param linkPage - String link
     * @param buffer   - bunary data of the image
     */
    public void putImage(String url, String provider, String linkPage, byte[] buffer) {
        //put image with now date
        SQLiteDatabase db = mImageDBHelper.getWritableDatabase();
        if (db != null) {
            SQLiteStatement insertStmt = db.compileStatement(INSERT_IMAGE_STMNT);
            insertStmt.bindString(1, url);
            insertStmt.bindString(2, provider);
            insertStmt.bindBlob(3, buffer);
            insertStmt.bindString(4, linkPage);
            insertStmt.execute();
            insertStmt.clearBindings();
            deleteOldImages();
        }

    }

    public int deleteImage(long id) {
        SQLiteDatabase db = mImageDBHelper.getWritableDatabase();
        if (db != null) {
            return db.delete(IMAGES_TABLE_NAME, IMAGES_COLUMN_ID + "=?", new String[]{Long.toString(id)});
        }
        return -1;
    }

    private void deleteOldImages() {
        SQLiteDatabase db = mImageDBHelper.getWritableDatabase();
        if (db != null) {
            db.execSQL(DROP_OLD_RECORDS);
        }
    }

    /**
     * @param id - int, ImageDescription id
     * @return ImageDescription object
     */
    public ImageDescription getImageById(long id) {
        ImageDescription retImage = null;
        SQLiteDatabase db = mImageDBHelper.getReadableDatabase();
        String where = Long.toString(id);
        Cursor c = db.query(IMAGES_TABLE_NAME,
                new String[]{IMAGES_COLUMN_IMAGE, IMAGES_COLUMN_DATE_INSERTED,
                        IMAGES_COLUMN_URL, IMAGES_COLUMN_PROVIDER, IMAGES_COLUMN_LINKPAGE},
                IMAGES_COLUMN_ID + " = ?",
                new String[]{where},
                null,
                null,
                null
        );
        if (c.moveToNext()) {

            byte[] b = c.getBlob(0);
            if (b != null && b.length > 1) {
                retImage =
                        new ImageDescription(c.getString(2), c.getInt(1), c.getString(3), c.getString(4), b);
            }
        }
        return retImage;
    }

    /**
     * @return ArrayList<ImageDescription> - array list of images
     */
    public Cursor getLastImagesCursor() {
        SQLiteDatabase db = mImageDBHelper.getReadableDatabase();

        return db.query(IMAGES_TABLE_NAME,
                new String[]{IMAGES_COLUMN_IMAGE, IMAGES_COLUMN_DATE_INSERTED,
                        IMAGES_COLUMN_URL, IMAGES_COLUMN_PROVIDER, IMAGES_COLUMN_ID, IMAGES_COLUMN_LINKPAGE},
                IMAGES_COLUMN_DATE_INSERTED + "> ?",
                new String[]{"0"},
                null,
                null,
                IMAGES_COLUMN_DATE_INSERTED + " desc",
                IMAGES_LAST_IMAGES_LIMIT);

    }

    private class ImageDBHelper extends SQLiteOpenHelper {
        public ImageDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_IMAGES_TABLE);


        }

        @Override
        public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion == 1 && newVersion == 2) {
                db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE_NAME);
                this.onCreate(db);
            } else if (oldVersion == 2 && newVersion == 3) {

                try {
                    db.execSQL("Alter table " + IMAGES_TABLE_NAME + " add column '" + IMAGES_COLUMN_LINKPAGE + "' TEXT default ''");
                } catch (SQLException e) {
                    db.execSQL("DROP TABLE IF EXISTS " + IMAGES_TABLE_NAME);
                    this.onCreate(db);
                }
            }
        }
    }

    public static synchronized ImageStorage getInstance() {
        return sInstance;
    }

    public static synchronized ImageStorage getInstance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new ImageStorage(context);
        }
        return sInstance;
    }

    public Uri saveImageToExternal(Bitmap bm) {
        //Create Path to save Image
        Uri retVal = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + appFolder); //Creates app specific folder
        if (path.mkdirs() || path.isDirectory()) {
            try {
                File imageFile = File.createTempFile("wpdimage", ".png", path);
                FileOutputStream out = new FileOutputStream(imageFile);
                bm.compress(Bitmap.CompressFormat.PNG, 100, out); // Compress Image
                out.flush();
                out.close();
                retVal = Uri.fromFile(imageFile);
            } catch (IOException e) {
                //do nothing
            }
        }
        return retVal;
    }
}

