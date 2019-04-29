package com.leihao.myexoplayerdemo.detail;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.util.ActivityUtils;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.support.DaggerAppCompatActivity;

public class AudioDetailActivity extends DaggerAppCompatActivity {

    @Inject
    Lazy<AudioDetailFragment> audioDetailFragmentProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_detail);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(AppConstants.AUDIO_BEAN_LIST)) {
                ArrayList<AudioBean> audioBeans = extras.getParcelableArrayList(AppConstants.AUDIO_BEAN_LIST);
                if (audioBeans != null) {
                    AudioDetailFragment audioDetailFragment = (AudioDetailFragment) getSupportFragmentManager().findFragmentById(R.id.audio_details_container);

                    if (audioDetailFragment == null) {
                        audioDetailFragment = audioDetailFragmentProvider.get();
                        Bundle args = new Bundle();
                        args.putParcelableArrayList(AppConstants.AUDIO_BEAN_LIST, audioBeans);
                        audioDetailFragment.setArguments(args);
                        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), audioDetailFragment, R.id.audio_details_container);
                    }
                }
            }
        }
    }
}
