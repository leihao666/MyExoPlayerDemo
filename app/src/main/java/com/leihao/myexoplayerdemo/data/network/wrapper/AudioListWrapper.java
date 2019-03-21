package com.leihao.myexoplayerdemo.data.network.wrapper;

import com.leihao.myexoplayerdemo.data.AudioBean;

import java.util.List;

public class AudioListWrapper {

    private List<AudioBean> data;

    public List<AudioBean> getData() {
        return data;
    }

    public void setData(List<AudioBean> data) {
        this.data = data;
    }
}
