package com.leihao.myexoplayerdemo.listing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.detail.AudioDetailActivity;
import com.leihao.myexoplayerdemo.di.ActivityScoped;
import com.leihao.myexoplayerdemo.player.AudioPlayerBar;
import com.leihao.myexoplayerdemo.player.AudioPlayerService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

@ActivityScoped
public class AudioListFragment extends DaggerFragment implements AudioListContract.View {

    @Inject
    AudioListContract.Presenter mPresenter;

    @BindView(R.id.audio_listing)
    RecyclerView audioListing;
    @BindView(R.id.player_bar)
    AudioPlayerBar player_bar;

    private SimpleExoPlayer player;
    private AudioPlayerService mService;
    private Intent intent;
    private boolean mBound = false;

    private ArrayList<AudioBean> audioBeans = new ArrayList<>();
    private AudioListingAdapter adapter;
    private Unbinder unbinder;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            initializePlayer();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Inject
    public AudioListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_audio_list, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        initViewEvent();
        return rootView;
    }

    private void initViewEvent() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        audioListing.setLayoutManager(layoutManager);
        adapter = new AudioListingAdapter(audioBeans, audioBean -> {
            int position = audioBeans.indexOf(audioBean);
            player_bar.seekTo(position);
            player.setPlayWhenReady(true);
        });
        audioListing.setAdapter(adapter);

        player_bar.setAudioList(audioBeans);
        player_bar.setOnClickListener(v -> showAudioDetailUi());
    }

    private void showAudioDetailUi() {
        int window = player.getCurrentWindowIndex();
        if (window >= audioBeans.size()) {
            return;
        }
        AudioBean bean = audioBeans.get(window);
        Intent intent = new Intent(getContext(), AudioDetailActivity.class);
        intent.putExtra(AppConstants.AUDIO_BEAN, bean);
        startActivity(intent);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.takeView(this);
        mPresenter.loadAudioList();
        intent = new Intent(getContext(), AudioPlayerService.class);
        startService();
    }

    private void startService() {
        intent.putParcelableArrayListExtra(AppConstants.AUDIO_BEAN_LIST, audioBeans);
        getActivity().startService(intent); // 开启服务
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        initializePlayer();
    }

    private void initializePlayer() {
        if (mBound) {
            player = mService.getPlayerInstance();
            player_bar.setPlayer(player);
        }
    }

    @Override
    public void setProgressIndicator(boolean active) {
    }

    @Override
    public void showAudioList(List<AudioBean> audioList) {
        this.audioBeans.clear();
        this.audioBeans.addAll(audioList);
        audioListing.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();

        startService();
    }

    @Override
    public void showLoadingAudioListError(String errorMessage) {
        Snackbar.make(audioListing, errorMessage, Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
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
        mPresenter.dropView();
        unbinder.unbind();
        player.release();
        player = null;
    }
}
