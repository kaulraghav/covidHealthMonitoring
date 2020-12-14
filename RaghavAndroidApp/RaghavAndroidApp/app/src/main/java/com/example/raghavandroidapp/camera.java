package com.example.raghavandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.Manifest;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

public class camera extends AppCompatActivity {
    //Variables
    private static final String TAG = "camera";
    camera activity;
    Intent intent;
    private TextureView textureView;
    private Size dim;
    public static float finalBPM;
    private int curWinAvg;
    private int prevWinAvg;
    private int firstWinAvg;
    private long [] hRateArray;
    public int min = 60000;
    private int entries = 0;
    private int beatCnt = 0;
    TextView bottomTextView;
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Handler handler;
    private HandlerThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Toast.makeText(getApplicationContext() , "Coming in Camera's OnCreate!", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        activity = this;
        intent = new Intent();
        textureView =  findViewById(R.id.textureView);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        hRateArray = new long [45];
        bottomTextView = (TextView)findViewById(R.id.textView7);
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            launchCamera();
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
            //Log.d(TAG, "In OnSurfaceTextureUpdated");
            Bitmap bmp = textureView.getBitmap();
            int width = bmp.getWidth();
            int height = bmp.getHeight();
            int[] pixels = new int[height * width];
            bmp.getPixels(pixels, 0, width, width / 2, height / 2, width / 20, height / 20);
            int total = 0;
            for (int i = 0; i < height * width; i++) {
                int red = (pixels[i] >> 16) & 0xFF;
                total = total + red;
            }
            if (entries == 20) {
                curWinAvg = total;
            }
            else if (entries > 20 && entries < 49) {
                curWinAvg = (curWinAvg * (entries - 20) + total)/(entries - 19);
            }
            else if (entries >= 49) {
                curWinAvg = (curWinAvg * 29 + total) / 30;
                if (prevWinAvg > curWinAvg && prevWinAvg > firstWinAvg && beatCnt < 45) {
                    hRateArray[beatCnt] = System.currentTimeMillis();
                    beatCnt++;
                    if (beatCnt == 45) {
                        calcHeartRate();
                    }
                }
            }
            entries++;
            firstWinAvg = prevWinAvg;
            prevWinAvg = curWinAvg;
        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            cameraInterface();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (cameraDevice != null)
                cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startThread() {
        thread = new HandlerThread("Camera");
        thread.start();
        handler = new Handler(thread.getLooper());
    }
    protected void stopThread() {
        thread.quitSafely();
        try {
            thread.join();
            thread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void calcHeartRate() {
        float middle;
        long[] diff = new long[14];
        for (int i = 0; i < 14; i++) {
            diff[i] = hRateArray[i + 1] - hRateArray[i];
        }
        Arrays.sort(diff);
        middle = (float) diff[diff.length / 2];
        finalBPM = min / middle;
        intent.putExtra("heartRate", finalBPM + "");
        activity.setResult(321, intent);
        Toast.makeText(camera.this, "Heart Rate recorded. Please navigate back to the main page.", Toast.LENGTH_LONG).show();
    }
    private void launchCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            dim = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(camera.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void cameraInterface() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(dim.getWidth(), dim.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    setCameraProperties();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void setCameraProperties() {
        if (null == cameraDevice) {
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startThread();
        if (textureView.isAvailable()) {
            launchCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        closeCamera();
        stopThread();
        super.onPause();
    }
}