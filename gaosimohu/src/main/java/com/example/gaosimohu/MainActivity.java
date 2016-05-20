package com.example.gaosimohu;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.gaosimohu.util.FastBlurUtil;

public class MainActivity extends AppCompatActivity {

    private Button gaosi;
    private ImageView imageview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
//        gaosi = ((Button) findViewById(R.id.gaosi));
        imageview = ((ImageView) findViewById(R.id.imageview));

        imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getWindow().getDecorView();
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache(true);
                Bitmap bitmap = view.getDrawingCache();
                new Async().execute(bitmap);
            }
        });
    }

    private class Async extends AsyncTask<Bitmap,Void,Bitmap> {

        @Override
        protected Bitmap doInBackground(Bitmap... params) {

            return FastBlurUtil.doBlur(params[0],30,true);
        }
        @SuppressLint("NewApi")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imageview.setBackground(new BitmapDrawable(bitmap));

        }
    }
}
