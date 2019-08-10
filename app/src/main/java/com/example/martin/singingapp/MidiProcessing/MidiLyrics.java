package com.example.martin.singingapp.MidiProcessing;

//
//  Martin Leipert
//  martin.leipert@fau.de
//
// Storage class for Midi Lyrics with Text and Time point
//

public class MidiLyrics {

    // Private variables which can only be accessed via a getter
    // Time point of the Lyrics
    private long timing;
    // Storage for the text
    private String text;

    // Construct and store
    public MidiLyrics(long timing, String text) {
        this.timing = timing;
        this.text = text;
    }

    // Getter for the Timing
    public long getTiming() {
        return this.timing;
    }

    // Getter for the Text
    public String getText() {
        return this.text;
    }

}
