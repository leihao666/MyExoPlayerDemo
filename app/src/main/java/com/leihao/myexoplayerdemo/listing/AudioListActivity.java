package com.leihao.myexoplayerdemo.listing;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.util.ActivityUtils;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class AudioListActivity extends DaggerAppCompatActivity {

    @Inject
    AudioListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioListFragment audioListFragment = (AudioListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_listing);
        if (audioListFragment == null) {
            audioListFragment = fragment;
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), audioListFragment, R.id.fragment_listing);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//设置返回按钮：不应该退出程序---而是返回桌面
            Intent home = new Intent(Intent.ACTION_MAIN);
//            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
