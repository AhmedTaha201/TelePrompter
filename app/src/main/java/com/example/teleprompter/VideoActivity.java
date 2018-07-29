package com.example.teleprompter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.teleprompter.utils.CameraUtils;
import com.example.teleprompter.utils.FileUtils;

import java.io.IOException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class VideoActivity extends AppCompatActivity {

    public static final int PERMISSION_REQUEST_CODE_CAMERA = 0;
    public static final int PERMISSION_REQUEST_CODE_EXT_STORAGE = 1;
    public static final int PERMISSION_REQUEST_CODE_RECORD_AUDIO = 2;

    //Camera Device
    CameraDevice mCameraDevice;
    //Background thread
    HandlerThread mThread;
    Handler mHandler;
    //Preview
    Size mPreviewSize;
    private CaptureRequest.Builder mCaptureRequestBuilder;

    //Orientation
    int mTotalRotation;

    //Record
    boolean mIsRecording;
    Size mVideoSize;
    //CameraId
    private String mCameraId;
    //File
    String mVideoFilePath;
    @BindView(R.id.record_btn_record_video)
    ImageView mRecordButton;

    private MediaRecorder mMediaRecorder;

    CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            //For pre-Marshmallow devices that causes the activity to pause for giving the permission for the first time
            mCameraDevice = camera;
            if (mIsRecording) {
                try {
                    mVideoFilePath = FileUtils.createVideoFile(getApplicationContext());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startNewVideo();
            } else {
                startPreview();
            }
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

    //Timer
    @BindView(R.id.timer)
    Chronometer mTimer;

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
            configureTransform(width, height);
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
        mMediaRecorder = new MediaRecorder();
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
        super.onWindowFocusChanged(hasFocus);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE_CAMERA && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.i("Camera permission successfully granted");
            openCamera();
        }
        if (requestCode == PERMISSION_REQUEST_CODE_EXT_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.i("External storage permission successfully granted");
            startNewVideo();
        }
        if (requestCode == PERMISSION_REQUEST_CODE_RECORD_AUDIO && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.i("Audio permission successfully granted");
            startNewVideo();

        }
    }


    @OnClick(R.id.record_btn_record_video)
    public void recordButtonTrigger() {
        if (mIsRecording) {
            mTimer.stop();
            mTimer.setVisibility(View.GONE);
            mIsRecording = false;
            mRecordButton.setImageResource(R.drawable.ic_videocam_green);
            startPreview();
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        } else {
            startNewVideo();
        }
    }

    //Helpers

    //Get the Id of the front camera
    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        configureTransform(width, height);
        try {
            if (cameraManager == null) return;
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraId);
                if (cc.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    continue;
                }
                mCameraId = cameraId;

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = CameraUtils.calculateTotalRotation(cc, deviceOrientation);
                StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                int finalWidth = width;
                int finalHeight = height;
                boolean swapDimensions = mTotalRotation == 90 || mTotalRotation == 270;
                if (swapDimensions) {
                    finalHeight = width;
                    finalWidth = height;
                }
                mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), finalWidth, finalHeight);
                mVideoSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), finalWidth, finalHeight);
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
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
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

    //Record new video
    public void startNewVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestAudioAndStoragePermissions();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                mIsRecording = true;
                mRecordButton.setImageResource(R.drawable.ic_videocam_red);
                try {
                    mVideoFilePath = FileUtils.createVideoFile(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecording();
            }
        } else {
            mIsRecording = true;
            mRecordButton.setImageResource(R.drawable.ic_videocam_red);
            try {
                mVideoFilePath = FileUtils.createVideoFile(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecording();
        }


    }

    private void requestAudioAndStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "The storage permission is needed to save video files", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE_EXT_STORAGE);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this, "The Audio permission is needed to record audio", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_CODE_RECORD_AUDIO);
            }

        }
    }

    private void setupMediaRecorder() {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFilePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRecording() {

        setupMediaRecorder();
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        Surface recordSurface = mMediaRecorder.getSurface();
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            Timber.i("Recording session started successfully");
                            try {
                                session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Timber.e("Failed to create the capture session");
                        }
                    }, null);

            //Start recording
            startTimer();
            mMediaRecorder.start();

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void startTimer() {
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.setVisibility(View.VISIBLE);
        mTimer.start();
    }

    //Close the active camera device
    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
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

    /*The following helper method is copied from here
     * https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java*/

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

}
