package com.weyye.todaynewsvideodownloader.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.weyye.todaynewsvideodownloader.R;
import com.weyye.todaynewsvideodownloader.model.Video;
import com.weyye.todaynewsvideodownloader.view.WaveView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/13 0013.
 */

public class DownLoadAdapter extends BaseQuickAdapter<Video> {
    private Map<String, BaseViewHolder> mBaseViewHolderMap = new HashMap<>();

    public DownLoadAdapter(List<Video> data) {
        super(R.layout.item_download, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Video video) {
        baseViewHolder.setText(R.id.tvTitle, video.title);
        if (mBaseViewHolderMap.get(video.main_url) == null) {
            mBaseViewHolderMap.put(video.main_url, baseViewHolder);
        }
    }

    private BaseViewHolder getViewHolder(String url) {
        return mBaseViewHolderMap.get(url);
    }

    public void setPercent(String url, int percent) {
        BaseViewHolder viewHolder = getViewHolder(url);
        viewHolder.setText(R.id.tvPercent, percent == 100 ? "下载成功" : (percent == -1 ? "下载失败" : percent + "%"));
        WaveView waveView = viewHolder.getView(R.id.waveView);
        waveView.setPercent(percent);

    }
}
