package app.pg.libpianoview.entity;


import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import app.pg.libpianoview.R;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;


//================================================================================================//
//================================================================================================//
public class Piano {
  private final static int            BLACK_PIANO_KEY_GROUPS = 8;
  private final static int            WHITE_PIANO_KEY_GROUPS = 9;
  private final ArrayList<PianoKey[]> blackPianoKeys = new ArrayList<>(BLACK_PIANO_KEY_GROUPS);
  private final ArrayList<PianoKey[]> whitePianoKeys = new ArrayList<>(WHITE_PIANO_KEY_GROUPS);
  private final int                   blackKeyWidth;
  private int                         blackKeyHeight;
  private final int                   whiteKeyWidth;
  private int                         whiteKeyHeight;
  private int                         pianoWith = 0;
  private float                       scaleHeight = 0;
  private final Context               context;

  public Piano(Context context, float argScaleHeight, int argWhiteKeyWidth) {
    this.context = context;
    this.scaleHeight = argScaleHeight;
    this.whiteKeyWidth = argWhiteKeyWidth;
    this.blackKeyWidth = Math.round(this.whiteKeyWidth * 0.77f); // TODO: Hardcoding can be parameterized

    InitPiano();
  }

  private void InitPiano() {
    if (scaleHeight > 0) {
      Drawable blackDrawable = ContextCompat.getDrawable(context, R.drawable.black_piano_key);
      Drawable whiteDrawable = ContextCompat.getDrawable(context, R.drawable.white_piano_key);
      // Widths are already calculated
      blackKeyHeight = (int) ((float) blackDrawable.getIntrinsicHeight() * scaleHeight);
      whiteKeyHeight = (int) ((float) whiteDrawable.getIntrinsicHeight() * scaleHeight);

      // Setup black piano keys
      for (int keyGroup = 0; keyGroup < BLACK_PIANO_KEY_GROUPS; keyGroup++) {
        PianoKey[] keys;
        switch (keyGroup) {
          case 0:
            keys = new PianoKey[1];
            break;
          default:
            keys = new PianoKey[5];
            break;
        }
        for (int keyIndexInGroup = 0; keyIndexInGroup < keys.length; keyIndexInGroup++) {
          keys[keyIndexInGroup] = new PianoKey();
          Rect[] areaOfKey = new Rect[1];
          keys[keyIndexInGroup].setType(PianoKeyType.BLACK);
          keys[keyIndexInGroup].setGroup(keyGroup);
          keys[keyIndexInGroup].setPositionOfGroup(keyIndexInGroup);
          keys[keyIndexInGroup].setVoiceId(getVoiceFromResources("b" + keyGroup + keyIndexInGroup));
          keys[keyIndexInGroup].setPressed(false);
          keys[keyIndexInGroup].setKeyDrawable(
              new ScaleDrawable(ContextCompat.getDrawable(context, R.drawable.black_piano_key),
                  Gravity.NO_GRAVITY, 1, scaleHeight).getDrawable());
          setBlackKeyDrawableBounds(keyGroup, keyIndexInGroup, keys[keyIndexInGroup].getKeyDrawable());
          areaOfKey[0] = keys[keyIndexInGroup].getKeyDrawable().getBounds();
          keys[keyIndexInGroup].setAreaOfKey(areaOfKey);

          // Group 0 has only one black key
          if (keyGroup == 0) {
            keys[keyIndexInGroup].setVoice(PianoVoice.LA);
            keys[keyIndexInGroup].setMidiNoteNumber(22); // A#0/Bb0
            break;
          }

          // Other groups have 5 black keys each
          switch (keyIndexInGroup) {
            case 0:
              keys[keyIndexInGroup].setVoice(PianoVoice.DO);
              keys[keyIndexInGroup].setMidiNoteNumber(13 + (keyGroup * 12)); // C#/Db
              break;
            case 1:
              keys[keyIndexInGroup].setVoice(PianoVoice.RE);
              keys[keyIndexInGroup].setMidiNoteNumber(15 + (keyGroup * 12)); // D#/Eb
              break;
            case 2:
              keys[keyIndexInGroup].setVoice(PianoVoice.FA);
              keys[keyIndexInGroup].setMidiNoteNumber(18 + (keyGroup * 12)); // F#/Gb
              break;
            case 3:
              keys[keyIndexInGroup].setVoice(PianoVoice.SO);
              keys[keyIndexInGroup].setMidiNoteNumber(20 + (keyGroup * 12)); // G#/Ab
              break;
            case 4:
              keys[keyIndexInGroup].setVoice(PianoVoice.LA);
              keys[keyIndexInGroup].setMidiNoteNumber(22 + (keyGroup * 12)); // A#/Bb
              break;
          }
        }
        blackPianoKeys.add(keys);
      }

      // Setup white piano keys
      for (int keyGroup = 0; keyGroup < WHITE_PIANO_KEY_GROUPS; keyGroup++) {
        PianoKey[] mKeys;

        switch (keyGroup) {
          case 0:
            mKeys = new PianoKey[2];
            break;
          case 8:
            mKeys = new PianoKey[1];
            break;
          default:
            mKeys = new PianoKey[7];
            break;
        }

        for (int keyIndexInGroup = 0; keyIndexInGroup < mKeys.length; keyIndexInGroup++) {
          mKeys[keyIndexInGroup] = new PianoKey();
          //固定属性
          mKeys[keyIndexInGroup].setType(PianoKeyType.WHITE);
          mKeys[keyIndexInGroup].setGroup(keyGroup);
          mKeys[keyIndexInGroup].setPositionOfGroup(keyIndexInGroup);
          mKeys[keyIndexInGroup].setVoiceId(getVoiceFromResources("w" + keyGroup + keyIndexInGroup));
          mKeys[keyIndexInGroup].setPressed(false);
          mKeys[keyIndexInGroup].setKeyDrawable(
              new ScaleDrawable(ContextCompat.getDrawable(context, R.drawable.white_piano_key),
                  Gravity.NO_GRAVITY, 1, scaleHeight).getDrawable());
          setWhiteKeyDrawableBounds(keyGroup, keyIndexInGroup, mKeys[keyIndexInGroup].getKeyDrawable());
          pianoWith += whiteKeyWidth;

          if (keyGroup == 0) {
            switch (keyIndexInGroup) {
              case 0:
                mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
                mKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
                mKeys[keyIndexInGroup].setLetterName("A0");
                mKeys[keyIndexInGroup].setMidiNoteNumber(21);
                break;
              case 1:
                mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
                mKeys[keyIndexInGroup].setVoice(PianoVoice.SI);
                mKeys[keyIndexInGroup].setLetterName("B0");
                mKeys[keyIndexInGroup].setMidiNoteNumber(23);
                break;
            }
            continue;
          }

          if (keyGroup == 8) {
            Rect[] areaOfKey = new Rect[1];
            areaOfKey[0] = mKeys[keyIndexInGroup].getKeyDrawable().getBounds();
            mKeys[keyIndexInGroup].setAreaOfKey(areaOfKey);
            mKeys[keyIndexInGroup].setVoice(PianoVoice.DO);
            mKeys[keyIndexInGroup].setLetterName("C8");
            mKeys[keyIndexInGroup].setMidiNoteNumber(108);
            break;
          }

          switch (keyIndexInGroup) {
            case 0:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.DO);
              mKeys[keyIndexInGroup].setLetterName("C" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(12 + (keyGroup * 12)); // C
              break;
            case 1:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT_RIGHT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.RE);
              mKeys[keyIndexInGroup].setLetterName("D" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(14 + (keyGroup * 12)); // D
              break;
            case 2:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.MI);
              mKeys[keyIndexInGroup].setLetterName("E" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(16 + (keyGroup * 12)); // E
              break;
            case 3:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.RIGHT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.FA);
              mKeys[keyIndexInGroup].setLetterName("F" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(17 + (keyGroup * 12)); // F
              break;
            case 4:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT_RIGHT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.SO);
              mKeys[keyIndexInGroup].setLetterName("G" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(19 + (keyGroup * 12)); // G
              break;
            case 5:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT_RIGHT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.LA);
              mKeys[keyIndexInGroup].setLetterName("A" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(21 + (keyGroup * 12)); // A
              break;
            case 6:
              mKeys[keyIndexInGroup].setAreaOfKey(getWhitePianoKeyArea(keyGroup, keyIndexInGroup, BlackKeyPosition.LEFT));
              mKeys[keyIndexInGroup].setVoice(PianoVoice.SI);
              mKeys[keyIndexInGroup].setLetterName("B" + keyGroup);
              mKeys[keyIndexInGroup].setMidiNoteNumber(23 + (keyGroup * 12)); // B
              break;
          }
        }
        whitePianoKeys.add(mKeys);
      }
    }
  }

  public enum PianoVoice {
    DO, RE, MI, FA, SO, LA, SI
  }

  public enum PianoKeyType {
    @SerializedName("0")
    BLACK(0), @SerializedName("1")
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
    LEFT, LEFT_RIGHT, RIGHT
  }

  private int getVoiceFromResources(String voiceName) {
    return context.getResources().getIdentifier(voiceName, "raw", context.getPackageName());
  }

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
            new Rect((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth, blackKeyHeight,
                (7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth + blackKeyWidth / 2,
                whiteKeyHeight);
        left[1] =
            new Rect((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth + blackKeyWidth / 2,
                0, (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth, whiteKeyHeight);
        return left;
      case LEFT_RIGHT:
        Rect[] leftRight = new Rect[3];
        leftRight[0] =
            new Rect((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth, blackKeyHeight,
                (7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth + blackKeyWidth / 2,
                whiteKeyHeight);
        leftRight[1] =
            new Rect((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth + blackKeyWidth / 2,
                0, (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth - blackKeyWidth / 2,
                whiteKeyHeight);
        leftRight[2] =
            new Rect((7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth - blackKeyWidth / 2,
                blackKeyHeight, (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth,
                whiteKeyHeight);
        return leftRight;
      case RIGHT:
        Rect[] right = new Rect[2];
        right[0] = new Rect((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth, 0,
            (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth - blackKeyWidth / 2,
            whiteKeyHeight);
        right[1] =
            new Rect((7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth - blackKeyWidth / 2,
                blackKeyHeight, (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth,
                whiteKeyHeight);
        return right;
    }
    return null;
  }

  private void setWhiteKeyDrawableBounds(int group, int positionOfGroup, Drawable drawable) {
    int offset = 0;
    if (group == 0) {
      offset = 5;
    }
    drawable.setBounds((7 * group - 5 + offset + positionOfGroup) * whiteKeyWidth, 0,
        (7 * group - 4 + offset + positionOfGroup) * whiteKeyWidth, whiteKeyHeight);
  }

  private void setBlackKeyDrawableBounds(int group, int positionOfGroup, Drawable drawable) {
    int whiteOffset = 0;
    int blackOffset = 0;
    if (group == 0) {
      whiteOffset = 5;
    }
    if (positionOfGroup == 2 || positionOfGroup == 3 || positionOfGroup == 4) {
      blackOffset = 1;
    }
    drawable.setBounds((7 * group - 4 + whiteOffset + blackOffset + positionOfGroup) * whiteKeyWidth
            - blackKeyWidth / 2, 0,
        (7 * group - 4 + whiteOffset + blackOffset + positionOfGroup) * whiteKeyWidth
            + blackKeyWidth / 2, blackKeyHeight);
  }

  public ArrayList<PianoKey[]> getWhitePianoKeys() {
    return whitePianoKeys;
  }

  public ArrayList<PianoKey[]> getBlackPianoKeys() {
    return blackPianoKeys;
  }

  public int getPianoWith() {
    return pianoWith;
  }

  public int getWhiteKeyWidth() {
    return whiteKeyWidth;
  }
}
