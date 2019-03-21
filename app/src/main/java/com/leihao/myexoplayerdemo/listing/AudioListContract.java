package com.leihao.myexoplayerdemo.listing;

import com.leihao.myexoplayerdemo.BasePresenter;
import com.leihao.myexoplayerdemo.BaseView;
import com.leihao.myexoplayerdemo.data.AudioBean;

import java.util.List;

public interface AudioListContract {

    interface View extends BaseView<Presenter> {

        void setProgressIndicator(boolean active);

        void showAudioList(List<AudioBean> audioList);

        void showLoadingAudioListError(String errorMessage);

        boolean isActive();
    }

    interface Presenter extends BasePresenter<View> {

        void loadAudioList();
    }
}
