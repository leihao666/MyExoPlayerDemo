package com.leihao.myexoplayerdemo.listing;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.leihao.myexoplayerdemo.R;
import com.leihao.myexoplayerdemo.data.AudioBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AudioListingAdapter extends RecyclerView.Adapter<AudioListingAdapter.ViewHolder> {

    private List<AudioBean> audioBeans;
    private AudioItemListener itemListener;

    AudioListingAdapter(List<AudioBean> audioBeans, AudioItemListener itemListener) {
        this.audioBeans = audioBeans;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int itemType) {
        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_list, parent, false);
        return new ViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioBean bean = audioBeans.get(position);
        holder.tv_name.setText(bean.speechName);
        holder.tv_artist.setText(bean.speechDesc);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.LOW);

        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(bean.speechImage)
                .apply(options)
                .into(new BitmapImageViewTarget(holder.iv_pic) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(bitmap, transition);
                        Palette.from(bitmap).generate(palette -> setBackgroundColor(palette, holder));
                    }
                });

        holder.itemView.setOnClickListener(holder);
        holder.audioBean = bean;
    }

    private void setBackgroundColor(Palette palette, ViewHolder holder) {
        holder.itemView.setBackgroundColor(palette.getVibrantColor(holder.itemView.getResources().getColor(R.color.black_translucent_60)));
    }

    @Override
    public int getItemCount() {
        return audioBeans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_pic)
        ImageView iv_pic;
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_artist)
        TextView tv_artist;

        AudioBean audioBean;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            AudioListingAdapter.this.itemListener.onAudioClick(audioBean);
        }
    }

    public interface AudioItemListener {

        void onAudioClick(AudioBean audioBean);
    }
}
