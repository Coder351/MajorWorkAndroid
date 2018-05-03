package com.mrinaanksinha.majorworkandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.lang.UCharacter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    public Mat img;
    Bitmap bitmap;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:{
                    img = new Mat();

                }
                break;
                default:{
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

        if(!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for Initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }else{
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.example1,options);

        Utils.bitmapToMat(bitmap,img);

        Imgproc.cvtColor(img,img,Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img,img,new Size(3,3),0);
        Imgproc.adaptiveThreshold(img,img,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY_INV ,55,10);

//        ArrayList<Point> points = new ArrayList<Point>();
//        //TOO SLOWWWW!!!!!!!
//        for(int row = 0; row < img.rows(); row++)
//        {
//            for(int col = 0; col< img.cols();col++)
//            {
//
//                if(img.get(row,col)[0]==255)
//                {
//                    points.add(new Point(col,row));
//
//                }
//            }
//        }
//
//        MatOfPoint2f pointsMat  = new MatOfPoint2f();
//        pointsMat.fromList(points);
//        RotatedRect rect = Imgproc.minAreaRect(pointsMat);
//
//        double angle = -rect.angle-90;
//        Point center = new Point(rect.center.y,rect.center.x);
//        if (angle<-45.0)
//        {
//            angle+=90;
//        }
//
//        Mat rotationMatrix2D = Imgproc.getRotationMatrix2D(center,angle,1);
//        Mat rotated = new Mat();
//        Imgproc.warpAffine(img,rotated,rotationMatrix2D,new Size(img.height(),img.width()),Imgproc.INTER_CUBIC);
//        Size rectSize;
//        if(angle<-45.0)
//        {
//
//            rectSize = new Size(rect.size.height,rect.size.width);
//        }
//        else
//        {
//            rectSize = new Size(rect.size.width,rect.size.height);
//        }
//
//        Mat cropped = new Mat();
//        Bitmap.Config config = Bitmap.Config.ARGB_8888;
////        Bitmap bmap = Bitmap.createBitmap((int) rectSize.width,(int) rectSize.height,config);
//        Bitmap bmap = Bitmap.createBitmap((int)rectSize.height,(int)rectSize.width,config);
//        Imgproc.getRectSubPix(rotated,new Size(rectSize.height,rectSize.width),center,cropped);
        RotatedRect rect = null;
        Mat points = Mat.zeros(img.size(),img.channels());
        Core.findNonZero(img, points);

        MatOfPoint mpoints = new MatOfPoint(points);
        MatOfPoint2f points2f = new MatOfPoint2f(mpoints.toArray());

//        if (points2f.rows() > 0) {
        rect = Imgproc.minAreaRect(points2f);
        double angle = rect.angle;
        Size croppedSize;
        if(rect.angle<-45.0)
        {
            angle+=90;
            croppedSize = new Size(rect.size.height,rect.size.width);
        }
        else
        {
            croppedSize = rect.size;
        }

        Mat rotMat = Imgproc.getRotationMatrix2D(rect.center,angle,1);
        Mat rotated = new Mat();
        Imgproc.warpAffine(img,rotated,rotMat,new Size(img.width(),img.height()),Imgproc.INTER_CUBIC);
        Mat cropped = new Mat();
        Imgproc.getRectSubPix(rotated,croppedSize,rect.center,cropped);

        Bitmap bmap = Bitmap.createBitmap(cropped.cols(),cropped.rows(), Bitmap.Config.ARGB_8888);

//        }

        Utils.matToBitmap(cropped,bmap);
        Log.d("TAG","GD");

    }
}
