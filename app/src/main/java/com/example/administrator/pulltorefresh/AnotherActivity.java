package com.example.administrator.pulltorefresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImageView;

public class AnotherActivity extends AppCompatActivity {

    private CropImageView mCropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another);
        mCropImageView = ((CropImageView) findViewById(R.id.cropImageView));
        mCropImageView.setImageResource(R.mipmap.ic_launcher);
//        Bitmap cropped = mCropImageView.getCroppedImage();
// or (must subscribe to async event using cropImageView.setOnGetCroppedImageCompleteListener(listener))
//        mCropImageView.getCroppedImageAsync(CropImageView.CropShape.RECTANGLE, 400, 400);
    }
}
