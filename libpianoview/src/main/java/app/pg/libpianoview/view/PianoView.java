package app.pg.libpianoview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import app.pg.libpianoview.R;
import app.pg.libpianoview.entity.Piano;
import app.pg.libpianoview.entity.PianoKey;
import app.pg.libpianoview.listener.OnPianoListener;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ChengTao on 2016-11-25.
 */

public class PianoView extends View {
  private final static String TAG = "PianoView";
  //定义钢琴键
  private Piano piano = null;
  private ArrayList<PianoKey[]> whitePianoKeys;
  private ArrayList<PianoKey[]> blackPianoKeys;
  //被点击过的钢琴键
  private final CopyOnWriteArrayList<PianoKey> pressedKeys = new CopyOnWriteArrayList<>();
  //画笔
  private final Paint paint;
  //定义标识音名的正方形
  private final RectF square;
  //正方形背景颜色
  private String[] pianoColors = {
          "#C0C0C0",
          "#FBB3B3",
          "#A2DCD7",
          "#D1C3EC",
          "#FFCC80",
          "#E5EF82",
          "#98DFFF",
          "#AFDFB1",
          "#C0C0C0"
  };
  //上下文
  private final Context context;
  //布局的宽度
  private int layoutWidth = 0;
  //缩放比例
  private float scale = 1;
  //接口
  private OnPianoListener pianoListener;
  //钢琴被滑动的一些属性
  private int progress = 0;
  //设置是否可以点击
  private boolean canPress = true;
  //初始化结束
  private boolean isInitFinish = false;
  private int minRange = 0;
  private int maxRange = 0;

  //构造函数
  public PianoView(Context context) {
    this(context, null);
  }

  public PianoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PianoView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.context = context;
    paint = new Paint();
    paint.setAntiAlias(true);
    //初始化画笔
    paint.setStyle(Paint.Style.FILL);
    //初始化正方形
    square = new RectF();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    Log.e(TAG, "onMeasure");
    Drawable whiteKeyDrawable = ContextCompat.getDrawable(context, R.drawable.white_piano_key);
    //最小高度
    int whiteKeyHeight = whiteKeyDrawable.getIntrinsicHeight();
    //获取布局中的高度和宽度及其模式
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    //设置高度
    switch (heightMode) {
      case MeasureSpec.AT_MOST:
        height = Math.min(height, whiteKeyHeight);
        break;
      case MeasureSpec.UNSPECIFIED:
        height = whiteKeyHeight;
        break;
      default:
        break;
    }
    //设置缩放比例
    scale = (float) (height - getPaddingTop() - getPaddingBottom()) / (float) (whiteKeyHeight);
    layoutWidth = width - getPaddingLeft() - getPaddingRight();
    //设置布局高度和宽度
    setMeasuredDimension(width, height);
  }

  @Override protected void onDraw(Canvas canvas) {
    //初始化钢琴
    if (piano == null) {
      minRange = 0;
      maxRange = layoutWidth;
      piano = new Piano(context, scale);
      //获取白键
      whitePianoKeys = piano.getWhitePianoKeys();
      //获取黑键
      blackPianoKeys = piano.getBlackPianoKeys();
    }
    //初始化白键
    if (whitePianoKeys != null) {
      for (int i = 0; i < whitePianoKeys.size(); i++) {
        for (PianoKey key : whitePianoKeys.get(i)) {
          paint.setColor(Color.parseColor(pianoColors[i]));
          key.getKeyDrawable().draw(canvas);
          //初始化音名区域
          Rect r = key.getKeyDrawable().getBounds();
          int sideLength = (r.right - r.left) / 2;
          int left = r.left + sideLength / 2;
          int top = r.bottom - sideLength - sideLength / 3;
          int right = r.right - sideLength / 2;
          int bottom = r.bottom - sideLength / 3;
          square.set(left, top, right, bottom);
          canvas.drawRoundRect(square, 6f, 6f, paint);
          paint.setColor(Color.BLACK);
          paint.setTextSize(sideLength / 1.8f);
          Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
          int baseline =
              (int) ((square.bottom + square.top - fontMetrics.bottom - fontMetrics.top) / 2);
          paint.setTextAlign(Paint.Align.CENTER);
          canvas.drawText(key.getLetterName(), square.centerX(), baseline, paint);
        }
      }
    }
    //初始化黑键
    if (blackPianoKeys != null) {
      for (int i = 0; i < blackPianoKeys.size(); i++) {
        for (PianoKey key : blackPianoKeys.get(i)) {
          key.getKeyDrawable().draw(canvas);
        }
      }
    }
    if (!isInitFinish && piano != null && pianoListener != null) {
      isInitFinish = true;
      pianoListener.onPianoInitFinish();
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getActionMasked();
    if (!canPress) {
      return false;
    }
    switch (action) {
      //当第一个手指点击按键的时候
      case MotionEvent.ACTION_DOWN:
        //多点触控，当其他手指点击键盘的手
      case MotionEvent.ACTION_POINTER_DOWN:
        handleDown(event.getActionIndex(), event);
        break;
      //当手指在键盘上滑动的时候
      case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < event.getPointerCount(); i++) {
          handleMove(i, event);
        }
        for (int i = 0; i < event.getPointerCount(); i++) {
          handleDown(i, event);
        }
        break;
      //多点触控，当其他手指抬起的时候
      case MotionEvent.ACTION_POINTER_UP:
        handlePointerUp(event.getPointerId(event.getActionIndex()));
        break;
      //但最后一个手指抬起的时候
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        handleUp();
        return false;
      default:
        break;
    }
    return true;
  }

  /**
   * 处理按下事件
   *
   * @param which 那个触摸点
   * @param event 事件对象
   */
  private void handleDown(int which, MotionEvent event) {
    int x = (int) event.getX(which) + this.getScrollX();
    int y = (int) event.getY(which);
    //检查白键
    for (int i = 0; i < whitePianoKeys.size(); i++) {
      for (PianoKey key : whitePianoKeys.get(i)) {
        if (!key.isPressed() && key.contains(x, y)) {
          handleWhiteKeyDown(which, event, key);
        }
      }
    }
    //检查黑键
    for (int i = 0; i < blackPianoKeys.size(); i++) {
      for (PianoKey key : blackPianoKeys.get(i)) {
        if (!key.isPressed() && key.contains(x, y)) {
          handleBlackKeyDown(which, event, key);
        }
      }
    }
  }

  /**
   * 处理白键点击
   *
   * @param which 那个触摸点
   * @param event 事件
   * @param key 钢琴按键
   */
  private void handleWhiteKeyDown(int which, MotionEvent event, PianoKey key) {
    key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
    key.setPressed(true);
    if (event != null) {
      key.setFingerID(event.getPointerId(which));
    }
    pressedKeys.add(key);
    invalidate(key.getKeyDrawable().getBounds());
    if (pianoListener != null) {
      pianoListener.onPianoClick(key.getType(), key.getVoice(), key.getGroup(),
          key.getPositionOfGroup());
    }
  }

  /**
   * 处理黑键点击
   *
   * @param which 那个触摸点
   * @param event 事件
   * @param key 钢琴按键
   */
  private void handleBlackKeyDown(int which, MotionEvent event, PianoKey key) {
    key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
    key.setPressed(true);
    if (event != null) {
      key.setFingerID(event.getPointerId(which));
    }
    pressedKeys.add(key);
    invalidate(key.getKeyDrawable().getBounds());
    if (pianoListener != null) {
      pianoListener.onPianoClick(key.getType(), key.getVoice(), key.getGroup(),
          key.getPositionOfGroup());
    }
  }

  /**
   * 处理滑动
   *
   * @param which 触摸点下标
   * @param event 事件对象
   */
  private void handleMove(int which, MotionEvent event) {
    int x = (int) event.getX(which) + this.getScrollX();
    int y = (int) event.getY(which);
    for (PianoKey key : pressedKeys) {
      if (key.getFingerID() == event.getPointerId(which)) {
        if (!key.contains(x, y)) {
          key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
          invalidate(key.getKeyDrawable().getBounds());
          key.setPressed(false);
          key.resetFingerID();
          pressedKeys.remove(key);
        }
      }
    }
  }

  /**
   * 处理多点触控时，手指抬起事件
   *
   * @param pointerId 触摸点ID
   */
  private void handlePointerUp(int pointerId) {
    for (PianoKey key : pressedKeys) {
      if (key.getFingerID() == pointerId) {
        key.setPressed(false);
        key.resetFingerID();
        key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
        invalidate(key.getKeyDrawable().getBounds());
        pressedKeys.remove(key);
        break;
      }
    }
  }

  /**
   * 处理最后一个手指抬起事件
   */
  private void handleUp() {
    if (pressedKeys.size() > 0) {
      for (PianoKey key : pressedKeys) {
        key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
        key.setPressed(false);
        invalidate(key.getKeyDrawable().getBounds());
      }
      pressedKeys.clear();
    }
  }

  //-----公共方法

  /**
   * 获取钢琴控件的总长度
   *
   * @return 钢琴控件的总长度
   */
  public int getPianoWidth() {
    if (piano != null) {
      return piano.getPianoWith();
    }
    return 0;
  }

  /**
   * 获取钢琴布局的实际宽度
   *
   * @return 钢琴布局的实际宽度
   */
  public int getLayoutWidth() {
    return layoutWidth;
  }

  /**
   * 设置显示音名的矩形的颜色<br>
   * <b>注:一共9中颜色</b>
   *
   * @param pianoColors 颜色数组，长度为9
   */
  public void setPianoColors(String[] pianoColors) {
    if (pianoColors.length == 9) {
      this.pianoColors = pianoColors;
    }
  }

  /**
   * 设置是否可点击
   *
   * @param canPress 是否可点击
   */
  public void setCanPress(boolean canPress) {
    this.canPress = canPress;
  }

  /**
   * 移动
   *
   * @param progress 移动百分比
   */
  public void scroll(int progress) {
    int x;
    switch (progress) {
      case 0:
        x = 0;
        break;
      case 100:
        x = getPianoWidth() - getLayoutWidth();
        break;
      default:
        x = (int) (((float) progress / 100f) * (float) (getPianoWidth() - getLayoutWidth()));
        break;
    }
    minRange = x;
    maxRange = x + getLayoutWidth();
    this.scrollTo(x, 0);
    this.progress = progress;
  }

  //接口

  /**
   * 初始化钢琴相关界面
   *
   * @param pianoListener 钢琴接口
   */
  public void setPianoListener(OnPianoListener pianoListener) {
    this.pianoListener = pianoListener;
  }

  //-----私有方法

  @Override protected void onRestoreInstanceState(Parcelable state) {
    super.onRestoreInstanceState(state);
    postDelayed(() -> scroll(progress), 200);
  }
}
