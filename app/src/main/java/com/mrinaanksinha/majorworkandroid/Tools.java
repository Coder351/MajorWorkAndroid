package com.mrinaanksinha.majorworkandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Tools
{
    //TODO: Check if these sizes are correct
    private static int MIN_PREVIEW_PIXELS = 470 * 320;
    private static int MAX_PREVIEW_PIXELS = 800 * 600;
    private static final int MIN_FOCUS_BOX_WIDTH = 50;
    private static final int MIN_FOCUS_BOX_HEIGHT = 20;

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap preRotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.preRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
    }

    public static enum ScalingLogic
    {
        CROP, FIT
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                          ScalingLogic scalingLogic)
    {
        if (scalingLogic == ScalingLogic.FIT)
        {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect)
            {
                return srcWidth / dstWidth;
            }
            else
            {
                return srcHeight / dstHeight;
            }
        }
        else
        {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect)
            {
                return srcHeight / dstHeight;
            }
            else
            {
                return srcWidth / dstWidth;
            }
        }
    }

    public static Bitmap decodeByteArray(byte[] bytes, int dstWidth, int dstHeight,
                                         ScalingLogic scalingLogic)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
                dstHeight, scalingLogic);
        Bitmap unscaledBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        return unscaledBitmap;
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic)
    {
        if (scalingLogic == ScalingLogic.CROP)
        {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect)
            {
                final int srcRectWidth = (int) (srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            }
            else
            {
                final int srcRectHeight = (int) (srcWidth / dstAspect);
                final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        }
        else
        {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic)
    {
        if (scalingLogic == ScalingLogic.FIT)
        {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect)
            {
                return new Rect(0, 0, dstWidth, (int) (dstWidth / srcAspect));
            }
            else
            {
                return new Rect(0, 0, (int) (dstHeight * srcAspect), dstHeight);
            }
        }
        else
        {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }

    public static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
                                            ScalingLogic scalingLogic)
    {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(),
                dstWidth, dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static Bitmap getFocusedBitmap(Context context, Camera camera, byte[] data, Rect box)
    {
        Point CamRes = getCameraResolution(context, camera);
        Point ScrRes = getScreenResolution(context);

        int SW = ScrRes.x;
        int SH = ScrRes.y;

        int RW = box.width();
        int RH = box.height();
        int RL = box.left;
        int RT = box.top;

        float RSW = (float) (RW * Math.pow(SW, -1));
        float RSH = (float) (RH * Math.pow(SH, -1));

        float RSL = (float) (RL * Math.pow(SW, -1));
        float RST = (float) (RT * Math.pow(SH, -1));

        float k = 0.5f;

        int CW = CamRes.x;
        int CH = CamRes.y;

        int X = (int) (k * CW);
        int Y = (int) (k * CH);

        Bitmap unscaledBitmap = Tools.decodeByteArray(data, X, Y, Tools.ScalingLogic.CROP);
        Bitmap bmp = Tools.createScaledBitmap(unscaledBitmap, X, Y, Tools.ScalingLogic.CROP);
        unscaledBitmap.recycle();

        if (CW > CH)
        {
            bmp = Tools.rotateBitmap(bmp, 90);
        }

        int BW = bmp.getWidth();
        int BH = bmp.getHeight();

        int RBL = (int) (RSL * BW);
        int RBT = (int) (RST * BH);

        int RBW = (int) (RSW * BW);
        int RBH = (int) (RSH * BH);

        Bitmap res = Bitmap.createBitmap(bmp, RBL, RBT, RBW, RBH);
        bmp.recycle();

        return res;
    }

    public static Point getScreenResolution(Context context)
    {

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        Point displaySize = new Point();
        display.getSize(displaySize);

        int width = displaySize.x;
        int height = displaySize.y;

        return new Point(width, height);

    }

    public static Point getCameraResolution(Context context, Camera camera)
    {
        Camera.Parameters parameters = camera.getParameters();
        Point screenResolution = getScreenResolution(context);


        List<Camera.Size> supportedPreviewSizes =
                new ArrayList<Camera.Size>(parameters.getSupportedPreviewSizes());

        Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>()
        {
            @Override
            public int compare(Camera.Size a, Camera.Size b)
            {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels)
                {
                    return -1;
                }
                if (bPixels > aPixels)
                {
                    return 1;
                }
                return 0;
            }
        });

        Point bestSize = null;
        float screenAspectRatio = (float) screenResolution.x / (float) screenResolution.y;

        float diff = Float.POSITIVE_INFINITY;
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes)
        {
            int realWidth = supportedPreviewSize.width;
            int realHeight = supportedPreviewSize.height;
            int pixels = realWidth * realHeight;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS)
            {
                continue;
            }
            boolean isCandidatePortrait = realWidth < realHeight;
            int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
            int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
            if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y)
            {
                return new Point(realWidth, realHeight);
            }
            float aspectRatio = (float) maybeFlippedWidth / (float) maybeFlippedHeight;
            float newDiff = Math.abs(aspectRatio - screenAspectRatio);
            if (newDiff < diff)
            {
                bestSize = new Point(realWidth, realHeight);
                diff = newDiff;
            }
        }

        if (bestSize == null)
        {
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
        }
        return bestSize;
    }

    public static Rect getBox(Context context, View view)
    {
        Point screenRes = getScreenResolution(context);

        int width = screenRes.x * 6 / 7;
        int height = screenRes.y / 9;
//TODO: Add later if required. dont forget to uncomment variables above
//        width = width == 0
//                ? MIN_FOCUS_BOX_WIDTH
//                : width < MIN_FOCUS_BOX_WIDTH ? MIN_FOCUS_BOX_WIDTH : width;
//
//        height = height == 0
//                ? MIN_FOCUS_BOX_HEIGHT
//                : height < MIN_FOCUS_BOX_HEIGHT ? MIN_FOCUS_BOX_HEIGHT : height;

        int left = (screenRes.x - width) / 2;
        int top = (screenRes.y - height) / 2;

        return new Rect(left, top, left + width, top + height);


    }

}
