package app.pg.libpianoview.entity;


import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


//================================================================================================//
//================================================================================================//
public class PianoKey {
  private Piano.PianoKeyType type;
  private Piano.PianoVoice   voice;
  private int                group;
  private int                positionOfGroup;
  private Drawable           keyDrawable;
  private int                voiceId;
  private boolean            isPressed;
  private Rect[]             areaOfKey;
  private String             letterName;
  private int                fingerID = -1;
  private int                mMidiNoteNum = -1;
  private String             mTmpHighlightedNoteName1 = "";
  private String             mTmpHighlightedNoteName2 = "";
  private String             mTmpHighlightedNoteName3 = "";

  public Piano.PianoKeyType getType() {
    return type;
  }

  public void setType(Piano.PianoKeyType type) {
    this.type = type;
  }

  public Piano.PianoVoice getVoice() {
    return voice;
  }

  public void setVoice(Piano.PianoVoice voice) {
    this.voice = voice;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public int getPositionOfGroup() {
    return positionOfGroup;
  }

  public void setPositionOfGroup(int positionOfGroup) {
    this.positionOfGroup = positionOfGroup;
  }

  public Drawable getKeyDrawable() {
    return keyDrawable;
  }

  public void setKeyDrawable(Drawable keyDrawable) {
    this.keyDrawable = keyDrawable;
  }

  public int getVoiceId() {
    return voiceId;
  }

  public void setVoiceId(int voiceId) {
    this.voiceId = voiceId;
  }

  public boolean isPressed() {
    return isPressed;
  }

  public void setPressed(boolean pressed) {
    isPressed = pressed;
  }

  public Rect[] getAreaOfKey() {
    return areaOfKey;
  }

  public void setAreaOfKey(Rect[] areaOfKey) {
    this.areaOfKey = areaOfKey;
  }

  public String getLetterName() {
    return letterName;
  }

  public void setLetterName(String letterName) {
    this.letterName = letterName;
  }

  public boolean contains(int x, int y) {
    boolean isContain = false;
    Rect[] areas = getAreaOfKey();
    int length = getAreaOfKey().length;
    for (int i = 0; i < length; i++) {
      if (areas[i] != null && areas[i].contains(x, y)) {
        isContain = true;
        break;
      }
    }
    return isContain;
  }

  public void resetFingerID() {
    fingerID = -1;
  }

  public void setFingerID(int fingerIndex) {
    this.fingerID = fingerIndex;
  }

  public int getFingerID() {
    return fingerID;
  }

  public void setMidiNoteNumber(int argMidiNoteNum) {
    mMidiNoteNum = argMidiNoteNum;
  }

  public int getMidiNoteNumber() {
    return mMidiNoteNum;
  }

  public static void sortListAscendingByMidiId(ArrayList<PianoKey> argPianoKeyList) {
    Collections.sort(argPianoKeyList, new Comparator<PianoKey>(){
      public int compare(PianoKey o1, PianoKey o2){
        if(o1.mMidiNoteNum == o2.mMidiNoteNum)
          return 0;
        return o1.mMidiNoteNum < o2.mMidiNoteNum ? -1 : 1;
      }
    });
  }

  public String getHighlightedNoteName1() {
    return mTmpHighlightedNoteName1;
  }

  public void setHighlightedNoteName1(String argHighlightedNoteName) {
    mTmpHighlightedNoteName1 = argHighlightedNoteName;
  }

  public String getHighlightedNoteName2() {
    return mTmpHighlightedNoteName2;
  }

  public void setHighlightedNoteName2(String argHighlightedNoteName) {
    mTmpHighlightedNoteName2 = argHighlightedNoteName;
  }

  public String getHighlightedNoteName3() {
    return mTmpHighlightedNoteName3;
  }

  public void setHighlightedNoteName3(String argHighlightedNoteName) {
    mTmpHighlightedNoteName3 = argHighlightedNoteName;
  }
}
