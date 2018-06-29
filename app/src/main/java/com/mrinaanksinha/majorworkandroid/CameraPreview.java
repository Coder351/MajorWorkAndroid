package com.mrinaanksinha.majorworkandroid;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private SurfaceHolder holder;
    private Camera camera;


    public Handler focusHandler = new Handler();

    private Runnable focusCameraRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            focusCamera();
        }
    };

    public CameraPreview(Context context, Camera camera, Activity parent)
    {
        super(context);
        this.camera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        holder = getHolder();
        holder.addCallback(this);

        setCameraDisplayOrientation(parent, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size bestPreviewSize = determineBestPreviewSize(parameters);
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        Camera.Size bestPictureSize = determineBestPictureSize(parameters);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);
        camera.setParameters(parameters);
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        // The Surface has been created, now tell the camera where to draw the preview.
        try
        {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            focusHandler.post(focusCameraRunnable);
        }
        catch (IOException e)
        {
            Log.d("TAG", "Error setting camera preview: " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (this.holder.getSurface() == null)
        {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try
        {
            camera.stopPreview();
        }
        catch (Exception e)
        {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        Camera.Size size = camera.getParameters().getPreviewSize();
        Camera.Size size1 = camera.getParameters().getPictureSize();
        Log.d("GETE", size.toString());
        // start preview with new settings
        try
        {
            camera.setPreviewDisplay(this.holder);
            camera.startPreview();

        }
        catch (Exception e)
        {
            Log.d("TAG", "Error starting camera preview: " + e.getMessage());
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera)
    {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        camera.getParameters().setPreviewSize(1440, 1080);
        switch (rotation)
        {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        // do something for phones running an SDK before lollipop
//        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//            result = (info.orientation + degrees) % 360;
//            result = (360 - result) % 360; // compensate the mirror
//        } else { // back-facing
        result = (info.orientation - degrees + 360) % 360;
//        }

        camera.setDisplayOrientation(result);
    }

    public static Camera.Size determineBestPreviewSize(Camera.Parameters parameters)
    {
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        return determineBestSize(sizes);
    }

    public static Camera.Size determineBestPictureSize(Camera.Parameters parameters)
    {
        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        return determineBestSize(sizes);

    }

    protected static Camera.Size determineBestSize(List<Camera.Size> sizes)
    {
        Camera.Size bestSize = null;
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long availableMemory = Runtime.getRuntime().maxMemory() - used;
        for (Camera.Size currentSize : sizes)
        {
            int newArea = currentSize.width * currentSize.height;
            long neededMemory = newArea * 4; // newArea * 4 Bytes/pixel * 4 needed copies of the bitmap (4copies removed)(for safety :) )
            boolean isDesiredRatio = (currentSize.width / 16) == (currentSize.height / 9);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isSafe = neededMemory < availableMemory;
            if (isDesiredRatio && isBetterSize && isSafe)
            {
                bestSize = currentSize;
            }
        }
        if (bestSize == null)
        {
            return sizes.get(0);
        }
        return bestSize;
    }

    public void initFocus(Rect box)
    {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        List<Camera.Area> areas = new ArrayList<>();
//        areas.add(new Camera.Area(box, 1000));
//        parameters.setFocusAreas(areas);
        camera.setParameters(parameters);
    }

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success, Camera camera)
        {
                camera.cancelAutoFocus();
        }

    };

    public void focusCamera()
    {
        camera.autoFocus(autoFocusCallback);
    }

    public void removeFocusCallback() //Encapsulation FTW
    {
        focusHandler.removeCallbacks(focusCameraRunnable);
    }
}
