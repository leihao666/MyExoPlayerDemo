package com.leihao.myexoplayerdemo.player;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ControlDispatcher;
import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.util.Assertions;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioPlayerBar extends FrameLayout {

    @BindView(R.id.progress_horizontal)
    ProgressBar progress_horizontal;
    @BindView(R.id.iv_pic)
    ImageView iv_pic;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_artist)
    TextView tv_artist;
    @BindView(R.id.iv_play)
    ImageView iv_play;
    private final Timeline.Window window;
    private final Runnable updateProgressAction;

    private Animation animation;
    private final Drawable playButtonDrawable;
    private final Drawable pauseButtonDrawable;
    private final String playButtonContentDescription;
    private final String pauseButtonContentDescription;
    private boolean wasPlayWhenReady;
    private int lastPlaybackState;

    private Player player;
    private ControlDispatcher controlDispatcher;
    @Nullable
    private PlaybackPreparer playbackPreparer;
    private final Player.EventListener eventListener;

    private List<AudioBean> audioBeans;

    public AudioPlayerBar(Context context) {
        this(context, null);
    }

    public AudioPlayerBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioPlayerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        window = new Timeline.Window();
        controlDispatcher = new DefaultControlDispatcher();
        updateProgressAction = this::updateProgress;

        LayoutInflater.from(context).inflate(R.layout.audio_player_bar, this);
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
                updateProgress();
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                updateNavigation();
            }
        };
        iv_play.setOnClickListener(v -> {
            if (isPlaying()) {
                controlDispatcher.dispatchSetPlayWhenReady(player, false);
            } else {
                if (player.getPlaybackState() == Player.STATE_IDLE) {
                    if (playbackPreparer != null) {
                        playbackPreparer.preparePlayback();
                    }
                } else if (player.getPlaybackState() == Player.STATE_ENDED) {
                    controlDispatcher.dispatchSeekTo(player, player.getCurrentWindowIndex(), C.TIME_UNSET);
                }
                controlDispatcher.dispatchSetPlayWhenReady(player, true);
            }
        });
        animation = new RotateAnimation(0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(4000);
        Resources resources = context.getResources();
        playButtonDrawable = resources.getDrawable(R.mipmap.audiobar_play);
        pauseButtonDrawable = resources.getDrawable(R.mipmap.audiobar_pause);
        playButtonContentDescription =
                resources.getString(com.google.android.exoplayer2.ui.R.string.exo_controls_play_description);
        pauseButtonContentDescription =
                resources.getString(com.google.android.exoplayer2.ui.R.string.exo_controls_pause_description);
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
        Assertions.checkArgument(player == null || player.getApplicationLooper() == Looper.getMainLooper());
        if (this.player == player) {
            return;
        }
        if (this.player != null) {
            this.player.removeListener(eventListener);
        }
        this.player = player;
        if (player != null) {
            wasPlayWhenReady = player.getPlayWhenReady();
            lastPlaybackState = player.getPlaybackState();
            player.addListener(eventListener);
            if (lastPlaybackState != Player.STATE_IDLE) {
                updateNavigation();
            }
        }
        updateAll();
    }

    private void updateAll() {
        updatePlayPauseButton();
        updateNavigation();
        updateProgress();
    }

    private void updatePlayPauseButton() {
        if (!isVisible()) {
            return;
        }
        if (isPlaying()) {
            iv_pic.startAnimation(animation);
            iv_play.setImageDrawable(pauseButtonDrawable);
            iv_play.setContentDescription(pauseButtonContentDescription);
        } else {
            iv_pic.clearAnimation();
            iv_play.setImageDrawable(playButtonDrawable);
            iv_play.setContentDescription(playButtonContentDescription);
        }
        tv_name.setSelected(isPlaying());
        tv_artist.setSelected(isPlaying());
    }

    private void updateNavigation() {
        if (!isVisible()) {
            return;
        }
        Timeline timeline = player != null ? player.getCurrentTimeline() : null;
        boolean haveNonEmptyTimeline = timeline != null && !timeline.isEmpty();
        if (haveNonEmptyTimeline && !player.isPlayingAd()) {
            int windowIndex = player.getCurrentWindowIndex();
            timeline.getWindow(windowIndex, window);
        }
        updateAudioInfo();
    }

    private void updateAudioInfo() {
        if (!isVisible()) {
            return;
        }
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
                .into(iv_pic);
        tv_name.setText(bean.speechName);
        tv_artist.setText(bean.speechDesc);
    }

    private void updateProgress() {
        if (!isVisible()) {
            return;
        }

        long position = 0;
        long duration = 0;
        if (player != null) {
            long currentWindowTimeBarOffsetMs = 0;
            long durationUs = 0;
            Timeline timeline = player.getCurrentTimeline();
            if (!timeline.isEmpty()) {
                int currentWindowIndex = player.getCurrentWindowIndex();
                currentWindowTimeBarOffsetMs = C.usToMs(durationUs);
                timeline.getWindow(currentWindowIndex, window);
                durationUs += window.durationUs;
            }
            duration = C.usToMs(durationUs);
            position = currentWindowTimeBarOffsetMs + player.getContentPosition();
        }
        progress_horizontal.setProgress((int) (position / 1000));
        progress_horizontal.setMax((int) (duration / 1000));

        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                float playbackSpeed = player.getPlaybackParameters().speed;
                if (playbackSpeed <= 0.1f) {
                    delayMs = 1000;
                } else if (playbackSpeed <= 5f) {
                    long mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1 / playbackSpeed));
                    long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - (position % mediaTimeUpdatePeriodMs);
                    if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                        mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                    }
                    delayMs =
                            playbackSpeed == 1 ? mediaTimeDelayMs : (long) (mediaTimeDelayMs / playbackSpeed);
                } else {
                    delayMs = 200;
                }
            } else {
                delayMs = 1000;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }

    /**
     * Returns whether the controller is currently visible.
     */
    public boolean isVisible() {
        return getVisibility() == VISIBLE;
    }

    private boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    /**
     * Sets the {@link PlaybackPreparer}.
     *
     * @param playbackPreparer The {@link PlaybackPreparer}.
     */
    public void setPlaybackPreparer(@Nullable PlaybackPreparer playbackPreparer) {
        this.playbackPreparer = playbackPreparer;
    }

    public boolean seekTo(int windowIndex) {
        return controlDispatcher.dispatchSeekTo(player, windowIndex, C.TIME_UNSET);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    /**
     * Called to process media key events. Any {@link KeyEvent} can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false;
        }
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getRepeatCount() == 0) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, !player.getPlayWhenReady());
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        controlDispatcher.dispatchSetPlayWhenReady(player, true);
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        controlDispatcher.dispatchSetPlayWhenReady(player, false);
                        break;
                    default:
                        break;
                }
            }
        }
        return true;
    }

    @SuppressLint("InlinedApi")
    private static boolean isHandledMediaKey(int keyCode) {
        return keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE;
    }
}
