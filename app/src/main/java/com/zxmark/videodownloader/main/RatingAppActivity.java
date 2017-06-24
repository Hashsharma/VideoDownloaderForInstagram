package com.zxmark.videodownloader.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.imobapp.videodownloaderforinstagram.R;
import com.zxmark.videodownloader.BaseActivity;

/**
 * Created by fanlitao on 6/24/17.
 */

public class RatingAppActivity extends BaseActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去掉 title

        setContentView(R.layout.rating_app);


    }
}
