package com.example.beautycamera;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import static java.lang.System.currentTimeMillis;


public class SecondActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnClickListener{
    private JavaCameraView mjavaCameraView;
    private static int cameraIndex=0;
    private int option = 0;
    private Mat imagesave;
    private int brightness;
    private double ContrastRatio=1.0;
    private int buffing=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        //for 6.0 and 6.0 above, apply permission
        if(Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mjavaCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        mjavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mjavaCameraView.setCvCameraViewListener(this);

        Button button = (Button)this.findViewById(R.id.take_photo_btn);               //????????????
        RadioButton backOption = (RadioButton)this.findViewById(R.id.backCameraBtn);  //???????????????????????????
        RadioButton frontOption = (RadioButton)this.findViewById(R.id.frontCameraBtn);//???????????????????????????
        backOption.setSelected(true);

        backOption.setOnClickListener(this);
        frontOption.setOnClickListener(this);
        button.setOnClickListener(this);
        SeekBar seekBar1 = (SeekBar)findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness=((seekBar.getProgress()-250)/2);                                          //????????????
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {

            }
        });

        SeekBar seekBar2 = (SeekBar)findViewById(R.id.seekBar2);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ContrastRatio=(seekBar.getProgress()/200.0*2);                                          //???????????????
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {

            }
        });

        Switch aswitch = (Switch)findViewById(R.id.switch1);
        aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    buffing=1;          //????????????
                }else{
                    buffing=0;          //????????????
                }
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.invert:
                option = 1;
                break;
            case R.id.edge:
                option = 2;
                break;
            case R.id.sobel:
                option = 3;
                break;
            case R.id.blur:
                option = 4;
                break;
            case R.id.grey:
                option = 5;
                break;
            case R.id.dilate:
                option = 6;
                break;
            case R.id.erode:
                option = 7;
                break;
            case R.id.normal:
                option = 8;
                break;
            default:
                option = 0;
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        Mat frame2 = new Mat();
        Core.add(frame, new Scalar(brightness,brightness,brightness), frame2);     //??????
        frame = frame2;
        Mat frame3 = new Mat();
        Core.multiply(frame2,new Scalar(ContrastRatio,ContrastRatio,ContrastRatio),frame3);//?????????
        frame = frame3;
        if(buffing==1){                                //??????
            Mat frame4 = new Mat();
            //??????????????????????????????CV_8UC3??????????????????????????????????????????????????????4?????????CV_8UC4???
            Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGBA2RGB);
            //???????????????????????????????????????????????????????????????
            Imgproc.medianBlur(frame,frame,3);
            //?????????????????????????????????
            Imgproc.bilateralFilter(frame,frame4,40,50,30);
            //????????????????????????????????????
            Mat kx = new Mat(3, 3, CvType.CV_32FC1);
            float[] robert_x = new float[]{0, -1, 0, -1, 5, -1, 0, -1, 0};
            kx.put(0, 0, robert_x);
            Imgproc.filter2D(frame4,frame4,-1,kx );
            frame=frame4;
        }else {
        }
        if(cameraIndex == 0) {                          //???????????????
            if(this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
            }
            imagesave = frame;
            process(frame);
            return frame;
        } else {                                        //???????????????
            if(this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
                Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
                Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);
            }
            Core.flip(frame, frame, 1);
            imagesave = frame;
            process(frame);
            return frame;
        }
    }
    private void process(Mat frame) {
        switch (option){
            case 1:
                Core.bitwise_not(frame, frame);                             //??????
                break;
            case 2:
                Mat edges = new Mat();
                Imgproc.Canny(frame, edges, 100, 200, 3, false);
                Mat result = Mat.zeros(frame.size(), frame.type());
                frame.copyTo(result, edges);                                //??????
                result.copyTo(frame);
                edges.release();
                result.release();
                break;
            case 3:
                Mat gradx = new Mat();
                Imgproc.Sobel(frame, gradx, CvType.CV_32F, 1, 0);
                Core.convertScaleAbs(gradx, gradx);                         //??????
                gradx.copyTo(frame);
                gradx.release();
                break;
            case 4:
                Mat temp = new Mat();
                Imgproc.blur(frame, temp, new Size(15, 15));//??????
                temp.copyTo(frame);
                temp.release();
                break;
            case 5:
                Mat grey = new Mat();
                Imgproc.cvtColor(frame,grey,Imgproc.COLOR_BGRA2GRAY);      //??????
                grey.copyTo(frame);
                grey.release();
                break;
            case 6:
                Mat dilate = Imgproc.getStructuringElement(                //??????
                        Imgproc.MORPH_RECT, new Size(15, 15), new Point(-1, -1));
                Imgproc.morphologyEx(frame, dilate, Imgproc.MORPH_DILATE, dilate);
                dilate.copyTo(frame);
                dilate.release();
                break;
            case 7:
                Mat erode = Imgproc.getStructuringElement(                 //??????
                        Imgproc.MORPH_RECT, new Size(15, 15), new Point(-1, -1));
                Imgproc.morphologyEx(frame, erode, Imgproc.MORPH_ERODE, erode);
                erode.copyTo(frame);
                erode.release();
                break;
            case 8:                                                        //??????
                break;
            default:
                break;
        }
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.frontCameraBtn) {
            cameraIndex = 1;
        } else if(id == R.id.backCameraBtn) {
            cameraIndex = 0;
        }
        if(id==R.id.take_photo_btn){                                //????????????
            File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
            String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeMillis()) + ".jpg";
            File tempFile = new File(filedir.getAbsoluteFile()+File.separator, name);
            String fileName = tempFile.getAbsolutePath();
            Toast.makeText(this, fileName + " ????????????????????????DCIM/Camera???*^_^*", Toast.LENGTH_SHORT).show();
            Imgproc.cvtColor(imagesave, imagesave, Imgproc.COLOR_RGBA2BGR);
            Imgcodecs.imwrite(tempFile.getAbsolutePath(),imagesave);//??????

        }
        mjavaCameraView.setCameraIndex(cameraIndex);
        if (mjavaCameraView != null) {
            mjavaCameraView.disableView();
        }
        mjavaCameraView.enableView();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mjavaCameraView != null)
            mjavaCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mjavaCameraView != null)
            mjavaCameraView.disableView();
    }

    public void onResume() {
        super.onResume();
        if (mjavaCameraView != null) {
            mjavaCameraView.setCameraIndex(cameraIndex);
            mjavaCameraView.enableFpsMeter();
            mjavaCameraView.enableView();
        }
    }
}
