package com.example.administrator.weixinrecordproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2017/12/5.
 */

public class BothWayProgressBar extends View {
    private Context mContext;
    //正在录制的画笔
    private Paint mRecordPaint;
    //是否显示
    private int mVisibility;
    // 当前进度
    private int progress;

    //进度条结束的监听
    private OnProgressEndListener mOnProgressEndListener;

    public BothWayProgressBar(Context context) {
        super(context, null);
    }

    public BothWayProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initdata();
    }

    private void initdata() {
        mVisibility = INVISIBLE;
        mRecordPaint = new Paint();
        mRecordPaint.setColor(Color.GREEN);
    }

    //
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mVisibility == VISIBLE) {
            int height = getHeight();
            int width = getWidth();
            int middle = width / 2;
            if (progress < middle) {
                //进度条
                canvas.drawRect(progress, 0, width - progress, height, mRecordPaint);

            } else {
                //结束
                if (mOnProgressEndListener != null) {
                    mOnProgressEndListener.onProgressEndListener();

                }
            }

        } else {
            canvas.drawColor(Color.argb(0, 0, 0, 0));
        }
    }

    //设置进度
    public void setProcess(int prosess) {
        this.progress = prosess;
        //重绘
        invalidate();
    }

    @Override
    public void setVisibility(int mVisibility) {
        this.mVisibility = mVisibility;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public interface OnProgressEndListener {
        void onProgressEndListener();
    }
}
