package com.mrinaanksinha.majorworkandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

//TODO: ensure that if permissions not present, appropriate screen shown after permissions entered
public class MainActivity extends AppCompatActivity
{

    public Mat img;
    Bitmap bitmap;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    img = new Mat();

                }
                break;
                default:
                {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private android.util.Size previewsize;
    private android.util.Size jpegSizes[] = null;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    Button button_capture;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_PERMISSIONS = 200;

    private ImageView selectorView;
    private Camera camera;
    private CameraPreview preview;
    private FrameLayout previewFrame;

    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
        {


            if (requestCode == REQUEST_PERMISSIONS)
            {
                Toast.makeText(this, "You need to provide permissions to use the app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        if (!OpenCVLoader.initDebug())
        {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for Initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
        else
        {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        selectorView = (ImageView) findViewById(R.id.selectorView);

        camera = getCameraInstance(getApplicationContext());

        preview = new CameraPreview(this, camera, this);
        preview.initFocus(Tools.getBox(selectorView));
        previewFrame = (FrameLayout) findViewById(R.id.cameraPreviewFrame);
        previewFrame.addView(preview);

//
//        textureView = (TextureView) findViewById(R.id.textureview);
//        textureView.setSurfaceTextureListener(surfaceTextureListener);
        button_capture = (Button) findViewById(R.id.button_capture);
        button_capture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                getPicture();
                camera.takePicture(null, null, pictureCallback);
            }
        });


        selectorView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                preview.focusCamera();
            }
        });


    }

    @Override
    protected void onPause()
    {
        super.onPause();
        releaseCamera();
    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//        if (preview.getParent() != previewFrame)
//        {
//            camera = getCameraInstance(getApplicationContext());
//            CameraPreview.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
//            previewFrame.addView(preview);
//        }
//    }

    private void releaseCamera()
    {
        if (camera != null)
        {
            camera.release();
            camera = null;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(Context context)
    {
        Camera cam = null;
        try
        {
            cam = Camera.open(); // attempt to get a Camera instance
        }
        catch (RuntimeException e)
        {
            // Camera is not available (in use or does not exist)
            Toast.makeText(context, "Camera in use by another application. Please close the camera before using this app.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cam; // returns null if camera is unavailable
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            File pictureFile = getOutputMediaFile();
            long x = pictureFile.length();
            if (pictureFile == null)
            {
                Log.d("TAG", "Error creating media file, check storage permissions: ");
                return;
            }

            try
            {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            }
            catch (FileNotFoundException e)
            {
                Log.d("TAG", "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                Log.d("TAG", "Error accessing file: " + e.getMessage());
            }

            Bitmap bmp = Tools.getCroppedBitmap(pictureFile.getAbsolutePath(), preview.getWidth(), preview.getHeight(), Tools.getBox(selectorView));
            ExtractText(bmp);

            Log.d("TAG", "IMAGE LOADED");
        }
    };

    private File getOutputMediaFile()
    {
        File mediaStorageDir = new File(getFilesDir(), getString(R.string.app_name));

        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");


        return mediaFile;
    }


    private void ExtractText(Bitmap bitmap)
    {
//        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.example3, options);
//        bitmap = Tools.rotateBitmap(bitmap,90);

        Utils.bitmapToMat(bitmap, img);

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 55, 10);

        Mat croppedImg = RotateAndCrop(img);
        bitmap = Bitmap.createBitmap(croppedImg.cols(), croppedImg.rows(), Bitmap.Config.ARGB_8888);

//        }

        Utils.matToBitmap(croppedImg, bitmap);
        selectorView.setImageBitmap(bitmap);
        Log.d("TAG", "GD");
    }

    private Mat RotateAndCrop(@NonNull Mat src)
    {
        RotatedRect rect;
        Mat points = Mat.zeros(src.size(), src.channels());
        Core.findNonZero(src, points);

        MatOfPoint mpoints = new MatOfPoint(points);
        MatOfPoint2f points2f = new MatOfPoint2f(mpoints.toArray()); //TAKES WAY TOO LONGGGGG!!!!!!!

//        if (points2f.rows() > 0) {
        rect = Imgproc.minAreaRect(points2f);
        double angle = rect.angle;
        Size croppedSize;
        if (rect.angle < -45.0)
        {
            angle += 90;
            croppedSize = new Size(rect.size.height, rect.size.width);
        }
        else
        {
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

