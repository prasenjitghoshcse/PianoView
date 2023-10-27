package app.pg.libpianoview.entity;

public class HighlightedKeyInfo {
    int    mMidiNoteNum = -1;
    String mTmpDisplayName = "";

    public HighlightedKeyInfo(int argMidiNoteNum, String argTmpDisplayName) {
        mMidiNoteNum = argMidiNoteNum;
        mTmpDisplayName = argTmpDisplayName;
    }
}