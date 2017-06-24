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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.imobapp.videodownloaderforinstagram.BuildConfig;
import com.imobapp.videodownloaderforinstagram.R;
import com.nineoldandroids.view.ViewHelper;
import com.umeng.analytics.MobclickAgent;
import com.zxmark.videodownloader.adapter.MainListRecyclerAdapter;
import com.zxmark.videodownloader.adapter.MainViewPagerAdapter;
import com.zxmark.videodownloader.db.DownloaderDBHelper;
import com.zxmark.videodownloader.downloader.DownloadingTaskList;
import com.zxmark.videodownloader.downloader.VideoDownloadFactory;
import com.zxmark.videodownloader.floatview.FloatViewManager;
import com.zxmark.videodownloader.main.GalleryPagerActivity;
import com.zxmark.videodownloader.main.RatingAppActivity;
import com.zxmark.videodownloader.service.DownloadService;
import com.zxmark.videodownloader.service.IDownloadBinder;
import com.zxmark.videodownloader.service.IDownloadCallback;
import com.zxmark.videodownloader.util.DeviceUtil;
import com.zxmark.videodownloader.util.DownloadUtil;
import com.zxmark.videodownloader.util.EventUtil;
import com.zxmark.videodownloader.util.GPDataGenerator;
import com.zxmark.videodownloader.util.Globals;
import com.zxmark.videodownloader.util.LogUtil;
import com.zxmark.videodownloader.util.PreferenceUtils;
import com.zxmark.videodownloader.util.URLMatcher;
import com.zxmark.videodownloader.util.Utils;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    public static final String KEY = "main";

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
        EventUtil.getDefault().onEvent(KEY,"MainActivity.onCreate");
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
            if (Globals.TEST_FOR_GP) {
                if (BuildConfig.DEBUG) {
                    //showRatingDialog();
                    GPDataGenerator.saveGPTask();
                }
            }
            EventUtil.getDefault().onEvent("main","openInsByNav");
            Utils.openInstagram();
        } else if (id == R.id.nav_gallery) {
            EventUtil.getDefault().onEvent("main","showHowTo");
            if (mViewPagerAdapter.getDownloadingFragment() != null) {
                mViewPagerAdapter.getDownloadingFragment().showHotToInfo();
            }
        } else if (id == R.id.nav_send) {
            EventUtil.getDefault().onEvent("main","sendMyApp");
            Utils.sendMyApp();
        } else if (id == R.id.nav_rate) {
            EventUtil.getDefault().onEvent("main","rateUs");
            Utils.rateUs5Star();
        } else if (id == R.id.nav_change_language) {
            EventUtil.getDefault().onEvent("main","changeLanguage");
            showLocaleSelectDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void showRatingDialog() {
        if(PreferenceUtils.isShowedRateGuide()) {
            return;
        }
        EventUtil.getDefault().onEvent("main","showRatingDialog");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        View convertView = getLayoutInflater().inflate(R.layout.rating_app, null);
        builder.setView(convertView);
        final TextView ragingInfo = (TextView) convertView.findViewById(R.id.rating_info);
        final View ragingInfo2 = convertView.findViewById(R.id.rating_info_2);
        final RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.rating_us);
        final TextView ratingDetail = (TextView) convertView.findViewById(R.id.rate_detail);
        final int deepOrangeColor = getResources().getColor(R.color.deep_orange_color);
        final int greenColor = getResources().getColor(R.color.rating_bar_color);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ragingInfo.setVisibility(View.GONE);
                ragingInfo2.setVisibility(View.GONE);
                if (rating == 1.0f) {
                    ratingDetail.setText(R.string.ratingstar_1);
                } else if (rating > 1.0f && rating <= 2.0f) {
                    ratingDetail.setText(R.string.ratingstar_2);
                } else if (rating > 2.0f && rating <= 3.0f) {
                    ratingDetail.setText(R.string.ratingstar_3);
                } else if (rating > 3.0f && rating <= 4.0f) {
                    ratingDetail.setText(R.string.ratingstar_4);
                } else if (rating > 4.0f && rating <= 5.0f) {
                    ratingDetail.setText(R.string.ratingstar_5);
                }

                if (rating < 3.0f) {
                    ratingDetail.setTextColor(deepOrangeColor);
                } else {
                    ratingDetail.setTextColor(greenColor);
                }
                ratingDetail.setVisibility(View.VISIBLE);

            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("CLOSE", null);
        builder.setPositiveButton("RATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float rating = ratingBar.getRating();
                EventUtil.getDefault().onEvent("main","rating=" + rating);
                if (rating >= 3.0f) {
                    PreferenceUtils.showedRateGuide();
                    Utils.rateUs5Star();
                } else {
                    Utils.sendMeEmail();
                }
            }
        });
        builder.show();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ins_icon) {
            EventUtil.getDefault().onEvent("main","openInsByTitle");
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


                        //TODO:安装第三天后引导用户给评分
                        if (DownloaderDBHelper.SINGLETON.getDownloadedTaskCount() > 3 && System.currentTimeMillis() - Utils.getMyAppInstallTime() > 2 * 24 * 60 * 60 * 1000) {
                            showRatingDialog();
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
                                        EventUtil.getDefault().onEvent("la","coutryCode:" + countryCode[which]);
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
            }
        });
    }
}
