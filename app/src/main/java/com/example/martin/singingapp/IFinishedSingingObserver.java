package com.example.martin.singingapp;

/*

    Classes which receive a signal if the Midi File has finished

 */

public interface IFinishedSingingObserver {
    // Class for a message in case the MIDI file finished
    void onFinishedSinging();
}
