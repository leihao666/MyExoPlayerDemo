package com.leihao.myexoplayerdemo.detail;

import com.leihao.myexoplayerdemo.di.FragmentScoped;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AudioDetailModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract AudioDetailFragment audioDetailFragment();
}
