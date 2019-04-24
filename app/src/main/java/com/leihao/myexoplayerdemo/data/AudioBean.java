package com.leihao.myexoplayerdemo.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioBean implements Parcelable {

    private int speechColumnId;
    public String speechImage;// 图片
    public String speechName;// 名称
    public String speechDesc;// 歌手
    public String speechUrl;// 音频URL
    private String duration;// 时长

    private AudioBean(Parcel in) {
        speechColumnId = in.readInt();
        speechImage = in.readString();
        speechName = in.readString();
        speechDesc = in.readString();
        speechUrl = in.readString();
        duration = in.readString();
    }

    public static final Creator<AudioBean> CREATOR = new Creator<AudioBean>() {
        @Override
        public AudioBean createFromParcel(Parcel in) {
            return new AudioBean(in);
        }

        @Override
        public AudioBean[] newArray(int size) {
            return new AudioBean[size];
        }
    };

    public int getSpeechColumnId() {
        return speechColumnId;
    }

    public void setSpeechColumnId(int speechColumnId) {
        this.speechColumnId = speechColumnId;
    }

    public String getSpeechImage() {
        return speechImage;
    }

    public void setSpeechImage(String speechImage) {
        this.speechImage = speechImage;
    }

    public String getSpeechName() {
        return speechName;
    }

    public void setSpeechName(String speechName) {
        this.speechName = speechName;
    }

    public String getSpeechDesc() {
        return speechDesc;
    }

    public void setSpeechDesc(String speechDesc) {
        this.speechDesc = speechDesc;
    }

    public String getSpeechUrl() {
        return speechUrl;
    }

    public void setSpeechUrl(String speechUrl) {
        this.speechUrl = speechUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(speechColumnId);
        dest.writeString(speechImage);
        dest.writeString(speechName);
        dest.writeString(speechDesc);
        dest.writeString(speechUrl);
        dest.writeString(duration);
    }
}
