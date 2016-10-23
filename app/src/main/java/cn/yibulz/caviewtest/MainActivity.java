package cn.yibulz.caviewtest;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";


    private CameraView mCameraView;

    private Handler mBackgroundHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCameraView = (CameraView) findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        FloatingActionButton takePicture = (FloatingActionButton) findViewById(R.id.take_picture);
        if (takePicture != null) {
            takePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                }
            });
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandle != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                /*需要注意的是Looper的quit方法从API Level 1就存在了，但是Looper的quitSafely方法从API Level 18才添加进来。
                * 详情请看http://blog.csdn.net/iispring/article/details/47622705*/
                mBackgroundHandle.getLooper().quitSafely();
            } else {
                mBackgroundHandle.getLooper().quit();
            }
            mBackgroundHandle = null;
        }
    }

    private Handler getBackgroundHandle() {
        if (mBackgroundHandle == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandle = new Handler(thread.getLooper());
        }
        return mBackgroundHandle;
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            Log.d(TAG, "onCameraOpened: ");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            Log.d(TAG, "onCameraClosed: ");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            super.onPictureTaken(cameraView, data);
            Log.d(TAG, "onPictureTaken: data.length" + data.length);
            getBackgroundHandle().post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "picture.jpg");
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, "run: Canonot write to " + file, e );
                    } finally {
                        if (os != null) {
                            if (os != null) {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            });
        }

    };
}
