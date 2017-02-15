package com.weyye.todaynewsvideodownloader.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.weyye.todaynewsvideodownloader.R;
import com.weyye.todaynewsvideodownloader.adapter.DownLoadAdapter;
import com.weyye.todaynewsvideodownloader.core.FileDownload;
import com.weyye.todaynewsvideodownloader.core.VideoPathDecoder;
import com.weyye.todaynewsvideodownloader.model.Video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    //    private EditText et;
    private List<Video> mDatas = new ArrayList<>();
    private DownLoadAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();
        if (checkPermission()) {
            //分享内容
            String title = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(title))
                parseUrl(title);
        }
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    100);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("tss", "onRequestPermissionsResult");
        } else {
            //权限拒绝
            Snackbar.make(mRecyclerView, "权限申请失败，即将关闭app", Snackbar.LENGTH_LONG).show();
//            Toast.makeText(this, "权限申请失败，即将关闭app", Toast.LENGTH_SHORT).show();
            mRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1500);
        }
    }


    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//        et = (EditText) findViewById(et);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new DownLoadAdapter(mDatas);
        mRecyclerView.setAdapter(mAdapter);
        View emptyView = View.inflate(this, R.layout.layout_empty, null);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAdapter.setEmptyView(emptyView);

        mAdapter.setOnRecyclerViewItemClickListener(new BaseQuickAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int i) {
                Video video = mDatas.get(i);
                if (video.file != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(video.file.getAbsolutePath()), "video/" + video.vtype);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("MainActivity", "onNewIntent");
        String title = intent.getStringExtra(Intent.EXTRA_TEXT);
        parseUrl(title);
    }

    private void parseUrl(String title) {
        //取出网页地址
        Pattern pattern = Pattern.compile("【(.+)】\\n(http.+)");

        final Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("正在获取视频地址，请稍后~");
            dialog.setCanceledOnTouchOutside(false);
            //解析地址
            VideoPathDecoder decoder = new VideoPathDecoder() {
                @Override
                public void onSuccess(Video s) {
                    dialog.dismiss();
                    s.title = matcher.group(1);
                    mDatas.add(s);
                    mAdapter.notifyItemInserted(mDatas.size());
                    startDownload(s);
                }

                @Override
                public void onDecodeError(Throwable e) {
                    dialog.dismiss();
                    Snackbar.make(mRecyclerView, "获取视频失败！", Snackbar.LENGTH_LONG).show();
                }
            };
            dialog.show();
            decoder.decodePath(matcher.group(2));
        } else {
            Snackbar.make(mRecyclerView, "不是分享的链接", Snackbar.LENGTH_LONG).show();
        }
    }

    private void startDownload(final Video video) {

        FileDownload download = new FileDownload(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "todayNewsVideo"
                , UUID.randomUUID().toString() + "." + video.vtype);
        download.download(video.main_url, new FileDownload.Callback() {
            @Override
            public void onError(Exception e) {
                mAdapter.setPercent(video.main_url, -1);
            }

            @Override
            public void onSuccess(File file) {
                video.file = file;
                mAdapter.setPercent(video.main_url, 100);
            }

            @Override
            public void inProgress(float progress, long total) {
                mAdapter.setPercent(video.main_url, (int) (progress * 100));
            }
        });


    }
}
