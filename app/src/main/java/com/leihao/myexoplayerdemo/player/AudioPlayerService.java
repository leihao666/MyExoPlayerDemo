package com.leihao.myexoplayerdemo.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.leihao.myexoplayerdemo.AppConstants;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.detail.AudioDetailActivity;
import com.leihao.myexoplayerdemo.util.DownloadUtil;

import java.util.ArrayList;

public class AudioPlayerService extends Service {

    private final IBinder mBinder = new LocalBinder();
    private SimpleExoPlayer player;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private PlayerNotificationManager playerNotificationManager;

    private ArrayList<AudioBean> audioBeans = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public SimpleExoPlayer getPlayerInstance() {
        if (player == null) {
            initPlayer();
        }
        return player;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //releasePlayer();
        audioBeans = intent.getParcelableArrayListExtra(AppConstants.AUDIO_BEAN_LIST);
        if (player == null) {
            initPlayer();
        }
        if (!audioBeans.isEmpty()) {
            preparePlay();
        }
        return START_STICKY;
    }

    private void initPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        // Produces DataSource instances through which media data is loaded.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));
        cacheDataSourceFactory = new CacheDataSourceFactory(
                DownloadUtil.getCache(this),
                dataSourceFactory,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        playerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(this, AppConstants.PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                AppConstants.PLAYBACK_NOTIFICATION_ID,
                new PlayerNotificationManager.MediaDescriptionAdapter() {
                    @Override
                    public String getCurrentContentTitle(Player player) {
                        int window = player.getCurrentWindowIndex();
                        return getTitle(window);
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        int window = player.getCurrentWindowIndex();
                        return createPendingIntent(window);
                    }

                    @Nullable
                    @Override
                    public String getCurrentContentText(Player player) {
                        int window = player.getCurrentWindowIndex();
                        return getDescription(window);
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        int window = player.getCurrentWindowIndex();
                        if (getLargeIconUri(window) != null) {
                            // load bitmap async
                            loadBitmap(getLargeIconUri(window), callback);
                            return getPlaceholderBitmap();
                        }
                        return null;
                    }
                }
        );
        playerNotificationManager.setNotificationListener(new PlayerNotificationManager.NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                stopSelf();
            }
        });
        playerNotificationManager.setPlayer(player);
    }

    private String getTitle(int window) {
        if (window >= audioBeans.size()) {
            return null;
        }
        AudioBean bean = audioBeans.get(window);
        return bean.speechName;
    }

    private String getDescription(int window) {
        if (window >= audioBeans.size()) {
            return null;
        }
        AudioBean bean = audioBeans.get(window);
        return bean.speechDesc;
    }

    private String getLargeIconUri(int window) {
        if (window >= audioBeans.size()) {
            return null;
        }
        AudioBean bean = audioBeans.get(window);
        return bean.speechImage;
    }

    private void loadBitmap(String iconUri, PlayerNotificationManager.BitmapCallback callback) {
        Glide.with(this)
                .asBitmap()
                .load(iconUri)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.onBitmap(resource);
                    }
                });
    }

    private Bitmap getPlaceholderBitmap() {
        return BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    }

    private PendingIntent createPendingIntent(int window) {
        if (window >= audioBeans.size()) {
            return null;
        }
        Intent resultIntent = new Intent(this, AudioDetailActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//关键的一步，设置启动模式
        resultIntent.putParcelableArrayListExtra(AppConstants.AUDIO_BEAN_LIST, audioBeans);
        return PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void preparePlay() {
        // This is the MediaSource representing the media to be played.
        ConcatenatingMediaSource concatenatedSource = new ConcatenatingMediaSource();
        for (AudioBean audioBean : this.audioBeans) {
            if (audioBean.speechUrl != null && !audioBean.speechUrl.isEmpty()) {
                MediaSource source = new ExtractorMediaSource.Factory(cacheDataSourceFactory).createMediaSource(Uri.parse(audioBean.speechUrl));
                concatenatedSource.addMediaSource(source);
            }
        }
        // Prepare the player with the source.
        player.prepare(concatenatedSource);
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        if (player != null) {
            playerNotificationManager.setPlayer(null);
            player.release();
            player = null;
        }
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
}
