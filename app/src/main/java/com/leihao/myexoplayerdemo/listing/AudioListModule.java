package com.leihao.myexoplayerdemo.listing;

import com.leihao.myexoplayerdemo.di.ActivityScoped;
import com.leihao.myexoplayerdemo.di.FragmentScoped;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link AudioListPresenter}.
 */
@Module
public abstract class AudioListModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract AudioListFragment audioListFragment();

    @ActivityScoped
    @Binds
    abstract AudioListContract.Presenter audioListPresenter(AudioListPresenter presenter);
}
