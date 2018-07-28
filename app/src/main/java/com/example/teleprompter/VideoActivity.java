package com.example.teleprompter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class VideoActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE_CAMERA = 0;

    //Camera Device
    CameraDevice mCameraDevice;
    //Background thread
    HandlerThread mThread;
    Handler mHandler;
    //Preview
    private CaptureRequest.Builder mCaptureRequestBuilder;
    CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };
    //CameraId
    private String mCameraId;

    //TextureView
    @BindView(R.id.texture_view)
    TextureView mTextureView;
    TextureView.SurfaceTextureListener mTextureViewListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mTextureViewListener);
        }
    }

    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    //Helpers

    //Get the Id of the front camera
    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (cameraManager == null) return;
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //Open the camera
    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraStateCallback, mHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, R.string.record_permission_needed_toast, Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CODE_CAMERA);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraStateCallback, mHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //Start the preview
    private void startPreview() {
        //The surface from the textureView
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(1080, 720);
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Timber.e("Failed to start the preview");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    //Close the active camera device
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    //Start the background thread for the camera
    private void startBackgroundThread() {
        mThread = new HandlerThread(getString(R.string.app_name));
        mThread.start();
        Timber.d("Background thread started successfully");
        mHandler = new Handler(mThread.getLooper());
    }

    private void stopBackgroundThread() {
        mThread.quitSafely();
        try {
            mThread.join();
            mThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            Timber.e("Failed to stop the background thread");
        }
    }
}
