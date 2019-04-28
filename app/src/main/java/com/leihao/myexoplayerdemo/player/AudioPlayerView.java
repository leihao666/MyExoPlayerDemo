package com.leihao.myexoplayerdemo.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.util.Assertions;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class AudioPlayerView extends FrameLayout {

    @BindView(R.id.iv_background)
    ImageView iv_background;
    @BindView(R.id.iv_pic)
    CircleImageView iv_pic;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_artist)
    TextView tv_artist;
    @Nullable
    private final PlayerControlView controller;
    private final Timeline.Window window;

    private Animation animation;
    private boolean wasPlayWhenReady;
    private int lastPlaybackState;

    private Player player;
    private final Player.EventListener eventListener;

    private List<AudioBean> audioBeans;

    public AudioPlayerView(Context context) {
        this(context, null);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        window = new Timeline.Window();

        LayoutInflater.from(context).inflate(R.layout.audio_player_view, this);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        ButterKnife.bind(this);

        eventListener = new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if ((wasPlayWhenReady != playWhenReady && playbackState != Player.STATE_IDLE)
                        || lastPlaybackState != playbackState) {
                    updateNavigation();
                }
                wasPlayWhenReady = playWhenReady;
                lastPlaybackState = playbackState;
                updatePlayPauseButton();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                updateNavigation();
            }
        };

        // Playback control view.
        View controllerPlaceholder = findViewById(R.id.exo_controller_placeholder);
        if (controllerPlaceholder != null) {
            // Propagate attrs as playbackAttrs so that PlayerControlView's custom attributes are
            // transferred, but standard FrameLayout attributes (e.g. background) are not.
            this.controller = new PlayerControlView(context, null, 0, attrs);
            controller.setLayoutParams(controllerPlaceholder.getLayoutParams());
            ViewGroup parent = ((ViewGroup) controllerPlaceholder.getParent());
            int controllerIndex = parent.indexOfChild(controllerPlaceholder);
            parent.removeView(controllerPlaceholder);
            parent.addView(controller, controllerIndex);
        } else {
            this.controller = null;
        }

        animation = new RotateAnimation(0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(4000);
    }

    public void setAudioList(List<AudioBean> audioBeans) {
        this.audioBeans = audioBeans;
    }

    /**
     * Returns the {@link Player} currently being controlled by this view, or null if no player is
     * set.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the {@link Player} to control.
     *
     * @param player The {@link Player} to control, or {@code null} to detach the current player. Only
     *               players which are accessed on the main thread are supported ({@code
     *               player.getApplicationLooper() == Looper.getMainLooper()}).
     */
    public void setPlayer(@Nullable Player player) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper());
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(eventListener);
        }
        this.player = player;
        if (controller != null) {
            controller.setPlayer(player);
        }
        updateAll();
        if (player != null) {
            wasPlayWhenReady = player.getPlayWhenReady();
            lastPlaybackState = player.getPlaybackState();
            player.addListener(eventListener);
            if (lastPlaybackState != Player.STATE_IDLE) {
                updateNavigation();
            }
        }
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
    }

    private void updatePlayPauseButton() {
        if (isPlaying()) {
            iv_pic.startAnimation(animation);
        } else {
            iv_pic.clearAnimation();
        }
        tv_name.setSelected(isPlaying());
        tv_artist.setSelected(isPlaying());
    }

    private boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    private void updateNavigation() {
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
        }
        updateAudioInfo();
    }

    private void updateAudioInfo() {
        AudioBean bean = null;
        if (player != null) {
            int windowIndex = player.getCurrentWindowIndex();
            if (audioBeans != null && audioBeans.size() > windowIndex) {
                bean = audioBeans.get(windowIndex);
            }
        }
        if (bean == null) {
            return;
        }
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.NORMAL);
        Glide.with(getContext())
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

    /**
     * Sets the {@link PlaybackPreparer}.
     *
     * @param playbackPreparer The {@link PlaybackPreparer}.
     */
    public void setPlaybackPreparer(@Nullable PlaybackPreparer playbackPreparer) {
        Assertions.checkState(controller != null);
        controller.setPlaybackPreparer(playbackPreparer);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return controller.dispatchMediaKeyEvent(event);
    }
}
