package com.leihao.myexoplayerdemo.data.network;

import com.leihao.myexoplayerdemo.data.network.wrapper.AudioListWrapper;

import io.reactivex.Flowable;
import retrofit2.http.GET;

public interface AudioWebService {

    @GET("/speech/indexListSpeech")
    Flowable<AudioListWrapper> getAudioList();
}
