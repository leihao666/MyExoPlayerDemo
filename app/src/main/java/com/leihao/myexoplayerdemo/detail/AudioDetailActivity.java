package com.leihao.myexoplayerdemo.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.util.ActivityUtils;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class AudioDetailActivity extends DaggerAppCompatActivity {

    @Inject
    AudioDetailFragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_detail);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(AppConstants.AUDIO_BEAN)) {
                AudioBean bean = extras.getParcelable(AppConstants.AUDIO_BEAN);
                if (bean != null) {
                    AudioDetailFragment audioDetailFragment = (AudioDetailFragment) getSupportFragmentManager().findFragmentById(R.id.audio_details_container);

                    if (audioDetailFragment == null) {
                        audioDetailFragment = fragment;
                        Bundle args = new Bundle();
                        args.putParcelable(AppConstants.AUDIO_BEAN, bean);
                        audioDetailFragment.setArguments(args);
                        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), audioDetailFragment, R.id.audio_details_container);
                    }
                }
            }
        }
    }
}
