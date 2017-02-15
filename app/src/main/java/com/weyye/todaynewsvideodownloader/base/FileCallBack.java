package com.weyye.todaynewsvideodownloader.base;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.ResponseBody;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/2/15 0015.
 */

public class FileCallBack implements Func1<ResponseBody, File> {
    String mFileName;
    public FileCallBack(String fileName) {
        mFileName =fileName;
    }

    @Override
    public File call(ResponseBody responseBody) {
        File file = null;
        try {
            file = saveFile(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void inProgress(float progress, long total) {

    }

    private File saveFile(ResponseBody response) throws IOException {
        File downloadFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "todayNewsVideo");
        if(!downloadFile.exists())
            downloadFile.mkdirs();
        String destFileDir =downloadFile.getAbsolutePath() ;
        String destFileName = mFileName;
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = response.byteStream();
            final long total = response.contentLength();
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
                inProgress(sum * 1.0f / total, total);
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
}
