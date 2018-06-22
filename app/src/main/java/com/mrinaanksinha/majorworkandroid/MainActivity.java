package com.mrinaanksinha.majorworkandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//TODO: ensure that if permissions not present, appropriate screen shown after permissions entered
public class MainActivity extends AppCompatActivity
{

    public Mat img;
    private final String TESS_DATA_PATH ="/tessdata";
    private TessBaseAPI tessBaseAPI;
    private FocusBox focusBox;


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

            Bitmap bmp = ImageProcessingTools.getCroppedBitmap(pictureFile.getAbsolutePath(), preview.getWidth(), preview.getHeight(), focusBox.getSelectorViewBox());
            ExtractText(bmp);

            Log.d("TAG", "IMAGE LOADED");
        }
    };

    Button button_capture;
    private static final int REQUEST_PERMISSIONS = 200;

    private ImageView selectorView;
    private Camera camera;
    private CameraPreview preview;
    private FrameLayout previewFrame;

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

        prepareTessData();
        selectorView = (ImageView) findViewById(R.id.selectorView);
        focusBox = new FocusBox(getApplicationContext(),selectorView);
        camera = getCameraInstance(getApplicationContext());

        preview = new CameraPreview(this, camera, this);
        preview.initFocus(ImageProcessingTools.getBox(selectorView));
        previewFrame = (FrameLayout) findViewById(R.id.cameraPreviewFrame);
        previewFrame.addView(preview);
        previewFrame.addView(focusBox);
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
//        if (camera == null)
//        {
//            camera = getCameraInstance(getApplicationContext());
//            CameraPreview.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
//        }
//    }

    private void releaseCamera()
    {
        if (camera != null)
        {
            preview.removeFocusCallback();
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


    public void ExtractText(Bitmap bitmap)
    {
        bitmap = preprocess(bitmap);

        String detectedTextBoxes = detectText(bitmap);

        String infixEquation = EquationTools.standardizeEquationToInfix(detectedTextBoxes,new android.util.Size(bitmap.getWidth(),bitmap.getHeight()));
        ArrayList<String> postfixEquation = EquationTools.infixToPostfix(infixEquation);
        String equationSolution = EquationTools.solvePostfix(postfixEquation);
        String formattedEquationWSolution = infixEquation + " = " + equationSolution;

        button_capture.setText(formattedEquationWSolution);


    }

    private Bitmap preprocess(Bitmap bitmap)
    {
        //        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize =1;
//        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.example1, options);
//        bitmap = ImageProcessingTools.rotateBitmap(bitmap,90);

        Utils.bitmapToMat(bitmap, img);

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(3, 3), 0);
        Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 55, 10);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(2,2));
        Imgproc.morphologyEx(img,img, Imgproc.MORPH_ERODE,kernel);
        Mat croppedImg = RotateAndCrop(img);
        bitmap = Bitmap.createBitmap(croppedImg.cols(), croppedImg.rows(), Bitmap.Config.ARGB_8888);

//        }

        Utils.matToBitmap(croppedImg, bitmap);
        return bitmap;
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

    private void prepareTessData()
    {

        try
        {
            File dir = new File(getFilesDir().toString() + TESS_DATA_PATH);
            if (!dir.exists())
            {
                dir.mkdir();
            }
            String fileList[] = getAssets().list("");
            for (String fileName : fileList)
            {
                String pathToDataFile = getFilesDir() + TESS_DATA_PATH + "/" + fileName;
                if (!(new File(pathToDataFile)).exists())
                {
                    InputStream is = getAssets().open(fileName);
                    OutputStream os = new FileOutputStream(pathToDataFile);
                    byte[] buff = new byte[1024];
                    int len;
                    while ((len = is.read(buff)) > 0)
                    {
                        os.write(buff, 0, len);
                    }
                    is.close();
                    os.close();
                }
            }
        }

        catch (IOException e)
        {
            e.printStackTrace();

        }

        try
        {
            tessBaseAPI = new TessBaseAPI();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        tessBaseAPI.setPageSegMode(TessBaseAPI.OEM_TESSERACT_ONLY);//Change to Tesseract_cube_maybe Slower but more accurate
        tessBaseAPI.setDebug(true);

    }



    private String detectText(Bitmap bitmap)
    {
        try
        {
            tessBaseAPI.init(getFilesDir().toString(),"eng");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        tessBaseAPI.setImage(bitmap);
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789+=()-/x*");
//        String extractedText = tessBaseAPI.getUTF8Text();
        String extractedBoxText = tessBaseAPI.getBoxText(0);

        tessBaseAPI.clear();
        tessBaseAPI.end();
        return extractedBoxText;

    }


}

