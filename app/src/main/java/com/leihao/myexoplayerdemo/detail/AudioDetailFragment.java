package com.leihao.myexoplayerdemo.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.di.ActivityScoped;
import com.leihao.myexoplayerdemo.player.AudioPlayerService;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

@ActivityScoped
public class AudioDetailFragment extends DaggerFragment {

    @BindView(R.id.iv_background)
    ImageView iv_background;
    @BindView(R.id.iv_pic)
    ImageView iv_pic;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_artist)
    TextView tv_artist;
    @BindView(R.id.playerView)
    PlayerView playerView;

    private AudioBean bean;
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
            AudioBean bean = (AudioBean) getArguments().get(AppConstants.AUDIO_BEAN);
            if (bean != null) {
                this.bean = bean;
                intent = new Intent(getContext(), AudioPlayerService.class);
                playerView.setUseController(true);
                playerView.showController();
                playerView.setControllerAutoShow(true);
                playerView.setControllerHideOnTouch(false);
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
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.NORMAL);

        Glide.with(this)
                .asBitmap()
                .load(bean.speechImage)
                .apply(options)
                .into(new BitmapImageViewTarget(iv_pic) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(bitmap, transition);
                        Palette.from(bitmap).generate(palette -> setBackgroundColor(palette, iv_background));
                    }
                });

        tv_name.setText(bean.speechName);
        tv_artist.setText(bean.speechDesc);
    }

    private void setBackgroundColor(Palette palette, View view) {
        view.setBackgroundColor(palette.getVibrantColor(getResources().getColor(R.color.black_translucent_60)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
