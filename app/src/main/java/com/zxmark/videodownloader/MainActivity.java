package com.zxmark.videodownloader;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.service.IDownloadBinder;
import com.zxmark.videodownloader.service.IDownloadCallback;
import com.zxmark.videodownloader.service.TLRequestParserService;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.FileComparator;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.URLMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RunnableFuture;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private EditText mUrlEditText;
    private Button mDownloadBtn;
    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private List<DownloaderBean> mDataList;
    private MainListRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeDownloadService();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mUrlEditText = (EditText) findViewById(R.id.paste_url);
        mUrlEditText.setInputType(InputType.TYPE_NULL);
        findViewById(R.id.btn_download).setOnClickListener(this);
        findViewById(R.id.btn_paste).setOnClickListener(this);
        mListView = (RecyclerView) findViewById(R.id.list);
        mListView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false);

        mListView.setLayoutManager(mLayoutManager);

        File file = DownloadUtil.getHomeDirectory();
        File[] fileArray = file.listFiles();
        if (fileArray != null && fileArray.length > 0) {
            mDataList = new ArrayList<DownloaderBean>();
            for (File item : fileArray) {
                DownloaderBean bean = new DownloaderBean();
                bean.file = item;
                bean.progress = 0;
                mDataList.add(bean);
            }
            Collections.sort(mDataList, new FileComparator());
            mAdapter = new MainListRecyclerAdapter(mDataList);
            mListView.setAdapter(mAdapter);
        }

        handleSendIntent();
    }

    private void handleSendIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            LogUtil.v("tl", "action:" + action + ":" + type);
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                // Update UI to reflect text being shared
                LogUtil.v("TL", "sharedText:" + sharedText);
                String url = URLMatcher.getHttpURL(sharedText);
                mUrlEditText.setText(url);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSendIntent();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_download) {
            String downloadUrl = mUrlEditText.getText().toString();
            if (!TextUtils.isEmpty(downloadUrl)) {
                Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show();
                startDownload(downloadUrl);
            }
        } else if (v.getId() == R.id.btn_paste) {
            final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            String pastUrl = cb.getText().toString();
            if (!TextUtils.isEmpty(pastUrl)) {
                Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show();
                startDownload(pastUrl);
            }
        }
    }

    private void startDownload(final String url) {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.REQUEST_VIDEO_URL_ACTION);
        intent.putExtra(Globals.EXTRAS, url);
        startService(intent);
    }

    private void subscribeDownloadService() {
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private IDownloadBinder mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IDownloadBinder.Stub.asInterface(service);

            try {
                mService.registerCallback(mRemoteCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mService.unregisterCallback(mRemoteCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
        }
    };

    private IDownloadCallback.Stub mRemoteCallback = new IDownloadCallback.Stub() {
        @Override
        public void onPublishProgress(final String key, final int progress) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DownloaderBean bean = new DownloaderBean();
                    bean.file = new File(key);
                    int index = mDataList.indexOf(bean);
                    LogUtil.v("TL","index:" + index);
                    if (index > -1) {
                        DownloaderBean cacheBean = mDataList.get(index);
                        cacheBean.progress = progress;
                        LogUtil.v("TL", "onPublishProgress:" + progress);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onStartDownload(final String path) throws RemoteException {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter == null) {
                        mDataList = new ArrayList<>();
                        mAdapter = new MainListRecyclerAdapter(mDataList);
                        mListView.setAdapter(mAdapter);
                    }

                    DownloaderBean bean = new DownloaderBean();
                    bean.file = new File(path);
                    bean.progress = 0;
                    mDataList.add(bean);
                    Collections.sort(mDataList,new FileComparator());
                    mAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void onDownloadSuccess(String path) throws RemoteException {

        }
    };

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
