package com.example.administrator.weixinrecordproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView sufaceView;
    private TextView tvRemind;
    private BothWayProgressBar progress;
    private ImageView ivDismiss;
    private TextView tvRecord;
    private GestureDetector gestureDetector;
    private int width;
    private int heiht;
    private SurfaceHolder surfaceHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sufaceView = (SurfaceView) findViewById(R.id.suface_view);
        tvRemind = (TextView) findViewById(R.id.tv_remind);
        progress = (BothWayProgressBar) findViewById(R.id.progress);
        ivDismiss = (ImageView) findViewById(R.id.iv_dismiss);
        tvRecord = (TextView) findViewById(R.id.tv_record);

        setData();
    }

    private void setData() {
        width = 640;
        heiht = 400;

        gestureDetector = new GestureDetector(this, new DoubleDetector());
        //单独处理surfaceview的点击事件
        sufaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        //urface的抽象接口，使你可以控制surface的大小和格式， 以及在surface上编辑像素，和监视surace的改变
        surfaceHolder = sufaceView.getHolder();
        surfaceHolder.setFixedSize(width, heiht);
        surfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    //双击点击事件
    public class DoubleDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }
    }

}
