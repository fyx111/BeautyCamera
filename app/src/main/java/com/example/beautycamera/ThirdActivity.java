package com.example.beautycamera;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import static java.lang.System.currentTimeMillis;

public class ThirdActivity extends AppCompatActivity implements View.OnClickListener {
    private int REQUEST_CAPTURE_IMAGE = 1;
    private int option2 = 0;
    private int brightness2;
    private double ContrastRatio2=1.0;
    private int buffing2=0;
    Bitmap bm;
    private Mat imagesave2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);
        Button button1 = (Button)this.findViewById(R.id.select_image_btn);
        Button button2 = (Button)this.findViewById(R.id.change_image_btn);
        Button button3 = (Button)this.findViewById(R.id.save_image_btn);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        SeekBar seekBar3 = (SeekBar)findViewById(R.id.seekBar3);
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness2=((seekBar.getProgress()-250)/2);                                          //亮度调节
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {

            }
        });

        SeekBar seekBar4 = (SeekBar)findViewById(R.id.seekBar4);
        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ContrastRatio2=(seekBar.getProgress()/200.0*2);                                          //对比度调节
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar1) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar1) {

            }
        });

        Switch aswitch = (Switch)findViewById(R.id.switch2);
        aswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    buffing2=1;          //打开磨皮
                }else{
                    buffing2=0;          //关闭磨皮
                }
            }
        });

    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.change_image_btn:
                ImageView imageView = (ImageView)this.findViewById(R.id.imageView);

                Mat frame = new Mat ();
                //Mat frame = new Mat();
                Utils.bitmapToMat(bm, frame);
                Mat frame2 = new Mat();
                Core.add(frame, new Scalar(brightness2,brightness2,brightness2), frame2);     //亮度
                frame = frame2;
                Mat frame3 = new Mat();
                Core.multiply(frame,new Scalar(ContrastRatio2,ContrastRatio2,ContrastRatio2),frame3);//对比度
                frame = frame3;
                if(buffing2==1){                                //磨皮
                    Mat frame4 = new Mat();
                    //将图像转换为三通道（CV_8UC3），因为双边滤波的操作不支持原图像的4通道（CV_8UC4）
                    Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGBA2RGB);
                    //中值滤波，去除图像的噪点（或是脸部小斑点）
                    Imgproc.medianBlur(frame,frame,3);
                    //双边滤波，保留图像边缘
                    Imgproc.bilateralFilter(frame,frame4,40,50,30);
                    //锐化图像，让边缘更加明显
                    Mat kx = new Mat(3, 3, CvType.CV_32FC1);
                    float[] robert_x = new float[]{0, -1, 0, -1, 5, -1, 0, -1, 0};
                    kx.put(0, 0, robert_x);
                    Imgproc.filter2D(frame4,frame4,-1,kx );
                    frame=frame4;
                }else {
                    Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGBA2RGB);
                }
                process(frame);
                imagesave2=frame;
                //Bitmap bm2 = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
                Bitmap bm2 = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);;
                Utils.matToBitmap(frame, bm2);
                imageView.setImageBitmap(bm2);
                break;
            case R.id.save_image_btn:
                File filedir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTimeMillis()) + ".jpg";
                File tempFile = new File(filedir.getAbsoluteFile()+File.separator, name);
                String fileName = tempFile.getAbsolutePath();
                Toast.makeText(this, fileName + " 保存成功辣！放在DCIM/Camera哦*^_^*", Toast.LENGTH_SHORT).show();
                Imgproc.cvtColor(imagesave2, imagesave2, Imgproc.COLOR_RGBA2BGR);
                Imgcodecs.imwrite(tempFile.getAbsolutePath(),imagesave2);//保存
                break;
            default:
                break;
        }
    }
    private void pickUpImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                ImageView imageView = (ImageView)this.findViewById(R.id.imageView);
                Uri uri = data.getData();
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bm=bitmap;
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera2, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.invert2:
                option2 = 1;
                break;
            case R.id.edge2:
                option2 = 2;
                break;
            case R.id.sobel2:
                option2 = 3;
                break;
            case R.id.blur2:
                option2 = 4;
                break;
            case R.id.grey2:
                option2 = 5;
                break;
            case R.id.dilate2:
                option2 = 6;
                break;
            case R.id.erode2:
                option2 = 7;
                break;
            case R.id.normal2:
                option2 = 8;
                break;
            default:
                option2 = 0;
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void process(Mat frame) {
        switch (option2){
            case 1:
                Core.bitwise_not(frame, frame);                             //反相
                break;
            case 2:
                Mat edges = new Mat();
                Imgproc.Canny(frame, edges, 100, 200, 3, false);
                Mat result = Mat.zeros(frame.size(), frame.type());
                frame.copyTo(result, edges);                                //描边
                result.copyTo(frame);
                edges.release();
                result.release();
                break;
            case 3:
                Mat gradx = new Mat();
                Imgproc.Sobel(frame, gradx, CvType.CV_32F, 1, 0);
                Core.convertScaleAbs(gradx, gradx);                         //梯度
                gradx.copyTo(frame);
                gradx.release();
                break;
            case 4:
                Mat temp = new Mat();
                Imgproc.blur(frame, temp, new Size(15, 15));//模糊
                temp.copyTo(frame);
                temp.release();
                break;
            case 5:
                Mat grey = new Mat();
                Imgproc.cvtColor(frame,grey,Imgproc.COLOR_BGRA2GRAY);      //灰白
                grey.copyTo(frame);
                grey.release();
                break;
            case 6:
                Mat dilate = Imgproc.getStructuringElement(                //膨胀
                        Imgproc.MORPH_RECT, new Size(15, 15), new Point(-1, -1));
                Imgproc.morphologyEx(frame, dilate, Imgproc.MORPH_DILATE, dilate);
                dilate.copyTo(frame);
                dilate.release();
                break;
            case 7:
                Mat erode = Imgproc.getStructuringElement(                 //腐蚀
                        Imgproc.MORPH_RECT, new Size(15, 15), new Point(-1, -1));
                Imgproc.morphologyEx(frame, erode, Imgproc.MORPH_ERODE, erode);
                erode.copyTo(frame);
                erode.release();
                break;
            case 8:                                                        //正常
                break;
            default:
                break;
        }
    }

}
