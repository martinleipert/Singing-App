package com.example.martin.singingapp.PitchProcessing;

/*
    Martin Leipert
    martin.leipert@fau.de

    Interface that handles pitch signals

 */
public interface IPitchSignalListener {

    // Gets the note value (one note step is 1 and the timing of the pitch signal)
    void onPitchSignal(float note_signal, long timer_ms);
}
