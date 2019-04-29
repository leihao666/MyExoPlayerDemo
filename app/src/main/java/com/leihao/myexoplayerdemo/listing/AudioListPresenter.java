package com.leihao.myexoplayerdemo.listing;

import androidx.annotation.NonNull;

import com.leihao.myexoplayerdemo.data.AudioBean;
import com.leihao.myexoplayerdemo.data.network.AudioWebService;
import com.leihao.myexoplayerdemo.data.network.wrapper.AudioListWrapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Listens to user actions from the UI ({@link AudioListFragment}), retrieves the data and updates
 * the UI as required.
 * <p/>
 * By marking the constructor with {@code @Inject}, Dagger injects the dependencies required to
 * create an instance of the StatisticsPresenter (if it fails, it emits a compiler error). It uses
 * {@link AudioListModule} to do so.
 * <p/>
 * Dagger generated code doesn't require public access to the constructor or class, and
 * therefore, to ensure the developer doesn't instantiate the class manually and bypasses Dagger,
 * it's good practice minimise the visibility of the class/constructor as much as possible.
 **/
final public class AudioListPresenter implements AudioListContract.Presenter {

    private final AudioWebService audioWebService;

    private AudioListContract.View view;

    @NonNull
    private CompositeDisposable mCompositeDisposable;

    /**
     * Dagger strictly enforces that arguments not marked with {@code @Nullable} are not injected
     * with {@code @Nullable} values.
     */
    @Inject
    AudioListPresenter(AudioWebService audioWebService) {
        this.audioWebService = audioWebService;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void loadAudioList() {
        if (isViewAttached()) {
            view.setProgressIndicator(true);
        }
        Disposable disposable = audioWebService.getAudioList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(AudioListWrapper::getData)
                .map(audioBeans -> {//清除空数据
                    List<AudioBean> result = new ArrayList<>();
                    for (AudioBean audioBean : audioBeans) {
                        if (audioBean.speechUrl != null && !audioBean.speechUrl.isEmpty()) {
                            result.add(audioBean);
                        }
                    }
                    return result;
                })
                .subscribe(
                        // onNext
                        audioBeans -> {
                            // The view may not be able to handle UI updates anymore
                            if (!isViewAttached()) {
                                return;
                            }
                            view.showAudioList(audioBeans);
                        },
                        // onError
                        throwable -> {
                            if (isViewAttached()) {
                                view.showLoadingAudioListError(throwable.getMessage());
                            }
                        },
                        // onCompleted
                        () -> {
                            if (isViewAttached()) {
                                view.setProgressIndicator(false);
                            }
                        });
        mCompositeDisposable.add(disposable);
    }

    private boolean isViewAttached() {
        return view != null && view.isActive();
    }

    @Override
    public void takeView(AudioListContract.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        mCompositeDisposable.clear();
        this.view = null;
    }
}
