package app.pg.libpianoview.entity;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import app.pg.libpianoview.R;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;


//================================================================================================//
// Class used for selecting some notes for highlighting
//================================================================================================//
public class Piano {
    private final static int            BLACK_PIANO_KEY_GROUPS = 8;
    private final static int            WHITE_PIANO_KEY_GROUPS = 9;

    private final ArrayList<PianoKey[]> mBlackPianoKeys = new ArrayList<>(BLACK_PIANO_KEY_GROUPS);
    private final ArrayList<PianoKey[]> mWhitePianoKeys = new ArrayList<>(WHITE_PIANO_KEY_GROUPS);
    private final ArrayList<PianoKey>   mAllPianoKeysSorted = new ArrayList<>(); // Used for O(1) search/access
    private int                         mLayoutWidth  = 0; // TODO: Should be used for setting the minimum white key widths so that no blank space is there in the view when key width becomes smaller
    private int                         mLayoutHeight = 0;
    private int                         mBlackKeyWidth;
    private int                         mBlackKeyHeight;
    private float                       mBlack2WhiteKeyHeightRatio;
    private int                         mWhiteKeyWidth; // White key height is same as layout height
    private int                         mNumOfWhiteKeys = 0;
    private final Context               mContext;
    private final ArrayList<HighlightedKeyInfo> mHighlightedKeyInfoList1 = new ArrayList<>();
    private final ArrayList<HighlightedKeyInfo> mHighlightedKeyInfoList2 = new ArrayList<>();
    private final ArrayList<HighlightedKeyInfo> mHighlightedKeyInfoList3 = new ArrayList<>();

    //==================================================================================//
    //==================================================================================//
    public Piano(
            Context argContext,
            int argLayoutWidth,
            int argLayoutHeight,
            int argWhiteKeyWidth,
            float argBlack2WhiteKeyHeightRatio) {
        this.mContext                   = argContext;
        this.mLayoutWidth               = argLayoutWidth;
        this.mLayoutHeight              = argLayoutHeight;
        this.mBlack2WhiteKeyHeightRatio = argBlack2WhiteKeyHeightRatio;

        setWhiteKeyWidth(argWhiteKeyWidth);

        // InitPiano() is called from inside setWhiteKeyWidth()
    }

    //==================================================================================//
    //==================================================================================//
    private void InitPiano() {
        if ((mLayoutWidth > 0) && (mLayoutHeight > 0)) {
            mBlackKeyHeight = (int) ((float) mLayoutHeight * mBlack2WhiteKeyHeightRatio);

            // Setup black piano keys
            mBlackPianoKeys.clear();
            mAllPianoKeysSorted.clear();

            for (int keyGroup = 0; keyGroup < BLACK_PIANO_KEY_GROUPS; keyGroup++) {
                PianoKey[] blackKeys;

                switch (keyGroup) {
                    case 0:
                        blackKeys = new PianoKey[1];
                        break;

                    default:
                        blackKeys = new PianoKey[5];
                        break;
                }

                for (int keyIndexInGroup = 0; keyIndexInGroup < blackKeys.length; keyIndexInGroup++) {
                    blackKeys[keyIndexInGroup] = new PianoKey();
                    mAllPianoKeysSorted.add(blackKeys[keyIndexInGroup]);

                    Rect[] areaOfKey = new Rect[1];
                    blackKeys[keyIndexInGroup].setType(PianoKeyType.BLACK);
                    blackKeys[keyIndexInGroup].setGroup(keyGroup);
                    blackKeys[keyIndexInGroup].setPositionOfGroup(keyIndexInGroup);
//                    blackKeys[keyIndexInGroup].setVoiceId(getVoiceFromResources("b" + keyGroup + keyIndexInGroup));
                    blackKeys[keyIndexInGroup].setPressed(false);
                    blackKeys[keyIndexInGroup].setKeyDrawable(ContextCompat.getDrawable(mContext, R.drawable.black_piano_key));

                    setBlackKeyDrawableBounds(keyGroup, keyIndexInGroup, blackKeys[keyIndexInGroup].getKeyDrawable());
                    areaOfKey[0] = blackKeys[keyIndexInGroup].getKeyDrawable().getBounds();
                    blackKeys[keyIndexInGroup].setAreaOfKey(areaOfKey);

                    // Group 0 has only one black key
                    if (keyGroup == 0) {
                        blackKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
                        blackKeys[keyIndexInGroup].setMidiNoteNumber(22); // A#0/Bb0
                        blackKeys[keyIndexInGroup].setLetterName("A♯\nB♭");
                        break;
                    }

                    // Other groups have 5 black keys each
                    switch (keyIndexInGroup) {
                        case 0:
                            blackKeys[keyIndexInGroup].setVoice(PianoVoice.DO);
                            blackKeys[keyIndexInGroup].setMidiNoteNumber(13 + (keyGroup * 12)); // C#/Db
                            blackKeys[keyIndexInGroup].setLetterName("C♯\nD♭");
                            break;

                        case 1:
                            blackKeys[keyIndexInGroup].setVoice(PianoVoice.RE);
                            blackKeys[keyIndexInGroup].setMidiNoteNumber(15 + (keyGroup * 12)); // D#/Eb
                            blackKeys[keyIndexInGroup].setLetterName("D♯\nE♭");
                            break;

                        case 2:
                            blackKeys[keyIndexInGroup].setVoice(PianoVoice.FA);
                            blackKeys[keyIndexInGroup].setMidiNoteNumber(18 + (keyGroup * 12)); // F#/Gb
                            blackKeys[keyIndexInGroup].setLetterName("F♯\nG♭");
                            break;

                        case 3:
                            blackKeys[keyIndexInGroup].setVoice(PianoVoice.SO);
                            blackKeys[keyIndexInGroup].setMidiNoteNumber(20 + (keyGroup * 12)); // G#/Ab
                            blackKeys[keyIndexInGroup].setLetterName("G♯\nA♭");
                            break;

                        case 4:
                            blackKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
                            blackKeys[keyIndexInGroup].setMidiNoteNumber(22 + (keyGroup * 12)); // A#/Bb
                            blackKeys[keyIndexInGroup].setLetterName("A♯\nB♭");
                            break;
                    }
                }

                mBlackPianoKeys.add(blackKeys);
            }

            // Setup white piano keys
            mWhitePianoKeys.clear();
            mNumOfWhiteKeys = 0;

            for (int keyGroup = 0; keyGroup < WHITE_PIANO_KEY_GROUPS; keyGroup++) {
                PianoKey[] whiteKeys;

                switch (keyGroup) {
                    case 0:
                        whiteKeys = new PianoKey[2];
                        break;

                    case 8:
                        whiteKeys = new PianoKey[1];
                        break;

                    default:
                        whiteKeys = new PianoKey[7];
                        break;
                }

                for (int keyIndexInGroup = 0; keyIndexInGroup < whiteKeys.length; keyIndexInGroup++) {
                    whiteKeys[keyIndexInGroup] = new PianoKey();
                    mAllPianoKeysSorted.add(whiteKeys[keyIndexInGroup]);

                    //固定属性
                    whiteKeys[keyIndexInGroup].setType(PianoKeyType.WHITE);
                    whiteKeys[keyIndexInGroup].setGroup(keyGroup);
                    whiteKeys[keyIndexInGroup].setPositionOfGroup(keyIndexInGroup);
//                    whiteKeys[keyIndexInGroup].setVoiceId(getVoiceFromResources("w" + keyGroup + keyIndexInGroup));
                    whiteKeys[keyIndexInGroup].setPressed(false);
                    whiteKeys[keyIndexInGroup].setKeyDrawable(ContextCompat.getDrawable(mContext, R.drawable.white_piano_key));

                    setWhiteKeyDrawableBounds(keyGroup, keyIndexInGroup, whiteKeys[keyIndexInGroup].getKeyDrawable());
                    mNumOfWhiteKeys++;

                    if (keyGroup == 0) {
                        switch (keyIndexInGroup) {
                            case 0:
                                whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
                                whiteKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
                                whiteKeys[keyIndexInGroup].setLetterName("A0");
                                whiteKeys[keyIndexInGroup].setMidiNoteNumber(21);
                                break;

                            case 1:
                                whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
                                whiteKeys[keyIndexInGroup].setVoice(PianoVoice.SI);
                                whiteKeys[keyIndexInGroup].setLetterName("B0");
                                whiteKeys[keyIndexInGroup].setMidiNoteNumber(23);
                                break;
                        }

                        continue;
                    }

                    if (keyGroup == 8) {
                        Rect[] areaOfKey = new Rect[1];
                        areaOfKey[0] = whiteKeys[keyIndexInGroup].getKeyDrawable().getBounds();
                        whiteKeys[keyIndexInGroup].setAreaOfKey(areaOfKey);
                        whiteKeys[keyIndexInGroup].setVoice(PianoVoice.DO);
                        whiteKeys[keyIndexInGroup].setLetterName("C8");
                        whiteKeys[keyIndexInGroup].setMidiNoteNumber(108);
                        break;
                    }

                    switch (keyIndexInGroup) {
                        case 0:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.DO);
                            whiteKeys[keyIndexInGroup].setLetterName("C" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(12 + (keyGroup * 12)); // C
                            break;

                        case 1:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.CENTER));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.RE);
                            whiteKeys[keyIndexInGroup].setLetterName("D" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(14 + (keyGroup * 12)); // D
                            break;

                        case 2:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.MI);
                            whiteKeys[keyIndexInGroup].setLetterName("E" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(16 + (keyGroup * 12)); // E
                            break;

                        case 3:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.FA);
                            whiteKeys[keyIndexInGroup].setLetterName("F" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(17 + (keyGroup * 12)); // F
                            break;

                        case 4:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.CENTER));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.SO);
                            whiteKeys[keyIndexInGroup].setLetterName("G" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(19 + (keyGroup * 12)); // G
                            break;

                        case 5:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.CENTER));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
                            whiteKeys[keyIndexInGroup].setLetterName("A" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(21 + (keyGroup * 12)); // A
                            break;

                        case 6:
                            whiteKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
                            whiteKeys[keyIndexInGroup].setVoice(PianoVoice.SI);
                            whiteKeys[keyIndexInGroup].setLetterName("B" + keyGroup);
                            whiteKeys[keyIndexInGroup].setMidiNoteNumber(23 + (keyGroup * 12)); // B
                            break;
                    }
                }

                mWhitePianoKeys.add(whiteKeys);
            }

            // Sort all piano keys
            PianoKey.sortListAscendingByMidiId(mAllPianoKeysSorted);

            // Refreshing highlighted key info as new Key objects have been created
            // MUST be called after mAllPianoKeysSorted is sorted
            refreshHighlightedKeys();
        }
    }

    public enum PianoVoice {
        DO, RE, MI, FA, SO, LA, SI
    }

    public enum PianoKeyType {
        @SerializedName("0")
        BLACK(0),
        @SerializedName("1")
        WHITE(1);
        private final int value;

        PianoKeyType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override public String toString() {
            return "PianoKeyType{" + "value=" + value + '}';
        }
    }

    private enum BlackKeyPosition {
        LEFT, CENTER, RIGHT
    }

    //==================================================================================//
    //==================================================================================//
    private Rect[] getWhitePianoKeyArea(int group, int positionOfGroup,
        BlackKeyPosition blackKeyPosition) {
        int offset = 0;

        if (group == 0) {
            offset = 5;
        }

        switch (blackKeyPosition) {
            case LEFT:
                Rect[] left = new Rect[2];
                left[0] =
                    new Rect((7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth, mBlackKeyHeight,
                        (7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth + mBlackKeyWidth / 2,
                            mLayoutHeight);
                left[1] =
                    new Rect((7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth + mBlackKeyWidth / 2,
                        0, (7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth, mLayoutHeight);
                return left;

            case CENTER:
                Rect[] leftRight = new Rect[3];
                leftRight[0] =
                    new Rect((7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth, mBlackKeyHeight,
                        (7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth + mBlackKeyWidth / 2,
                            mLayoutHeight);
                leftRight[1] =
                    new Rect((7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth + mBlackKeyWidth / 2,
                        0, (7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth - mBlackKeyWidth / 2,
                            mLayoutHeight);
                leftRight[2] =
                    new Rect((7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth - mBlackKeyWidth / 2,
                            mBlackKeyHeight, (7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth,
                            mLayoutHeight);
                return leftRight;

            case RIGHT:
                Rect[] right = new Rect[2];
                right[0] = new Rect((7 * group - 5 + offset + positionOfGroup) * mWhiteKeyWidth, 0,
                    (7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth - mBlackKeyWidth / 2,
                        mLayoutHeight);
                right[1] =
                    new Rect((7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth - mBlackKeyWidth / 2,
                            mBlackKeyHeight, (7 * group - 4 + offset + positionOfGroup) * mWhiteKeyWidth,
                            mLayoutHeight);
                return right;
        }

        return null;
    }

    //==================================================================================//
    //==================================================================================//
    private void setWhiteKeyDrawableBounds(int argOctaveGroup, int argPositionInGroup, Drawable argDrawable) {
        int whiteOffset = 0;

        if (argOctaveGroup == 0) {
            whiteOffset = 5;
        }

        int tmpLeft = ((7 * argOctaveGroup) + (whiteOffset - 5) + argPositionInGroup) * mWhiteKeyWidth;

        argDrawable.setBounds(
                tmpLeft,
                0,
                tmpLeft + mWhiteKeyWidth,
                mLayoutHeight);
    }

    //==================================================================================//
    //==================================================================================//
    private void setBlackKeyDrawableBounds(int argOctaveGroup, int argPositionInGroup, Drawable argDrawable) {
        int whiteOffset = 0;
        int blackOffset = 0;

        if (argOctaveGroup == 0) {
            whiteOffset = 5;
        }

        if (argPositionInGroup == 2 || argPositionInGroup == 3 || argPositionInGroup == 4) {
            blackOffset = 1;
        }

        int tmpLeft = ((7 * argOctaveGroup) + (whiteOffset - 4) + (argPositionInGroup + blackOffset)) * mWhiteKeyWidth
                - (mBlackKeyWidth / 2);

        argDrawable.setBounds(
                tmpLeft,
                0,
                tmpLeft + mBlackKeyWidth,
                mBlackKeyHeight);
    }

    //==================================================================================//
    //==================================================================================//
    public ArrayList<PianoKey[]> getWhitePianoKeys() {
        return mWhitePianoKeys;
    }

    //==================================================================================//
    //==================================================================================//
    public ArrayList<PianoKey[]> getBlackPianoKeys() {
        return mBlackPianoKeys;
    }

    //==================================================================================//
    //==================================================================================//
    public int getPianoWith() {
        return mNumOfWhiteKeys * mWhiteKeyWidth;
    }

    //==================================================================================//
    //==================================================================================//
    public int getWhiteKeyWidth() {
        return mWhiteKeyWidth;
    }

    public void setWhiteKeyWidth(int argWhiteKeyWidth) {
        this.mWhiteKeyWidth = argWhiteKeyWidth;
        // NOTE: 0.5829f according to https://en.wikipedia.org/wiki/Musical_keyboard
        // But we are taking a larger value for better touch accuracy
        this.mBlackKeyWidth = Math.round(this.mWhiteKeyWidth * 0.75f);

        InitPiano();
    }

    //==================================================================================//
    //==================================================================================//
    public void setLayoutDimension(int argLayoutWidth, int argLayoutHeight) {
        this.mLayoutWidth = argLayoutWidth;
        this.mLayoutHeight = argLayoutHeight;

        InitPiano();
    }

    //==================================================================================//
    //==================================================================================//
    public void setHighlightedKeys(
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList1,
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList2,
            ArrayList<HighlightedKeyInfo> argHighlightedKeyInfoList3) {
        mHighlightedKeyInfoList1.clear();
        mHighlightedKeyInfoList2.clear();
        mHighlightedKeyInfoList3.clear();

        if(null != argHighlightedKeyInfoList1) {
            mHighlightedKeyInfoList1.addAll(argHighlightedKeyInfoList1);
        }

        if(null != argHighlightedKeyInfoList2) {
            mHighlightedKeyInfoList2.addAll(argHighlightedKeyInfoList2);
        }

        if(null != argHighlightedKeyInfoList3) {
            mHighlightedKeyInfoList3.addAll(argHighlightedKeyInfoList3);
        }

        refreshHighlightedKeys();
    }

    //==================================================================================//
    //==================================================================================//
    private void refreshHighlightedKeys() {
        // Reset previous highlights (if any)
        // Highlight 1
        {
            if (0 == mHighlightedKeyInfoList1.size()) {
                for (PianoKey pianoKey : mAllPianoKeysSorted) {
                    pianoKey.setHighlightedNoteName1("");
                }
            }
            // Set new highlights
            else {
                for (HighlightedKeyInfo highlightedKeyInfo : mHighlightedKeyInfoList1) {
                    int pianoKeyZIndex = highlightedKeyInfo.mMidiNoteNum - 21; // 21 is the MIDI note number for piano key A0 (the first note in the list)

                    if ((pianoKeyZIndex >= 0) && (pianoKeyZIndex < mAllPianoKeysSorted.size())) {
                        PianoKey pianoKey = mAllPianoKeysSorted.get(pianoKeyZIndex);
                        pianoKey.setHighlightedNoteName1(highlightedKeyInfo.mTmpDisplayName);
                    }
                }
            }
        }
        // Highlight 2
        {
            if (0 == mHighlightedKeyInfoList2.size()) {
                for (PianoKey pianoKey : mAllPianoKeysSorted) {
                    pianoKey.setHighlightedNoteName2("");
                }
            }
            // Set new highlights
            else {
                for (HighlightedKeyInfo highlightedKeyInfo : mHighlightedKeyInfoList2) {
                    int pianoKeyZIndex = highlightedKeyInfo.mMidiNoteNum - 21; // 21 is the MIDI note number for piano key A0 (the first note in the list)

                    if ((pianoKeyZIndex >= 0) && (pianoKeyZIndex < mAllPianoKeysSorted.size())) {
                        PianoKey pianoKey = mAllPianoKeysSorted.get(pianoKeyZIndex);
                        pianoKey.setHighlightedNoteName2(highlightedKeyInfo.mTmpDisplayName);
                    }
                }
            }
        }
        // Highlight 3
        {
            if (0 == mHighlightedKeyInfoList3.size()) {
                for (PianoKey pianoKey : mAllPianoKeysSorted) {
                    pianoKey.setHighlightedNoteName3("");
                }
            }
            // Set new highlights
            else {
                for (HighlightedKeyInfo highlightedKeyInfo : mHighlightedKeyInfoList3) {
                    int pianoKeyZIndex = highlightedKeyInfo.mMidiNoteNum - 21; // 21 is the MIDI note number for piano key A0 (the first note in the list)

                    if ((pianoKeyZIndex >= 0) && (pianoKeyZIndex < mAllPianoKeysSorted.size())) {
                        PianoKey pianoKey = mAllPianoKeysSorted.get(pianoKeyZIndex);
                        pianoKey.setHighlightedNoteName3(highlightedKeyInfo.mTmpDisplayName);
                    }
                }
            }
        }
    }
}
