package com.weyye.todaynewsvideodownloader.core;

import com.weyye.todaynewsvideodownloader.base.AppClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by RayYeung on 2017/1/4.
 */

public class FileDownload {

    /**
     * 目标文件存储的文件夹路径
     */
    private String destFileDir;
    /**
     * 目标文件存储的文件名
     */
    private String destFileName;

    public FileDownload(String destFileDir, String destFileName) {
        this.destFileDir = destFileDir;
        this.destFileName = destFileName;
    }

    public void download(String url, final Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final File file = saveFile(response, callback);
                    AppClient.getDelivery().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(file);
                        }
                    });
                } catch (IOException e) {
                    callback.onError(e);
                }
            }
        });
    }

    public File saveFile(Response response, final Callback callback) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            long sum = 0;

            File dir = new File(destFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, destFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                AppClient.getDelivery().post(new Runnable() {
                    @Override
                    public void run() {
                        callback.inProgress(finalSum * 1.0f / total, total);
                    }
                });
            }
            fos.flush();

            return file;

        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
            }

        }
    }

    public interface Callback {
        void onError(Exception e);

        void onSuccess(File file);

        void inProgress(float progress, long total);
    }
}
