package com.weyye.todaynewsvideodownloader.core;

import android.util.Base64;
import android.util.Log;

import com.weyye.todaynewsvideodownloader.base.ApiService;
import com.weyye.todaynewsvideodownloader.base.AppClient;
import com.weyye.todaynewsvideodownloader.base.ResultResponse;
import com.weyye.todaynewsvideodownloader.model.Video;
import com.weyye.todaynewsvideodownloader.model.VideoModel;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Administrator on 2017/2/9 0009.
 */

public abstract class VideoPathDecoder {
    static String TAG = VideoPathDecoder.class.getSimpleName();
    private CompositeSubscription mCompositeSubscription;

    public void addSubscription(Observable observable, Subscriber subscriber) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber));
    }

    public void decodePath(final String srcUrl) {

        AppClient.getApiService().getVideoHtml(srcUrl)
                .flatMap(new Func1<String, Observable<ResultResponse<VideoModel>>>() {
                    @Override
                    public Observable<ResultResponse<VideoModel>> call(String response) {
                        Pattern pattern = Pattern.compile("videoid:\'(.+)\'");
                        Matcher matcher = pattern.matcher(response);
                        if (matcher.find()) {
                            String videoId = matcher.group(1);
                            Log.i(TAG,videoId);
                            //1.将/video/urls/v/1/toutiao/mp4/{videoid}?r={Math.random()}，进行crc32加密。
                            String r = getRandom();
                            CRC32 crc32 = new CRC32();
                            String s = String.format(ApiService.URL_VIDEO, videoId, r);
                            //进行crc32加密。
                            crc32.update(s.getBytes());
                            String crcString = crc32.getValue() + "";
                            //2.访问http://i.snssdk.com/video/urls/v/1/toutiao/mp4/{videoid}?r={Math.random()}&s={crc32值}
                            String url = ApiService.HOST_VIDEO + s + "&s=" + crcString;
                            Log.i(TAG,url);
                            return AppClient.getApiService().getVideoData(url);
                        }
                        return null;
                    }
                })
                .map(new Func1<ResultResponse<VideoModel>, Video>() {
                    @Override
                    public Video call(ResultResponse<VideoModel> videoModelResultResponse) {
                        VideoModel.VideoListBean data = videoModelResultResponse.data.video_list;

                        if (data.video_3 != null) {
                            return updateVideo(data.video_3);
                        }
                        if (data.video_2 != null) {
                            return updateVideo(data.video_2);
                        }
                        if (data.video_1 != null) {
                            return updateVideo(data.video_1);
                        }
                        return null;
                    }

                    private String getRealPath(String base64) {
                        return new String(Base64.decode(base64.getBytes(), Base64.DEFAULT));
                    }

                    private Video updateVideo(Video video) {
                        //base64解码
                        video.main_url = getRealPath(video.main_url);
                        return video;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Video>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        onDecodeError(e);
                    }

                    @Override
                    public void onNext(Video s) {
                        onSuccess(s);
                    }
                });
    }

    public abstract void onSuccess(Video s);

    public abstract void onDecodeError(Throwable e);

    private String getRandom() {
        Random random = new Random();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
}
