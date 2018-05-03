package com.mrinaanksinha.majorworkandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.*;


public class MainActivity extends AppCompatActivity {

    public Mat img;
    Bitmap bitmap;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    img = new Mat();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for Initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.example3, options);

        Utils.bitmapToMat(bitmap, img);

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 55, 10);

        Mat croppedImg = RotateAndCrop(img);

        Bitmap bmap = Bitmap.createBitmap(croppedImg.cols(), croppedImg.rows(), Bitmap.Config.ARGB_8888);

//        }

        Utils.matToBitmap(croppedImg, bmap);
        Log.d("TAG", "GD");

    }

    private Mat RotateAndCrop(@NonNull Mat src) {
        RotatedRect rect = null;
        Mat points = Mat.zeros(src.size(), src.channels());
        Core.findNonZero(src, points);

        MatOfPoint mpoints = new MatOfPoint(points);
        MatOfPoint2f points2f = new MatOfPoint2f(mpoints.toArray());

//        if (points2f.rows() > 0) {
        rect = Imgproc.minAreaRect(points2f);
        double angle = rect.angle;
        Size croppedSize;
        if (rect.angle < -45.0) {
            angle += 90;
            croppedSize = new Size(rect.size.height, rect.size.width);
        } else {
            croppedSize = rect.size;
        }

        Mat rotMat = Imgproc.getRotationMatrix2D(rect.center, angle, 1);
        Mat rotated = new Mat();
        Imgproc.warpAffine(src, rotated, rotMat, new Size(src.width(), src.height()), Imgproc.INTER_CUBIC);
        Mat cropped = new Mat();
        Imgproc.getRectSubPix(rotated, croppedSize, rect.center, cropped);
        return cropped;
    }

}
