package com.shingrus.wpdaily;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class WPDMainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String _log_tag = "WPD/WPDMainActivity";
    private static boolean isUpdating = false;
    private ImageCursorAdapter imageCursorAdapter = null;

    /**
     * Implements OnRefreshListener
     */
    @Override
    public void onRefresh() {
        Log.d(_log_tag, "OnRefresh");
        if (!isUpdating) {
            new UpdateImages().execute(null, null, null);
        }

    }


    class UpdateImages extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.setRefreshing(true);
            isUpdating = true;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean retVal = false;
            SetWallPaper setWallPaper = SetWallPaper.getSetWallPaper();
            return  setWallPaper.updateWallPaperImage();
        }

        @Override
        protected void onPostExecute(Boolean hasnewImages) {
            isUpdating = false;
            mSwipeRefreshLayout.setRefreshing(false);
            if (hasnewImages) {
                new GetImagesFromStorage().execute(null, null, null);
            }

        }

    }

    class GetImagesFromStorage extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            ImageStorage storage = ImageStorage.getInstance();
            return storage.getLastImagesCursor();
        }

        @Override
        protected void onPostExecute(Cursor c) {

            if (imageCursorAdapter != null) {
                imageCursorAdapter.changeCursor(c);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ListView listView = (ListView) findViewById(R.id.images_list);
        imageCursorAdapter = new ImageCursorAdapter(WPDMainActivity.this, null);
        listView.setAdapter(imageCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(_log_tag, "Click on : "+ id);
                Intent intent = new Intent(WPDMainActivity.this, ShowImage.class);
                intent.putExtra(ShowImage.IMAGE_ID_KEY,id);
                startActivity(intent);
            }
        });

//        mSwipeRefreshLayout.setColorScheme(R.color.blue, R.color.green, R.color.yellow, R.color.red);

//        ListView lv = (ListView) findViewById(R.id.images_list);

        //start update
        new UpdateImages().execute(null, null, null);
//        getLoaderManager().initLoader(GET_IMAGES,null, this);
        //Load images
        new GetImagesFromStorage().execute(null, null, null);

    }

    @Override
    protected void onDestroy() {
        imageCursorAdapter.changeCursor(null);
        super.onDestroy();
        Log.d(_log_tag, "Destroy main activity");
    }
}
