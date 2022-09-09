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


//================================================================================================//
//================================================================================================//
public class PianoView extends View {
    private final static String TAG = "PianoView";
    private Piano piano = null;
    private ArrayList<PianoKey[]> whitePianoKeys;
    private ArrayList<PianoKey[]> blackPianoKeys;
    private final CopyOnWriteArrayList<PianoKey> pressedKeys = new CopyOnWriteArrayList<>();
    private final Paint paint;
    private final RectF square;
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
    private final Context   context;
    private int             layoutWidth = 0;
    private float           scaleHeight = 1;
    private OnPianoListener pianoListener;
    private int             progress = 0;
    private boolean         canPress = true;
    private boolean         isInitFinish = false;
    private int             minRange = 0;
    private int             maxRange = 0;

    //==================================================================================//
    //==================================================================================//
    public PianoView(Context context) {
        this(context, null);
    }

    //==================================================================================//
    public PianoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //==================================================================================//
    public PianoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        square = new RectF();
    }

    //==================================================================================//
    //==================================================================================//
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, "onMeasure");
        Drawable whiteKeyDrawable = ContextCompat.getDrawable(context, R.drawable.white_piano_key);

        int whiteKeyHeight = whiteKeyDrawable.getIntrinsicHeight();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

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

        scaleHeight = (float) (height - getPaddingTop() - getPaddingBottom()) / (float) (whiteKeyHeight);
        layoutWidth = width - getPaddingLeft() - getPaddingRight();

        setMeasuredDimension(width, height);
    }

    //==================================================================================//
    //==================================================================================//
    @Override protected void onDraw(Canvas canvas) {
        if (piano == null) {
            minRange       = 0;
            maxRange       = layoutWidth;
            piano          = new Piano(context, scaleHeight);
            whitePianoKeys = piano.getWhitePianoKeys();
            blackPianoKeys = piano.getBlackPianoKeys();
        }

        if (whitePianoKeys != null) {
            for (int i = 0; i < whitePianoKeys.size(); i++) {
                for (PianoKey key : whitePianoKeys.get(i)) {
                    paint.setColor(Color.parseColor(pianoColors[i]));
                    key.getKeyDrawable().draw(canvas);

                    // Draw the key names (e.g. C0, A4 etc.)
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

    //==================================================================================//
    //==================================================================================//
    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (!canPress) {
            return false;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                HandleDown(event.getActionIndex(), event);
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    HandleMove(i, event);
                }
                for (int i = 0; i < event.getPointerCount(); i++) {
                    HandleDown(i, event);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                HandlePointerUp(event.getPointerId(event.getActionIndex()));
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                HandleUp();
                return false;
            default:
                break;
        }
        return true;
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleDown(int which, MotionEvent event) {
        int x = (int) event.getX(which) + this.getScrollX();
        int y = (int) event.getY(which);

        for (int i = 0; i < whitePianoKeys.size(); i++) {
            for (PianoKey key : whitePianoKeys.get(i)) {
                if (!key.isPressed() && key.contains(x, y)) {
                    HandleWhiteKeyDown(which, event, key);
                }
            }
        }

        for (int i = 0; i < blackPianoKeys.size(); i++) {
            for (PianoKey key : blackPianoKeys.get(i)) {
                if (!key.isPressed() && key.contains(x, y)) {
                    HandleBlackKeyDown(which, event, key);
                }
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleWhiteKeyDown(int which, MotionEvent event, PianoKey key) {
        key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
        key.setPressed(true);

        if (event != null) {
            key.setFingerID(event.getPointerId(which));
        }

        pressedKeys.add(key);
        invalidate(key.getKeyDrawable().getBounds());

        if (pianoListener != null) {
            pianoListener.onPianoKeyPress(key.getType(), key.getVoice(), key.getGroup(),
                    key.getPositionOfGroup(), key.getMidiNoteNumber());
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleBlackKeyDown(int which, MotionEvent event, PianoKey key) {
        key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
        key.setPressed(true);

        if (event != null) {
            key.setFingerID(event.getPointerId(which));
        }

        pressedKeys.add(key);
        invalidate(key.getKeyDrawable().getBounds());

        if (pianoListener != null) {
            pianoListener.onPianoKeyPress(key.getType(), key.getVoice(), key.getGroup(),
                    key.getPositionOfGroup(), key.getMidiNoteNumber());
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleMove(int which, MotionEvent event) {
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

                    if (pianoListener != null) {
                        pianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                                key.getPositionOfGroup(), key.getMidiNoteNumber());
                    }
                }
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandlePointerUp(int pointerId) {
        for (PianoKey key : pressedKeys) {
            if (key.getFingerID() == pointerId) {
                key.setPressed(false);
                key.resetFingerID();
                key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
                invalidate(key.getKeyDrawable().getBounds());
                pressedKeys.remove(key);

                if (pianoListener != null) {
                    pianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                            key.getPositionOfGroup(), key.getMidiNoteNumber());
                }

                break;
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleUp() {
        if (pressedKeys.size() > 0) {
            for (PianoKey key : pressedKeys) {
                key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
                key.setPressed(false);
                invalidate(key.getKeyDrawable().getBounds());

                if (pianoListener != null) {
                    pianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                            key.getPositionOfGroup(), key.getMidiNoteNumber());
                }
            }
            pressedKeys.clear();
        }
    }

    //==================================================================================//
    //==================================================================================//
    public int GetFullPianoWidth() {
        if (piano != null) {
            return piano.getPianoWith();
        }
        return 0;
    }

    //==================================================================================//
    //==================================================================================//
    public int GetVisiblePianoWidth() {
        return layoutWidth;
    }

    //==================================================================================//
    //==================================================================================//
    public void SetPianoColors(String[] pianoColors) {
        if (pianoColors.length == 9) {
            this.pianoColors = pianoColors;
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetCanPress(boolean canPress) {
        this.canPress = canPress;
    }

    //==================================================================================//
    //==================================================================================//
    public void ScrollByPercent(int argProgressPercent) {
        int x;
        switch (argProgressPercent) {
            case 0:
                x = 0;
                break;
            case 100:
              x = GetFullPianoWidth() - GetVisiblePianoWidth();
              break;
            default:
              x = (int) (((float) argProgressPercent / 100f) * (float) (GetFullPianoWidth() - GetVisiblePianoWidth()));
              break;
        }
        minRange = x;
        maxRange = x + GetVisiblePianoWidth();
        this.scrollTo(x, 0);
        this.progress = argProgressPercent;
    }

    //==================================================================================//
    //==================================================================================//
    public void SafeScrollTo(int argScrollToPx) {
        int minScroll = 0;
        int maxScroll = GetFullPianoWidth() - GetVisiblePianoWidth();
        int positionToScrollTo = argScrollToPx;

        if(argScrollToPx < minScroll) {
            positionToScrollTo = minScroll;
        }
        else if(argScrollToPx > maxScroll) {
            positionToScrollTo = maxScroll;
        }

        this.scrollTo(positionToScrollTo, 0);
    }

    //==================================================================================//
    //==================================================================================//
    public void SafeScrollBy(int argScrollByPx) {
        int currentPosition = this.getScrollX();
        int positionToScrollTo = currentPosition + argScrollByPx;

        SafeScrollTo(positionToScrollTo);
    }

    //==================================================================================//
    //==================================================================================//
    public void SetPianoListener(OnPianoListener pianoListener) {
        this.pianoListener = pianoListener;
    }

    //==================================================================================//
    //==================================================================================//
    public int GetWhiteKeyWidth() {
        if (piano != null) {
            return piano.getWhiteKeyWidth();
        }

        return 0;
    }

    //==================================================================================//
    //==================================================================================//
    @Override protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        postDelayed(() -> ScrollByPercent(progress), 200);
    }
}
