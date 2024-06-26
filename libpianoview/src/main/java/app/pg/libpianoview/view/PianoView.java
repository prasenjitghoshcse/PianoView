package app.pg.libpianoview.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import app.pg.libpianoview.R;
import app.pg.libpianoview.entity.HighlightedKeyInfo;
import app.pg.libpianoview.entity.Piano;
import app.pg.libpianoview.entity.PianoKey;
import app.pg.libpianoview.listener.OnPianoListener;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;


//================================================================================================//
//================================================================================================//
public class PianoView extends View {
    // Constants
    private final static String TAG = "PianoView";
    private final static int kWhiteKeyWidthDpMin = 50;
    private final static int kWhiteKeyWidthDpDefault = 80;
    private final static float kBlack2WhiteKeyHeightRatio = 0.6f;
    private final static int kHighlight1ColourIndex = 0;
    private final static int kHighlight2ColourIndex = 6;
    private final static int kHighlight3ColourIndex = 1;
    private final static int mParamKeyNameTextSizeDp = 20;
    private final static int mParamHighlightNameTextSizeDp = 20;
    private final static int mParamHighlightRectPaddingLeftRightDp = 6;
    private final static int mParamHighlightRectPaddingTopDp = 4;
    private final static int mParamHighlightRectPaddingBotDp = 4;

    // Variables
    private Piano mPiano = null;
    private final CopyOnWriteArrayList<PianoKey> mPressedKeys = new CopyOnWriteArrayList<>();
    private final Paint mPaint;
    private final RectF mRectF;
    private final Rect mTmpTextBoundRect = new Rect(0,0,0,0);
    private String[] mPianoOctaveColors = {
            "#AFDFB1",
            "#FBB3B3",
            "#A2DCD7",
            "#D1C3EC",
            "#FFCC80",
            "#E5EF82",
            "#98DFFF",
            "#AFDFB1",
            "#FBB3B3"
    };
    private final Context   mContext;
    private int             mLayoutWidth  = 0;
    private int             mLayoutHeight = 0;
    private OnPianoListener mPianoListener;
    private boolean         mKeyPressEnabled = true;
    private boolean         mShowNoteNamesEnabled = true;
    private boolean         mOctaveColoringEnabled = true;
    private boolean         mSelectedKeysHighlight1Enabled = true;
    private boolean         mSelectedKeysHighlight2Enabled = false; // TODO:
    private boolean         mSelectedKeysHighlight3Enabled = true;
    private boolean         mIsInitFinish = false;

    //==================================================================================//
    //==================================================================================//
    public PianoView(Context argContext) {
        this(argContext, null);
    }

    //==================================================================================//
    public PianoView(Context argContext, AttributeSet argAttrs) {
        this(argContext, argAttrs, 0);
    }

    //==================================================================================//
    public PianoView(Context argContext, AttributeSet argAttrs, int argDefStyleAttr) {
        super(argContext, argAttrs, argDefStyleAttr);

        mContext = argContext;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mRectF = new RectF();
    }

    //==================================================================================//
    //==================================================================================//
    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, "onMeasure");
        Drawable whiteKeyDrawable = ContextCompat.getDrawable(mContext, R.drawable.white_piano_key);

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

        mLayoutWidth  = width - getPaddingLeft() - getPaddingRight();
        mLayoutHeight = height - getPaddingTop() - getPaddingBottom();

        // Refresh view on height change
        if (mPiano != null) {
            mPiano.setLayoutDimension(mLayoutWidth, mLayoutHeight);
        }

        setMeasuredDimension(width, height);
    }

    //==================================================================================//
    //==================================================================================//
    @Override protected void onDraw(@NonNull Canvas canvas) {
        // Ensure initialized Piano object
        if (mPiano == null) {
            int whiteKeyWidth = dpToPx(kWhiteKeyWidthDpDefault);
            mPiano = new Piano(mContext, mLayoutWidth, mLayoutHeight,
                    whiteKeyWidth, kBlack2WhiteKeyHeightRatio);
        }

        ArrayList<PianoKey[]> whitePianoKeys = mPiano.getWhitePianoKeys();
        ArrayList<PianoKey[]> blackPianoKeys = mPiano.getBlackPianoKeys();

        // Draw all the white keys
        if (whitePianoKeys != null) {
            for (int i = 0; i < whitePianoKeys.size(); i++) {
                for (PianoKey key : whitePianoKeys.get(i)) {
                    // Draw the piano key
                    key.getKeyDrawable().draw(canvas);

                    // Calculating the bound rectangle for key name and bound octave colour rect
                    Rect keyRect = key.getKeyDrawable().getBounds();
                    int unusedWidth = (keyRect.right - keyRect.left) / 2;
                    int keyHeight   = keyRect.bottom - keyRect.top;

                    // Calculating margins, paddings, text size, octave colour rect dynamically. TODO: consider some default max/min values
                    // Common margins
                    int keyMarginLeft            = unusedWidth/2;
                    int keyMarginRight           = unusedWidth/2;
                    int keyMarginBottom          = (int)(keyHeight * 0.04f); // Bottom Margin is a function of key height, not- of key width
                    // Octave colour rectangle related
                    int octaveColourRectHeight   = (int)(keyHeight * 0.02f);
                    int octaveColourRectLeft     = keyRect.left + keyMarginLeft;
                    int octaveColourRectRight    = keyRect.right - keyMarginRight;
                    int octaveColourRectBottom   = keyRect.bottom - keyMarginBottom;
                    int octaveColourRectTop      = keyRect.bottom - keyMarginBottom - octaveColourRectHeight;
                    // Key name text related
                    int keyNameTxtSize           = dpToPx(mParamKeyNameTextSizeDp);
                    int keyNameRectPaddingTop    = keyNameTxtSize/5;
                    int keyNameRectPaddingBottom = keyNameTxtSize/4;
                    int keyNameRectLeft          = keyRect.left + keyMarginLeft;
                    int keyNameRectRight         = keyRect.right - keyMarginRight;
                    int keyNameRectBottom        = keyRect.bottom - keyMarginBottom - octaveColourRectHeight;
                    int keyNameRectTop           = keyNameRectBottom - keyNameRectPaddingBottom - keyNameTxtSize - keyNameRectPaddingTop;
                    int keyNameRectHeight        = keyNameRectBottom - keyNameRectTop;
                    // Highlighted key name related
                    int highlightedNameTxtSize           = dpToPx(mParamHighlightNameTextSizeDp);
                    int highlightedNameRectPaddingTop    = dpToPx(mParamHighlightRectPaddingTopDp);
                    int highlightedNameRectPaddingBottom = dpToPx(mParamHighlightRectPaddingBotDp);
                    int highlightedNameRectLeft          = keyRect.left + keyMarginLeft; // Final value will be calculated based on text width
                    int highlightedNameRectRight         = keyRect.right - keyMarginRight; // Final value will be calculated based on text width
                    int highlightedNameRectBottom        = keyRect.bottom - keyMarginBottom - octaveColourRectHeight - keyNameRectHeight;
                    int highlightedNameRectTop           = highlightedNameRectBottom - highlightedNameRectPaddingBottom - highlightedNameTxtSize - highlightedNameRectPaddingTop;

                    // Draw the octave colouring bottom rectangle
                    if(mOctaveColoringEnabled) {
                        mRectF.set(octaveColourRectLeft, octaveColourRectTop, octaveColourRectRight, octaveColourRectBottom);
                        mPaint.setColor(Color.parseColor(mPianoOctaveColors[i]));
                        canvas.drawRoundRect(mRectF, 12f, 12f, mPaint);
                    }

                    // Draw the key names (e.g. C0, A4 etc.)
                    if(mShowNoteNamesEnabled) {
                        // The key name bound rectangle
                        mRectF.set(keyNameRectLeft, keyNameRectTop, keyNameRectRight, keyNameRectBottom);
                        mPaint.setColor(Color.GRAY);
                        mPaint.setTextSize(keyNameTxtSize);
                        Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
                        int baseline =
                                (int) ((mRectF.bottom + mRectF.top - fontMetrics.bottom - fontMetrics.top) / 2);
                        mPaint.setTextAlign(Paint.Align.CENTER);
                        canvas.drawText(key.getLetterName(), mRectF.centerX(), baseline, mPaint);
                    }

                    // Get the topmost highlight text
                    String topmostHighlightedKeyNoteName = GetTopmostHighlightedKeyNoteName(key);

                    // Draw the selected keys1 highlight
                    if(mSelectedKeysHighlight1Enabled && (!"".equals(key.getHighlightedNoteName1())))
                    {
                        // The highlighted key name bound rectangle with minimum width
                        mRectF.set(highlightedNameRectLeft, highlightedNameRectTop,
                                highlightedNameRectRight, highlightedNameRectBottom);

                        // No need to draw text for Highlight1 if either Highlight2 or Highlight3 is active
                        boolean drawHighlightedNoteName1 = true;
                        if((mSelectedKeysHighlight2Enabled && (!"".equals(key.getHighlightedNoteName2())))
                                || (mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3())))) {
                            drawHighlightedNoteName1 = false;
                        }

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                topmostHighlightedKeyNoteName, highlightedNameTxtSize, keyRect, mRectF);

                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight1ColourIndex]),
                                mRectF,
                                topmostHighlightedKeyNoteName,
                                highlightedNameTxtSize,
                                drawHighlightedNoteName1);
                    }

                    int driftPx = dpToPx(6);

                    // Draw the selected keys2 highlight
                    if(mSelectedKeysHighlight2Enabled && (!"".equals(key.getHighlightedNoteName2())))
                    {
                        // The highlighted key name bound rectangle
                        mRectF.set(highlightedNameRectLeft + driftPx,
                                highlightedNameRectTop - (2 * driftPx),
                                highlightedNameRectRight + driftPx,
                                highlightedNameRectBottom - (2 * driftPx));

                        driftPx += driftPx;

                        // No need to draw text for Highlight2 if Highlight3 is active
                        boolean drawHighlightedNoteName2 = true;
                        if(mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3()))) {
                            drawHighlightedNoteName2 = false;
                        }

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                topmostHighlightedKeyNoteName, highlightedNameTxtSize, keyRect, mRectF);

                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight2ColourIndex]),
                                mRectF,
                                topmostHighlightedKeyNoteName,
                                highlightedNameTxtSize,
                                drawHighlightedNoteName2);
                    }

                    // Draw the selected keys3 highlight
                    if(mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3())))
                    {
                        // The highlighted key name bound rectangle
                        mRectF.set(highlightedNameRectLeft + driftPx,
                                highlightedNameRectTop - (2 * driftPx),
                                highlightedNameRectRight + driftPx,
                                highlightedNameRectBottom - (2 * driftPx));

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                key.getHighlightedNoteName3(), highlightedNameTxtSize, keyRect, mRectF);

                        // Highlight3 is always drawn
                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight3ColourIndex]),
                                mRectF,
                                key.getHighlightedNoteName3(),
                                highlightedNameTxtSize,
                                true);
                    }
                }
            }
        }

        // Draw all the black keys
        if (blackPianoKeys != null) {
            for (int i = 0; i < blackPianoKeys.size(); i++) {
                for (PianoKey key : blackPianoKeys.get(i)) {
                    // Draw the piano key
                    key.getKeyDrawable().draw(canvas);

                    // Calculating the positional measurements
                    Rect keyRect    = key.getKeyDrawable().getBounds();
                    int unusedWidth = (keyRect.right - keyRect.left) / 2;
                    int keyHeight   = keyRect.bottom - keyRect.top;
                    // Calculating margins, paddings, text size dynamically. TODO: consider some default max/min values
                    // Common margins
                    int marginLeft     = unusedWidth/2;
                    int marginRight    = unusedWidth/2;
                    int marginBottom   = (int)(keyHeight * 0.14f); // Bottom Margin is a function of key height, not- of key width
                    // Key name text relayed
                    int keyNameTxtSize            = dpToPx(mParamKeyNameTextSizeDp);
                    int keyNameRectPaddingTop     = unusedWidth/5;
                    int keyNameRectPaddingBottom  = unusedWidth/5;
                    int keyNameRectLeft           = keyRect.left + marginLeft;
                    int keyNameRectRight          = keyRect.right - marginRight;
                    int keyNameRectBottom         = keyRect.bottom - marginBottom;
                    int keyNameRectTop            = keyRect.bottom - marginBottom - keyNameRectPaddingBottom - (2 * keyNameTxtSize) - keyNameRectPaddingTop;
                    int keyNameRectHeight         = keyNameRectBottom - keyNameRectTop;
                    // Highlighted key name 1 related
                    int highlightedNameTxtSize           = dpToPx(mParamHighlightNameTextSizeDp);
                    int highlightedNameRectPaddingTop    = dpToPx(mParamHighlightRectPaddingTopDp);
                    int highlightedNameRectPaddingBottom = dpToPx(mParamHighlightRectPaddingBotDp);
                    int highlightedNameRectLeft          = keyRect.left + marginLeft; // Final value will be calculated based on text width
                    int highlightedNameRectRight         = keyRect.right - marginRight; // Final value will be calculated based on text width
                    int highlightedNameRectBottom        = keyRect.bottom - marginBottom - keyNameRectHeight;
                    int highlightedNameRectTop           = highlightedNameRectBottom - highlightedNameRectPaddingBottom - highlightedNameTxtSize - highlightedNameRectPaddingTop;

                    // Draw the key names (e.g. C♯, A♭ etc.)
                    if(mShowNoteNamesEnabled) {
                        // The key name bound rectangle
                        mRectF.set(keyNameRectLeft, keyNameRectTop, keyNameRectRight, keyNameRectBottom);
                        mPaint.setColor(Color.LTGRAY);
                        mPaint.setTextSize(keyNameTxtSize);
                        mPaint.setTextAlign(Paint.Align.CENTER);

                        String keyName = key.getLetterName();
                        if (keyName.contains("\n")) {
                            // Multi-line text
                            String[] texts = keyName.split("\n");
                            float y = mRectF.top;
                            for (String txt : texts) {
                                canvas.drawText(
                                        txt,
                                        mRectF.centerX(),
                                        y + keyNameRectPaddingTop + keyNameTxtSize,
                                        mPaint);
                                y += mPaint.getTextSize();
                            }
                        } else {
                            // Single line text
                            canvas.drawText(
                                    keyName,
                                    mRectF.centerX(),
                                    mRectF.top + keyNameRectPaddingTop + keyNameTxtSize,
                                    mPaint);
                        }
                    }

                    // Get the topmost highlight text
                    String topmostHighlightedKeyNoteName = GetTopmostHighlightedKeyNoteName(key);

                    // Draw the selected keys1 highlight
                    if(mSelectedKeysHighlight1Enabled && (!"".equals(key.getHighlightedNoteName1())))
                    {
                        // The highlighted key name bound rectangle with min width
                        mRectF.set(highlightedNameRectLeft,
                                highlightedNameRectTop,
                                highlightedNameRectRight,
                                highlightedNameRectBottom);

                        // No need to draw text for Highlight1 if either Highlight2 or Highlight3 is active
                        boolean drawHighlightedNoteName1 = true;
                        if((mSelectedKeysHighlight2Enabled && (!"".equals(key.getHighlightedNoteName2())))
                                || (mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3())))) {
                            drawHighlightedNoteName1 = false;
                        }

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                topmostHighlightedKeyNoteName, highlightedNameTxtSize, keyRect, mRectF);

                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight1ColourIndex]),
                                mRectF,
                                topmostHighlightedKeyNoteName,
                                highlightedNameTxtSize,
                                drawHighlightedNoteName1);
                    }

                    int driftPx = dpToPx(6);

                    // Draw the selected keys2 highlight
                    if(mSelectedKeysHighlight2Enabled && (!"".equals(key.getHighlightedNoteName2())))
                    {
                        // The highlighted key name bound rectangle
                        mRectF.set(highlightedNameRectLeft + driftPx,
                                highlightedNameRectTop - (2 * driftPx),
                                highlightedNameRectRight + driftPx,
                                highlightedNameRectBottom - (2 * driftPx));

                        driftPx += driftPx;

                        // No need to draw text for Highlight2 if Highlight3 is active
                        boolean drawHighlightedNoteName2 = true;
                        if(mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3()))) {
                            drawHighlightedNoteName2 = false;
                        }

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                topmostHighlightedKeyNoteName, highlightedNameTxtSize, keyRect, mRectF);

                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight2ColourIndex]),
                                mRectF,
                                topmostHighlightedKeyNoteName,
                                highlightedNameTxtSize,
                                drawHighlightedNoteName2);
                    }

                    // Draw the selected keys3 highlight
                    if(mSelectedKeysHighlight3Enabled && (!"".equals(key.getHighlightedNoteName3())))
                    {
                        // The highlighted key name bound rectangle
                        mRectF.set(highlightedNameRectLeft + driftPx,
                                highlightedNameRectTop - (2 * driftPx),
                                highlightedNameRectRight + driftPx,
                                highlightedNameRectBottom - (2 * driftPx));

                        RecalculateHighlightRectWidthBasedOnInsideDisplayText(
                                key.getHighlightedNoteName3(), highlightedNameTxtSize, keyRect, mRectF);

                        // Highlight3 is always drawn
                        DrawKeyHighlightedRect(
                                canvas,
                                mPaint,
                                Color.parseColor(mPianoOctaveColors[kHighlight3ColourIndex]),
                                mRectF,
                                key.getHighlightedNoteName3(),
                                highlightedNameTxtSize,
                                true);
                    }
                }
            }
        }

        // Handle draw finish
        if (!mIsInitFinish && mPiano != null && mPianoListener != null) {
            mIsInitFinish = true;

            mPianoListener.onPianoInitFinish();
        }
    }

    //==================================================================================//
    // Determine the topmost highlight text
    //==================================================================================//
    private String GetTopmostHighlightedKeyNoteName(PianoKey argKey) {
        String topmostHighlightedKeyNoteName = "";

        if(mSelectedKeysHighlight3Enabled && (!"".equals(argKey.getHighlightedNoteName3()))) {
            topmostHighlightedKeyNoteName = argKey.getHighlightedNoteName3();
        }
        else if(mSelectedKeysHighlight2Enabled && (!"".equals(argKey.getHighlightedNoteName2()))) {
            topmostHighlightedKeyNoteName = argKey.getHighlightedNoteName2();
        }
        else if(mSelectedKeysHighlight1Enabled && (!"".equals(argKey.getHighlightedNoteName1()))) {
            topmostHighlightedKeyNoteName = argKey.getHighlightedNoteName1();
        }

        return topmostHighlightedKeyNoteName;
    }

    //==================================================================================//
    // Recalculate highlight rect based on inside display text width
    //==================================================================================//
    private void RecalculateHighlightRectWidthBasedOnInsideDisplayText(final String argText, int argTextSize, final Rect argKeyRect, RectF argHighlightRect) {
        ApplyTextStyleToPaint(argTextSize, mPaint);
        mPaint.getTextBounds(argText, 0, argText.length(), mTmpTextBoundRect);

        int textWidth = mTmpTextBoundRect.width();
        int maxAllowedWidth = argKeyRect.width() - (2 * dpToPx(4)); // 2 * left and right paddings
        int currentWidth = (int)argHighlightRect.width();
        int requiredWidth = textWidth + (2 * dpToPx(mParamHighlightRectPaddingLeftRightDp));

        // Determine optimum width
        {
            // Maintain min width
            if (requiredWidth < currentWidth) {
                requiredWidth = currentWidth;
            }
            // Restrict to max width
            else if (requiredWidth > maxAllowedWidth) {
                requiredWidth = maxAllowedWidth;
            }
        }

        // Resize based on optimum width
        mRectF.set(
                argKeyRect.left + ((argKeyRect.width() - requiredWidth)/2f),
                mRectF.top,
                argKeyRect.right - ((argKeyRect.width() - requiredWidth)/2f),
                mRectF.bottom);
    }

    //==================================================================================//
    //==================================================================================//
    private void DrawKeyHighlightedRect(
            @NonNull Canvas argCanvas,
            @NonNull Paint argPaint,
            int argRectColour,
            @NonNull RectF argHighlightedRect,
            @NonNull String argText,
            int argTextSizePx,
            boolean argDrawText) {

        // the background rectangle
        {
            int cornerRadius = (int)(argHighlightedRect.height()/4);
            argPaint.reset();
            argPaint.setColor(argRectColour);
            argCanvas.drawRoundRect(argHighlightedRect, cornerRadius, cornerRadius, argPaint);
        }

        // Draw the key name (always placed vertically at the center of the highlight rect)
        if(argDrawText)
        {
            ApplyTextStyleToPaint(argTextSizePx, argPaint);

            Paint.FontMetricsInt fontMetrics = argPaint.getFontMetricsInt();

            int yCenterBaseline = // Y baseline is calculated considering text is vertically centered
                    (int) (((argHighlightedRect.bottom + argHighlightedRect.top) - (fontMetrics.bottom + fontMetrics.top)) / 2);

            // Adjust baseline based on top/bottom padding
            {
                int paddingTop = dpToPx(mParamHighlightRectPaddingTopDp);
                int paddingBot = dpToPx(mParamHighlightRectPaddingBotDp);

                yCenterBaseline = yCenterBaseline + (paddingTop - paddingBot);
            }

            argCanvas.drawText(argText, argHighlightedRect.centerX(), yCenterBaseline, argPaint);
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void ApplyTextStyleToPaint(int argTextSizePx, @NonNull Paint argPaint) {
        argPaint.reset();
        argPaint.setAntiAlias(true);
        argPaint.setTypeface(Typeface.SANS_SERIF);
        argPaint.setLetterSpacing(0.00F);
        argPaint.setTextAlign(Paint.Align.CENTER);
        argPaint.setTextSize(argTextSizePx);
        argPaint.setColor(Color.BLACK);
    }

    //==================================================================================//
    //==================================================================================//
    @Override public boolean onTouchEvent(MotionEvent event) {
        if (!mKeyPressEnabled) {
            return false;
        }

        int action = event.getActionMasked();

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

        if(null != mPiano) {
            ArrayList<PianoKey[]> whitePianoKeys = mPiano.getWhitePianoKeys();
            ArrayList<PianoKey[]> blackPianoKeys = mPiano.getBlackPianoKeys();

            if (blackPianoKeys != null) {
                for (int i = 0; i < blackPianoKeys.size(); i++) {
                    for (PianoKey key : blackPianoKeys.get(i)) {
                        if (!key.isPressed() && key.contains(x, y)) {
                            HandleBlackKeyDown(which, event, key);

                            // No more keys are needed to be checked
                            return;
                        }
                    }
                }
            }

            if (whitePianoKeys != null) {
                for (int i = 0; i < whitePianoKeys.size(); i++) {
                    for (PianoKey key : whitePianoKeys.get(i)) {
                        if (!key.isPressed() && key.contains(x, y)) {
                            HandleWhiteKeyDown(which, event, key);

                            // No more keys are needed to be checked
                            return;
                        }
                    }
                }
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleWhiteKeyDown(int which, MotionEvent event, PianoKey key) {
        if (mPianoListener != null) {
            mPianoListener.onPianoKeyPress(key.getType(), key.getVoice(), key.getGroup(),
                    key.getPositionOfGroup(), key.getMidiNoteNumber());
        }

        key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
        key.setPressed(true);

        if (event != null) {
            key.setFingerID(event.getPointerId(which));
        }

        mPressedKeys.add(key);
        invalidate(key.getKeyDrawable().getBounds());
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleBlackKeyDown(int which, MotionEvent event, PianoKey key) {
        if (mPianoListener != null) {
            mPianoListener.onPianoKeyPress(key.getType(), key.getVoice(), key.getGroup(),
                    key.getPositionOfGroup(), key.getMidiNoteNumber());
        }

        key.getKeyDrawable().setState(new int[] { android.R.attr.state_pressed });
        key.setPressed(true);

        if (event != null) {
            key.setFingerID(event.getPointerId(which));
        }

        mPressedKeys.add(key);
        invalidate(key.getKeyDrawable().getBounds());
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleMove(int which, MotionEvent event) {
        int x = (int) event.getX(which) + this.getScrollX();
        int y = (int) event.getY(which);

        for (PianoKey key : mPressedKeys) {
            if (key.getFingerID() == event.getPointerId(which)) {
                if (!key.contains(x, y)) {
                    if (mPianoListener != null) {
                        mPianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                                key.getPositionOfGroup(), key.getMidiNoteNumber());
                    }

                    key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
                    invalidate(key.getKeyDrawable().getBounds());
                    key.setPressed(false);
                    key.resetFingerID();
                    mPressedKeys.remove(key);

                    // No more keys are needed to be checked
                    break;
                }
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandlePointerUp(int pointerId) {
        for (PianoKey key : mPressedKeys) {
            if (key.getFingerID() == pointerId) {
                if (mPianoListener != null) {
                    mPianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                            key.getPositionOfGroup(), key.getMidiNoteNumber());
                }

                key.setPressed(false);
                key.resetFingerID();
                key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
                invalidate(key.getKeyDrawable().getBounds());
                mPressedKeys.remove(key);

                // No more keys are needed to be checked
                break;
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    private void HandleUp() {
        if (mPressedKeys.size() > 0) {
            for (PianoKey key : mPressedKeys) {
                if (mPianoListener != null) {
                    mPianoListener.onPianoKeyRelease(key.getType(), key.getVoice(), key.getGroup(),
                            key.getPositionOfGroup(), key.getMidiNoteNumber());
                }

                key.getKeyDrawable().setState(new int[] { -android.R.attr.state_pressed });
                key.setPressed(false);
                invalidate(key.getKeyDrawable().getBounds());
            }

            mPressedKeys.clear();
        }
    }

    //==================================================================================//
    //==================================================================================//
    public int GetFullPianoWidth() {
        if (mPiano != null) {
            return mPiano.getPianoWith();
        }

        return 0;
    }

    //==================================================================================//
    //==================================================================================//
    public int GetVisiblePianoWidth() {
        return mLayoutWidth;
    }

    //==================================================================================//
    //==================================================================================//
    public void SetPianoOctaveColors(String[] pianoColors) {
        if (pianoColors.length == 9) {
            this.mPianoOctaveColors = pianoColors;
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetKeyPressEnabled(boolean argIsEnabled) {
        this.mKeyPressEnabled = argIsEnabled;
    }

    //==================================================================================//
    //==================================================================================//
    public void SetShowNoteNamesEnabled(boolean argIsEnabled) {
        if(argIsEnabled != this.mShowNoteNamesEnabled) {
            this.mShowNoteNamesEnabled = argIsEnabled;
            invalidate();
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetOctaveColoringEnabled(boolean argIsEnabled) {
        if(argIsEnabled != this.mOctaveColoringEnabled) {
            this.mOctaveColoringEnabled = argIsEnabled;
            invalidate();
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetSelectedKeysHighlight1Enabled(boolean argIsEnabled) {
        if(argIsEnabled != this.mSelectedKeysHighlight1Enabled) {
            this.mSelectedKeysHighlight1Enabled = argIsEnabled;
            invalidate();
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetSelectedKeysHighlight3Enabled(boolean argIsEnabled) {
        if(argIsEnabled != this.mSelectedKeysHighlight3Enabled) {
            this.mSelectedKeysHighlight3Enabled = argIsEnabled;
            invalidate();
        }
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
        this.mPianoListener = pianoListener;
    }

    //==================================================================================//
    //==================================================================================//
    public int GetWhiteKeyWidth() {
        if (mPiano != null) {
            return mPiano.getWhiteKeyWidth();
        }

        return 0;
    }

    //==================================================================================//
    //==================================================================================//
    public void SetWhiteKeyWidth(int argWidthToSetPx) {
        if (mPiano != null) {
            int minWidthPx = dpToPx(kWhiteKeyWidthDpMin);
            int maxWidthPx = mLayoutWidth / 8;
            int currentWidthPx = mPiano.getWhiteKeyWidth();

            // Entertain a changed value only
            if(argWidthToSetPx != currentWidthPx) {
                int widthToSet;

                // Handle increase in size
                if(argWidthToSetPx > currentWidthPx) {
                    widthToSet = Math.min(argWidthToSetPx, maxWidthPx);
                }
                // Handle decrease in size
                else {
                    widthToSet = Math.max(argWidthToSetPx, minWidthPx);
                }

                // Apply width change (if any)
                if(widthToSet != currentWidthPx) {
                    mPiano.setWhiteKeyWidth(widthToSet);
                    mIsInitFinish = false;
                    invalidate();
                }
            }
        }
    }

    //==================================================================================//
    //==================================================================================//
    public void SetHighlightedKeys(
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList1,
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList2,
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList3) {
        if(null != mPiano) {
            // Reset previous highlighted keys (if any)
            mPiano.setHighlightedKeys(null, null, null);
            // Highlight new keys
            mPiano.setHighlightedKeys(
                    argHighlightedKeyInfoList1,
                    argHighlightedKeyInfoList2,
                    argHighlightedKeyInfoList3);

            invalidate();
        }
    }

    //==================================================================================//
    //==================================================================================//
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();

        // TODO:
    }

    //==================================================================================//
    //==================================================================================//
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        // TODO:
    }

    //==================================================================================//
    //==================================================================================//
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
