package com.leihao.myexoplayerdemo.listing;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.di.ActivityScoped;
import com.leihao.myexoplayerdemo.player.AudioPlayerBar;

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

    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer player;

    private List<AudioBean> audioBeans = new ArrayList<>();
    private AudioListingAdapter adapter;
    private Unbinder unbinder;

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
            player_bar.setVisibility(View.VISIBLE);
            int position = audioBeans.indexOf(audioBean);
            player_bar.seekTo(position);
            player.setPlayWhenReady(true);
        });
        audioListing.setAdapter(adapter);

        player_bar.setAudioList(audioBeans);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.takeView(this);
        initPlayer();
    }

    private void initPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(getContext());
        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getContext(), "MyExoPlayerDemo"));
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player_bar.setPlayer(player);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadAudioList();
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

        // This is the MediaSource representing the media to be played.
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (AudioBean audioBean : this.audioBeans) {
            if (audioBean.speechUrl != null && !audioBean.speechUrl.isEmpty()) {
                MediaSource source = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(audioBean.speechUrl));
                concatenatedSource.addMediaSource(source);
            }
        }
        // Prepare the player with the source.
        player.prepare(concatenatedSource);
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
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.dropView();
        unbinder.unbind();
    }
}
