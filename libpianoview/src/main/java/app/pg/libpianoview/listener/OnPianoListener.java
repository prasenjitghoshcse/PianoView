package app.pg.libpianoview.listener;


import app.pg.libpianoview.entity.Piano;


//================================================================================================//
//================================================================================================//
public interface OnPianoListener {
  void onPianoInitFinish();

  void onPianoKeyPress(
          Piano.PianoKeyType type,
          Piano.PianoVoice voice,
          int group,
          int positionOfGroup,
          int argMidiNoteNumber);

  void onPianoKeyRelease(
          Piano.PianoKeyType type,
          Piano.PianoVoice voice,
          int group,
          int positionOfGroup,
          int argMidiNoteNumber);
}
