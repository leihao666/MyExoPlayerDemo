package com.leihao.myexoplayerdemo.listing;

import android.os.Bundle;

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
}
