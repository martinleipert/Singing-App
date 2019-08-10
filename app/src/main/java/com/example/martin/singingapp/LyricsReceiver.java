package com.example.martin.singingapp;

/*

    Martin Leipert
    martin.leipert@fau.de

    Interface serving the purpose to seperate the MIDI File Processing from the plugin
    implementation.

    Software Principle of Loose Coupling

    Interface to pass Lyrics which are to sing and which are yet to come
 */

public interface LyricsReceiver {

    // Receive lyrics obtained from a Lyrics event
    void receiveLyrics(String already_passed, String to_sing);
}
