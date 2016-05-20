package com.wingjay.blurimageview;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wingjay.blurimageviewlib.BlurImageView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

  @Bind(R.id.fast_blur_btn)
  Button fastBlurBtn;

  @Bind(R.id.full_blur_image_view)
  BlurImageView blurImageView;

  @Bind(R.id.image_indicator)
  TextView imageIndicator;

  @Bind(R.id.about_author)
  TextView aboutAuthor;

  boolean alreadyLoad = false;

  int[] mediumSmRes = {
    R.drawable.medium_sm_1,
    R.drawable.medium_sm_2,
    R.drawable.medium_sm_3,
    R.drawable.medium_sm_4
  };

  int[] mediumNmRes = {
    R.drawable.medium_nm_1,
    R.drawable.medium_nm_2,
    R.drawable.medium_nm_3,
    R.drawable.medium_nm_4
  };

  String[] mediumSmUrl = {
    "http://pic28.nipic.com/20130423/4748803_165423116180_2.png",
    "http://pic28.nipic.com/20130423/4748803_165423116180_2.png",
    "http://pic28.nipic.com/20130423/4748803_165423116180_2.png",
    "http://pic28.nipic.com/20130423/4748803_165423116180_2.png"
  };

  String[] mediumNmUrl = {
    "https://gss0.bdstatic.com/5eR1dDebRNRTm2_p8IuM_a/res/img/richanglogo168_24.png",
    "https://gss0.bdstatic.com/5eR1dDebRNRTm2_p8IuM_a/res/img/richanglogo168_24.png",
    "https://gss0.bdstatic.com/5eR1dDebRNRTm2_p8IuM_a/res/img/richanglogo168_24.png",
    "https://gss0.bdstatic.com/5eR1dDebRNRTm2_p8IuM_a/res/img/richanglogo168_24.png"
  };

  int[] blurImageViewProgressBgColor = {
      Color.BLACK,
      Color.BLACK,
      Color.parseColor("#E29C45"),
      Color.parseColor("#E29C45"),
  };

  int[] blurImageViewProgressClor = {
      Color.WHITE,
      Color.parseColor("#789262"),
      Color.parseColor("#7BCFA6"),
      Color.parseColor("#519A73"),
  };


  int currentIndex = 0;
  int getResIndex() {
    if (currentIndex > 3) {
      currentIndex = currentIndex - 4;
    }
    return currentIndex;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    String aboutAuthorString = "<u>Find me here: wingjay (https://github.com/wingjay)</u>";
    aboutAuthor.setText(Html.fromHtml(aboutAuthorString));
    aboutAuthor.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/wingjay"));
        startActivity(browserIntent);
      }
    });

  }

  @OnClick(R.id.fast_blur_btn)
  void doFastBlur() {
    if (!alreadyLoad) {
      customizeBlurImageView();

      int blurFactor = BlurImageView.DEFAULT_BLUR_FACTOR;
      blurImageView.setBlurFactor(blurFactor);
      blurImageView.setFullImageByUrl(mediumSmUrl[getResIndex()], mediumNmUrl[getResIndex()]);

      alreadyLoad = true;
      fastBlurBtn.setText("Click and Clear current image");
      imageIndicator.setText((getResIndex() + 1) + "/" + mediumNmUrl.length);
    } else {
      blurImageView.clear();

      currentIndex++;
      alreadyLoad = false;
      fastBlurBtn.setText("Click to load new Image");
    }
  }

  private void customizeBlurImageView() {
    blurImageView.setProgressBarBgColor(blurImageViewProgressBgColor[getResIndex()]);
    blurImageView.setProgressBarColor(blurImageViewProgressClor[getResIndex()]);
  }

  @Override
  protected void onDestroy() {
    blurImageView.cancelImageRequestForSafty();
    super.onDestroy();
  }

}
