package com.shingrus.wpdaily;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;

/**
 * Created by shingrus on 06/12/15.
 */
public class ImageStorage {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Images.db";
    private static final String IMAGES_TABLE_NAME = "Images";
    public static final String IMAGES_COLUMN_ID = "_id";
    public static final String IMAGES_COLUMN_URL = "url";
    public static final String IMAGES_COLUMN_IMAGE = "image";
    public static final String IMAGES_COLUMN_DATE_INSERTED = "inserted_at";
    public static final String IMAGES_COLUMN_PROVIDER = "provider";
    private static final String IMAGES_LAST_IMAGES_LIMIT = "10";
    static final String CREATE_IMAGES_TABLE =   "CREATE TABLE '"+ IMAGES_TABLE_NAME +"' (" +
            "'_id INTEGER PRIMARY KEY AUTOINCREMENT "+
            "'url' TEXT UNIQ, " +
            "'inserted_at' INTEGER default (strftime('%s','now'))," +
            "'provider' TEXT default ''," +
            "'image' BLOB,"+
            "CREATE UNIQ INDEX url_index on (url))";


    private static ImageStorage sInstance;
    private static Context ctx;


    private ImageStorage(Context context) {
        this.ctx = context;

    }



    static final String INSERT_IMAGE_STMNT  =   "INSERT INTO " + IMAGES_TABLE_NAME + " ("+
            IMAGES_COLUMN_URL+","+IMAGES_COLUMN_PROVIDER+","+IMAGES_COLUMN_IMAGE+") VALUES(?,?,?)";
    public void putImage (String url, String provider, byte[] buffer) {
        //put image with now date
        ImageDBHelper dbHelper = new ImageDBHelper(this.ctx);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if ( db != null) {
            SQLiteStatement insertStmt      =   db.compileStatement(INSERT_IMAGE_STMNT);
            insertStmt.bindString(1,url);
            insertStmt.bindString(2, provider);
            insertStmt.bindBlob(3, buffer);

            insertStmt.executeInsert();
            insertStmt.clearBindings();
        }

    }

    /**
     *
     * @param
     * @return ArrayList<Image> - array list of images
     */
    public Cursor getLastImagesCursor() {
        ArrayList<Image> resultArray = new ArrayList<>();
        SQLiteDatabase db = new ImageDBHelper(this.ctx).getReadableDatabase();

        Cursor c = db.query(IMAGES_TABLE_NAME,
                new String[]{IMAGES_COLUMN_IMAGE, IMAGES_COLUMN_DATE_INSERTED, IMAGES_COLUMN_URL, IMAGES_COLUMN_ID},
                IMAGES_COLUMN_DATE_INSERTED + "> ?",
                new String[]{"0"},
                null,
                null,
                IMAGES_COLUMN_DATE_INSERTED + " desc",
                IMAGES_LAST_IMAGES_LIMIT);
        return c;
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
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion ==1 && newVersion == 2) {
                db.execSQL("DROP TABLE IF EXISTS Images");
                this.onCreate(db);
            }
        }
    }

    public static synchronized ImageStorage getInstance() {
        return sInstance;
    }
    public static synchronized ImageStorage getInstance(Context context) {
        if (sInstance == null && context!=null) {
            sInstance = new ImageStorage(context);
        }
        return sInstance;
    }
}

