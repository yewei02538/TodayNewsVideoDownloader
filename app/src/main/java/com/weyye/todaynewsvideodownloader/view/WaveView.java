package com.weyye.todaynewsvideodownloader.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.weyye.todaynewsvideodownloader.R;

/**
 * Created by Administrator on 2017/2/14 0014.
 */

public class WaveView extends View {
    String TAG = this.getClass().getSimpleName();
    public int mDefaultAboveColor = Color.BLUE;
    public int mDefaultBelowColor = Color.parseColor("#aaFF0000");
    private Path mAbovePath;
    private int mAboveColor;
    private int mBelowColor;
    private Path mBelowPath;
    private Paint mAboveWavePaint;
    private Paint mBelowWavePaint;
    private PaintFlagsDrawFilter mDrawFilter;
    private int mPercent = 50;
    private float φ;
    private float A = 10;
    private Paint mTextPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.WaveView);
        mAboveColor = ta.getColor(R.styleable.WaveView_wvAboveColor, mDefaultAboveColor);
        mBelowColor = ta.getColor(R.styleable.WaveView_wvBelowColor, mDefaultBelowColor);
        init();
    }

    private void init() {
        //初始化路径
        mAbovePath = new Path();
        mBelowPath = new Path();

        //初始化画笔
        mAboveWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAboveWavePaint.setAntiAlias(true);
        mAboveWavePaint.setStyle(Paint.Style.FILL);
        mAboveWavePaint.setColor(mAboveColor);

        mBelowWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBelowWavePaint.setAntiAlias(true);
        mBelowWavePaint.setStyle(Paint.Style.FILL);
        mBelowWavePaint.setColor(mBelowColor);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(Color.BLACK);



    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(Color.TRANSPARENT);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas.setDrawFilter(mDrawFilter);

        mAbovePath.reset();
        mBelowPath.reset();

        mAbovePath.moveTo(getLeft(), getBottom());
        mBelowPath.moveTo(getLeft(), getBottom());

        φ -= 0.1f;
        float startY = ((1 - mPercent / 100f) * getHeight());
        double ω = 2 * Math.PI / getWidth();
        float y, y2;

        for (float x = 0; x <= getWidth(); x += 20) {
            /**
             *  y=Asin(ωx+φ)+k
             *  A—振幅越大，波形在y轴上最大与最小值的差值越大
             *  ω—角速度， 控制正弦周期(单位角度内震动的次数)
             *  φ—初相，反映在坐标系上则为图像的左右移动。这里通过不断改变φ,达到波浪移动效果
             *  k—偏距，反映在坐标系上则为图像的上移或下移。
             */
//            y = (float) (8 * Math.cos(ω * x + φ) +8)+startY;
//            y2 = (float) (8 * Math.sin(ω * x + φ))+startY;
            y = (float) (A * Math.cos(ω * x + φ) + A) + startY;
            y2 = (float) (A * Math.sin(ω * x + φ)) + startY;
            mAbovePath.lineTo(x, y);
            mBelowPath.lineTo(x, y2);
            clearCanvas(canvas);
//            //回调 把y坐标的值传出去(在activity里面接收让小机器人随波浪一起摇摆)
//            mWaveAnimationListener.OnWaveAnimation(y);
        }

        mAbovePath.lineTo(getRight(), getBottom());
        mBelowPath.lineTo(getRight(), getBottom());
        mCanvas.drawPath(mAbovePath, mAboveWavePaint);
        mCanvas.drawPath(mBelowPath, mBelowWavePaint);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        postInvalidateDelayed(20);

    }

    private void clearCanvas(Canvas canvas) {
//        Paint paint = new Paint();
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        canvas.drawPaint(paint);
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setAntiAlias(true);
        mCanvas.drawPaint(transparentPaint);
        invalidate();
    }

    private void drawText(Canvas canvas, float y) {
//        Log.i(TAG, "y:"+y);
        String str = mPercent + "";
        mTextPaint.setTextSize(sp2px(60));
        float txtLength = mTextPaint.measureText(str);
        mCanvas.drawText(str, getWidth() / 2 - txtLength / 2, y , mTextPaint);
        mTextPaint.setTextSize(sp2px(30));
        mCanvas.drawText("%", getWidth() / 2 + 50, y, mTextPaint);
    }

    public void setPercent(int percent) {
        mPercent = percent;
    }

    public  int sp2px( float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
