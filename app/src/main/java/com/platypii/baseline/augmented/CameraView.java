package com.platypii.baseline.augmented;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraView";

    private Camera camera;
    private Camera.Parameters cameraParameters;
    private SurfaceHolder holder;

    private int width;
    private int height;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            cameraParameters = camera.getParameters();
            camera.unlock();
        } catch(Exception e) {
            Log.e(TAG, "Failed to open camera", e);
            return;
        }

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
//        final Activity activity = (Activity) getContext();
//        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        int degrees = 0;
//        switch (rotation) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }
//        camera.setDisplayOrientation((info.orientation - degrees + 360) % 360);

        try {
            camera.setPreviewDisplay(this.holder);
//            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "surfaceCreated exception: ", e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        if(camera != null) {
            Camera.Parameters params = camera.getParameters();
            List<Camera.Size> prevSizes = params.getSupportedPreviewSizes();
            for (Camera.Size s : prevSizes) {
                if ((s.height <= height) && (s.width <= width)) {
                    params.setPreviewSize(s.width, s.height);
                    break;
                }
            }
            camera.setParameters(params);
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
