package com.example.martin.singingapp.MidiProcessing;

/*

    Martin Leipert
    martin.leipert@fau.de

    Interface serving the purpose to seperate the MIDI File Processing from the plugin
    implementation.

    Software Principle of Loose Coupling

    Lyrics in the Midi File are events at a single point of time
    whereas Notes are events over a time span having a beginning and an end
 */

public interface IMidiListener {

    // Receive lyrics obtained from a Lyrics event
    void receiveLyrics(MidiLyrics lyrics);

    // Receive the beginning of a note and the note Object
    void receiveNoteStart(MyMidiNote note);

    // Handle the end of a note being played
    void receiveNoteStop(MyMidiNote note);

    // If the Midi Event Printer stops -> Midi ended
    void receiveMidiStop();

}
