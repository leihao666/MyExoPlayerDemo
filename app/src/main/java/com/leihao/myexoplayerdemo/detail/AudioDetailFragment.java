package com.leihao.myexoplayerdemo.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.di.ActivityScoped;
import com.leihao.myexoplayerdemo.player.AudioPlayerService;
import com.leihao.myexoplayerdemo.player.AudioPlayerView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

@ActivityScoped
public class AudioDetailFragment extends DaggerFragment {

    @BindView(R.id.player_view)
    AudioPlayerView playerView;

    private ArrayList<AudioBean> audioBeans;
    private AudioPlayerService mService;
    private Intent intent;
    private boolean mBound = false;

    private Unbinder unbinder;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Inject
    public AudioDetailFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio_detail, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initViewEvent();
        return rootView;
    }

    private void initViewEvent() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {

            ArrayList<AudioBean> audioBeans = getArguments().getParcelableArrayList(AppConstants.AUDIO_BEAN_LIST);
            if (audioBeans != null) {
                this.audioBeans = audioBeans;
                intent = new Intent(getContext(), AudioPlayerService.class);
            }
        }
    }

    private void initializePlayer() {
        if (mBound) {
            SimpleExoPlayer player = mService.getPlayerInstance();
            playerView.setPlayer(player);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        initializePlayer();
        setUI();
    }

    private void setUI() {
        playerView.setAudioList(audioBeans);
    }

    @Override
    public void onStop() {
        getActivity().unbindService(mConnection);
        mBound = false;
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        playerView.setPlayer(null);
        unbinder.unbind();
    }
}
