package com.example.administrator.weixinrecordproject;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnTouchListener, BothWayProgressBar.OnProgressEndListener {
    private SurfaceView sufaceView;
    private TextView tvRemind;
    private BothWayProgressBar progress;
    private ImageView ivDismiss;
    private TextView tvRecord;
    private GestureDetector gestureDetector;
    private int width;
    private int heiht;
    private SurfaceHolder surfaceHolder;
    private MediaRecorder mediaRecorder;
    private Camera camera;
    private int mProgress;
    private boolean isRunning;
    private File mTargetFile;
    private boolean isZoomIn;
    private MyHandler mHandler;
    private Thread mProgressThread;
    private boolean isRecording;
    private int LISTENER_START = 200;
    private int cameraPosition = 0;
    private ImageView btn_switch_camera;
    private boolean isfore=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sufaceView = (SurfaceView) findViewById(R.id.suface_view);
        tvRemind = (TextView) findViewById(R.id.tv_remind);
        progress = (BothWayProgressBar) findViewById(R.id.progress);
        ivDismiss = (ImageView) findViewById(R.id.iv_dismiss);
        tvRecord = (TextView) findViewById(R.id.tv_record);
        btn_switch_camera = (ImageView) findViewById(R.id.btn_switch_camera);

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
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setFixedSize(width, heiht);
        surfaceHolder.addCallback(this);
        //

        tvRecord.setOnTouchListener(this);
        progress.setOnProgressEndListener(this);

        mHandler = new MyHandler(this);
        mediaRecorder = new MediaRecorder();
        //转换摄像头
        btn_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("TAGD", "isfore====" + isfore);
                change();
                startPreview(surfaceHolder);

            }
        });

    }

    // Handler处理
    ///////////////////////////////////////////////////////////////////////////
    private static class MyHandler extends Handler {
        private WeakReference<MainActivity> mReference;
        private MainActivity mActivity;

        public MyHandler(MainActivity activity) {
            mReference = new WeakReference<MainActivity>(activity);
            mActivity = mReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mActivity.progress.setProcess(mActivity.mProgress);
                    break;
            }

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        startPreview(surfaceHolder);


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;

        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;

        }

    }

    //开启预览
    private void startPreview(SurfaceHolder surfaceHolder) {
        if (!isfore) {//后置
            if (camera == null) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } else {
            if (camera == null) {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
        }

        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();

        }
        if (camera != null) {
            camera.setDisplayOrientation(90);
            try {
                camera.setPreviewDisplay(surfaceHolder);
//                Camera.Parameters parameters = camera.getParameters();
//                //实现Camera自动对焦
//                List<String> focusModes = parameters.getSupportedFocusModes();
//                if (focusModes != null) {
//                    for (String mode : focusModes) {
//                        mode.contains("continuous-video");
//                        parameters.setFocusMode("continuous-video");
//                    }
//                }
//                camera.setParameters(parameters);
                camera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    //触摸事件的
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();

        boolean isret = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                progress.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "开始录制", Toast.LENGTH_SHORT).show();
                startRecord();
                mProgressThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            mProgress = 0;
                            isRunning = true;
                            while (isRunning) {
                                mProgress++;
                                mHandler.obtainMessage(0).sendToTarget();
                                Thread.sleep(20);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mProgressThread.start();
                isret = true;

                break;

            case MotionEvent.ACTION_UP:
                progress.setVisibility(View.INVISIBLE);
                Log.e("TAGD", " ACTION_UP mProgress=====" + mProgress);
                Log.e("TAGD", "ACTION_UP isRecording====" + isRecording);
                if (mProgress < 50) {
                    stopRecordUnSave();
                    Toast.makeText(this, "时间太短", Toast.LENGTH_SHORT).show();
                    break;

                }
                stopRecordSave();
                isret = false;
                break;
        }
        return isret;
    }

    //保存
    private void stopRecordSave() {
        Log.e("TAGD", "stopRecordSave()");
        Log.e("TAGD", "isRecording====" + isRecording);
        if (isRecording) {
            isRunning = false;
            mediaRecorder.stop();
            isRecording = false;
            Toast.makeText(this, "视频已经放至" + mTargetFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
    }

    //    不保存
    private void stopRecordUnSave() {
        Log.e("TAGD", "stopRecordUnSave()");
        if (isRecording) {
            isRunning = false;
            mediaRecorder.stop();
            isRecording = false;
            if (mTargetFile.exists()) {
                //直接删掉
                mTargetFile.delete();

            }
        }

    }

    //    开始录制
    private void startRecord() {
        if (mediaRecorder != null) {
            //存储媒体已经挂载，并且挂载点可读/写。
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return;
            }
            try {
                //)解锁，便于MediaRecorder使用摄像头
                camera.unlock();
                mediaRecorder.setCamera(camera);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    mediaRecorder.setAudioSamplingRate(11025);
                }
                //设置音频 来自系统的音频
                mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                //设置音频
                // 设置meidaRecorder的音频源是麦克风
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                //—设置输出格式，指定缺省设置或MediaRecorder.OutputFormat.MPEG_4。
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//                mediaRecorder.setVideoSize(width, heiht);
                //每秒的帧数
//                mediaRecorder.setVideoFrameRate(24);
                // 设置音频的编码格式为amr。这里采用AAC主要为了适配IOS，保证在IOS上可以正常播放。
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //设置视频编码类型，指定缺省设置或者 MediaRecorder.VideoEncoder.MPEG_4_SP。
                mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//            设置帧频率，然后就清晰了
                mediaRecorder.setVideoEncodingBitRate(1 * 1024 * 1024 * 100);


                //用getOutputMediaFile(MEDIA_TYPE_VIDEO).toString()设置输出文件
                File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                mTargetFile = new File(directory, SystemClock.currentThreadTimeMillis() + ".mp4");
                Log.e("TAGD", "mTargetFile.getAbsolutePath()===" + mTargetFile.getAbsolutePath());
                mediaRecorder.setOutputFile(mTargetFile.getAbsolutePath());
                mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//            //解决录制视频, 播放器横向问题
                mediaRecorder.setOrientationHint(90);
                //准备开始录制
                mediaRecorder.prepare();
                mediaRecorder.start();
                isRecording = true;
                Log.e("TAGD", "最后的isRecording=========" + isRecording);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAGD", "e.printStackTrace()====" + e.getMessage().toString());
            }

        }

    }

    @Override
    public void onProgressEndListener() {
        stopRecordSave();
    }

    //双击点击事件
    public class DoubleDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mediaRecorder != null) {
                if (!isZoomIn) {
                    setZoom(20);
                    isZoomIn = true;
                } else {
                    setZoom(0);
                    isZoomIn = false;
                }
            }
            return true;
        }
    }

    private void setZoom(int zoomValue) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.isZoomSupported()) {//判断是否支持
                int maxZoom = parameters.getMaxZoom();
                if (maxZoom == 0) {
                    return;
                }
                if (zoomValue > maxZoom) {
                    zoomValue = maxZoom;
                }
                parameters.setZoom(zoomValue);
                camera.setParameters(parameters);
            }
        }
    }

    public void change() {
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (!isfore) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
//                    cameraPosition = 0;
                    isfore=true;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    camera.stopPreview();//停掉原来摄像头的预览
                    camera.release();//释放资源
                    camera = null;//取消原来摄像头
                    camera = Camera.open(i);//打开当前选中的摄像头
                    try {
                        camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    camera.startPreview();//开始预览
//                    cameraPosition = 1;
                    isfore=false;
                    break;
                }
            }

        }
    }
}
