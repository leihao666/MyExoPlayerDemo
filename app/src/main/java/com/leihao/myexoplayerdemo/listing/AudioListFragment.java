package com.leihao.myexoplayerdemo.listing;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
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

import static android.content.Context.NOTIFICATION_SERVICE;

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
    private PlayerNotificationManager playerNotificationManager;

    private List<AudioBean> audioBeans = new ArrayList<>();
    private AudioListingAdapter adapter;
    private Unbinder unbinder;

    private final String channelId = "voice_play";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, "音频播放", NotificationManager.IMPORTANCE_HIGH, null);
        }
        playerNotificationManager = new PlayerNotificationManager(
                getContext(),
                channelId,
                1,
                new DescriptionAdapter());
        playerNotificationManager.setPlayer(player);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    void createNotificationChannel(String id, String name, int importance, String desc) {
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager.getNotificationChannel(id) != null)
            return;
        NotificationChannel notificationChannel = new NotificationChannel(id, name, importance);
        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(false);

        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationChannel.setBypassDnd(true);
        notificationChannel.setDescription(desc);

        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadAudioList();
    }

    private class DescriptionAdapter implements PlayerNotificationManager.MediaDescriptionAdapter {

        @Override
        public String getCurrentContentTitle(Player player) {
            int window = player.getCurrentWindowIndex();
            return getTitle(window);
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

        @Nullable
        @Override
        public PendingIntent createCurrentContentIntent(Player player) {
            int window = player.getCurrentWindowIndex();
            return createPendingIntent(window);
        }
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
        Glide.with(getContext())
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
        AudioBean bean = audioBeans.get(window);
        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(getContext(), AudioListActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
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
        playerNotificationManager.setPlayer(null);
        player.release();
        player = null;
    }
}
