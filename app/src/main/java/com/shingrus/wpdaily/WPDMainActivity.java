package com.shingrus.wpdaily;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
    private UpdateReceiver updateReceiver = new UpdateReceiver();
    IntentFilter filter = new IntentFilter(WPUpdateService.UPDATE_ACTIVITY_ACTION);
    public static final String SKIP_WELCOME_CHECK_ACTION = "com.shingrus.wpdaily.action.skip_welcome";

    /**
     * Implements OnRefreshListener
     */
    @Override
    public void onRefresh() {
        Log.d(_log_tag, "OnRefresh");
        if (!isUpdating) {
            updateImages();
        }

    }

    public class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(WPUpdateService.RESULT_EXTRA)) {
                SetWallPaper.UpdateResult updateResult = (SetWallPaper.UpdateResult) intent.getSerializableExtra(WPUpdateService.RESULT_EXTRA);
                if (updateResult == SetWallPaper.UpdateResult.SUCCESS || updateResult == SetWallPaper.UpdateResult.ALREADY_SET)
                    getImagesFromStorage();
                Log.d(_log_tag, "Got update  to activity result: " + updateResult);
            }
            if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);
            isUpdating = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(updateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        updateImages();
    }

    private void updateImages() {
        Intent intent = new Intent(this.getApplicationContext(), WPUpdateService.class);
        startService(intent);
        isUpdating = true;

    }

    private void getImagesFromStorage() {

        new AsyncTask<Void, Void, Cursor>() {
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

        }.execute(null, null, null);
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
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Intent startIntent = getIntent();
        if (!SKIP_WELCOME_CHECK_ACTION.equals(startIntent.getAction()) &&
                !pref.getBoolean(getString(R.string.WelcomeScreenShowedKey), false)) {
            Intent newActivity = new Intent(this, WelcomeActivity.class);
            startActivity(newActivity);
            finish();
        } else {
            setContentView(R.layout.activity_main);
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
            mSwipeRefreshLayout.setOnRefreshListener(this);
            ListView listView = (ListView) findViewById(R.id.images_list);
            imageCursorAdapter = new ImageCursorAdapter(WPDMainActivity.this, null);
            listView.setAdapter(imageCursorAdapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    return false;
                }
            });
            registerForContextMenu(listView);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.d(_log_tag, "Click on : "+ id);
//                Intent intent = new Intent(WPDMainActivity.this, ShowImage.class);
//                intent.putExtra(ShowImage.IMAGE_ID_KEY,id);
//                startActivity(intent);
//            }
//        });


            //Load images
            getImagesFromStorage();
            //start update
            updateImages();
        }

    }

    @Override
    protected void onDestroy() {
        if (imageCursorAdapter != null)
            imageCursorAdapter.changeCursor(null);
        super.onDestroy();
        Log.d(_log_tag, "Destroy main activity");
    }
}
