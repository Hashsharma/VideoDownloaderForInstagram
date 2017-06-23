package com.zxmark.videodownloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.imobapp.videodownloaderforinstagram.R;
import com.nineoldandroids.view.ViewHelper;
import com.umeng.analytics.MobclickAgent;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.adapter.MainViewPagerAdapter;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.main.GalleryPagerActivity;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.service.IDownloadBinder;
import com.zxmark.videodownloader.service.IDownloadCallback;
import com.zxmark.videodownloader.util.DeviceUtil;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;
import com.zxmark.videodownloader.util.URLMatcher;
import com.zxmark.videodownloader.util.Utils;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private ViewPager mMainViewPager;
    private MainViewPagerAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscribeDownloadService();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //TODO: dismiss float view
        FloatViewManager.getDefault().dismissFloatView();
        findViewById(R.id.ins_icon).setOnClickListener(this);
        mMainViewPager = (ViewPager) findViewById(R.id.viewPager);
        mMainViewPager.setOffscreenPageLimit(2);
        FragmentManager fm = getSupportFragmentManager();

        String params = null;
        if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            params = URLMatcher.getHttpURL(sharedText);
        }
        mViewPagerAdapter = new MainViewPagerAdapter(fm, params);
        mMainViewPager.setAdapter(mViewPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.slidindg_tabs);
        mTabLayout.setupWithViewPager(mMainViewPager);
        mMainViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    private void handleSendIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (sharedText != null) {
                // Update UI to reflect text being shared
                LogUtil.v("TL", "sharedText:" + sharedText);
                String url = URLMatcher.getHttpURL(sharedText);
                if (mViewPagerAdapter.getDownloadingFragment() != null) {
                    mViewPagerAdapter.getDownloadingFragment().receiveSendAction(url);
                }
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
        // getMenuInflater().inflate(R.menu.main, menu);
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
            Utils.openInstagram();
        } else if (id == R.id.nav_gallery) {
            if (mViewPagerAdapter.getDownloadingFragment() != null) {
                mViewPagerAdapter.getDownloadingFragment().showHotToInfo();
            }
        } else if (id == R.id.nav_send) {
            Utils.sendMyApp();
        } else if (id == R.id.nav_rate) {
            Utils.rateUs5Star();
        } else if (id == R.id.nav_change_language) {
            showLocaleSelectDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_paste) {
//            String downloadUrl = mUrlEditText.getText().toString();
//            if (!TextUtils.isEmpty(downloadUrl)) {
//                Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show();
//                startDownload(downloadUrl);
//            }
//        } else if (v.getId() == R.id.btn_paste) {
//            final ClipboardManager cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            String pastUrl = cb.getText().toString();
//            if (!TextUtils.isEmpty(pastUrl)) {
//                Toast.makeText(this, "start download", Toast.LENGTH_SHORT).show();
//                startDownload(pastUrl);
//            }
        if (v.getId() == R.id.ins_icon) {
            Utils.openInstagram();
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
        public void onPublishProgress(final String pageURL, final int filePostion, final int progress) throws RemoteException {
            if (mViewPagerAdapter != null && mViewPagerAdapter.getDownloadingFragment() != null) {
                mViewPagerAdapter.getDownloadingFragment().publishProgress(pageURL, filePostion, progress);
            }
        }

        @Override
        public void onReceiveNewTask(final String pageURL) throws RemoteException {
            LogUtil.v("main", "onReceiveNewTask:" + pageURL);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter.getDownloadingFragment() != null) {
                        mViewPagerAdapter.getDownloadingFragment().onReceiveNewTask(pageURL);
                    }
                }
            });
        }

        @Override
        public void onStartDownload(final String path) throws RemoteException {
            LogUtil.v("start", "onStartDownload:" + path);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter.getDownloadingFragment() != null) {
                        mViewPagerAdapter.getDownloadingFragment().onStartDownload(path);
                    }
                }
            });

        }

        @Override
        public void onDownloadSuccess(final String path) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mViewPagerAdapter != null) {
                        if (mViewPagerAdapter.getDownloadingFragment() != null) {
                            mViewPagerAdapter.getDownloadingFragment().deleteVideoByPath(path);
                        }

                        if (mViewPagerAdapter.getVideoHistoryFragment() != null) {
                            mViewPagerAdapter.getVideoHistoryFragment().onAddNewDownloadedFile(path);
                        }
                    }


                }
            });
        }

        @Override
        public void onDownloadFailed(String path) throws RemoteException {

        }
    };

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    private void showLocaleSelectDialog() {
        DownloadingTaskList.SINGLETON.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {

                final String[] supportLanguages = getResources().getStringArray(R.array.support_languages);
                final String[] countryCode = getResources().getStringArray(R.array.support_languages_ccd);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.nav_change_language)
                                .setSingleChoiceItems(supportLanguages, PreferenceUtils.getCurrentLanguagePos(), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PreferenceUtils.saveCurrentLanguage(countryCode[which], which);
                                        Utils.changeLocale(countryCode[which]);
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
            }
        });
    }
}
