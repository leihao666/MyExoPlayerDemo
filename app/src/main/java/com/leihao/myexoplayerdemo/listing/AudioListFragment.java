package com.leihao.myexoplayerdemo.listing;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.di.ActivityScoped;

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
            Snackbar.make(audioListing, audioBean.speechName, Snackbar.LENGTH_SHORT).show();
        });
        audioListing.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.takeView(this);
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
