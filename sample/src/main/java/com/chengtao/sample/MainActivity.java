package com.chengtao.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;

import app.pg.libpianoview.entity.Piano;
import app.pg.libpianoview.listener.OnPianoListener;
import app.pg.libpianoview.view.PianoView;

@SuppressWarnings("FieldCanBeLocal") public class MainActivity extends Activity
    implements OnPianoListener, SeekBar.OnSeekBarChangeListener,
    View.OnClickListener {
  //flight_of_the_bumble_bee,simple_little_star_config
  private static final String CONFIG_FILE_NAME = "simple_little_star_config";
  private static final boolean USE_CONFIG_FILE = true;
  private PianoView pianoView;
  private SeekBar seekBar;
  private Button leftArrow;
  private Button rightArrow;
  private Button btnMusic;
  private int scrollProgress = 0;
  private final static float SEEKBAR_OFFSET_SIZE = -12;
  //
  private boolean isPlay = false;
  private static final long LITTER_STAR_BREAK_SHORT_TIME = 500;
  private static final long LITTER_STAR_BREAK_LONG_TIME = 1000;

  @Override protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //view
    pianoView = findViewById(R.id.pv);
    seekBar = findViewById(R.id.sb);
    seekBar.setThumbOffset((int) convertDpToPixel(SEEKBAR_OFFSET_SIZE));
    leftArrow = findViewById(R.id.iv_left_arrow);
    rightArrow = findViewById(R.id.iv_right_arrow);
    btnMusic = findViewById(R.id.iv_music);
    //listener
    pianoView.SetPianoListener(this);
    seekBar.setOnSeekBarChangeListener(this);
    rightArrow.setOnClickListener(this);
    leftArrow.setOnClickListener(this);
    btnMusic.setOnClickListener(this);
    //init
    if (USE_CONFIG_FILE) {
      AssetManager assetManager = getAssets();
    } else {
      initLitterStarList();
    }
  }

  /**
   * 初始化小星星列表
   */
  private void initLitterStarList() {
  }

  @Override public void onPianoInitFinish() {

  }

  @Override public void onPianoKeyPress(
          Piano.PianoKeyType type,
          Piano.PianoVoice voice,
          int group,
          int positionOfGroup,
          int argMidiNoteNumber) {
    Log.d("MainActivity", "===================================================");
    Log.d("MainActivity", "Piano.PianoKeyType = " + type);
    Log.d("MainActivity", "Piano.PianoVoice   = " + voice);
    Log.d("MainActivity", "group              = " + group);
    Log.d("MainActivity", "positionOfGroup    = " + positionOfGroup);
    Log.d("MainActivity", "argMidiNoteNumber  = " + argMidiNoteNumber);
    Log.d("MainActivity", "===================================================");
  }

  @Override public void onPianoKeyRelease(
          Piano.PianoKeyType type,
          Piano.PianoVoice voice,
          int group,
          int positionOfGroup,
          int argMidiNoteNumber) {

  }

  @Override public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    // FIXME:
//    pianoView.ScrollByPercent(i);
  }

  @Override public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override public void onStopTrackingTouch(SeekBar seekBar) {

  }

  @Override protected void onResume() {
    /**
     * 设置为横屏
     */
    if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    super.onResume();
  }

  @Override public void onClick(View view) {
    if (scrollProgress == 0) {
      try {
        scrollProgress = (pianoView.GetVisiblePianoWidth() * 100) / pianoView.GetFullPianoWidth();
      } catch (Exception e) {

      }
    }
    int progress;
    switch (view.getId()) {
      case R.id.iv_left_arrow:
        if (scrollProgress == 0) {
          progress = 0;
        } else {
          progress = seekBar.getProgress() - scrollProgress;
          if (progress < 0) {
            progress = 0;
          }
        }
        seekBar.setProgress(progress);
        break;
      case R.id.iv_right_arrow:
        if (scrollProgress == 0) {
          progress = 100;
        } else {
          progress = seekBar.getProgress() + scrollProgress;
          if (progress > 100) {
            progress = 100;
          }
        }
        seekBar.setProgress(progress);
        break;
      case R.id.iv_music:
        if (!isPlay) {
        }
        break;
    }
  }

  /**
   * Dp to px
   *
   * @param dp dp值
   * @return px 值
   */
  private float convertDpToPixel(float dp) {
    Resources resources = this.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }
}