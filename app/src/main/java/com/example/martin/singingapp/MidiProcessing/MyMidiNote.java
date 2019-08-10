package com.example.martin.singingapp.MidiProcessing;

public class MyMidiNote {
    // MIDI Velocity (force of tone)
    public int velocity;

    // Start in ms
    public long start_tick;

    // MyMidiNote Value
    public int note_value;

    public MyMidiNote(int note_value, long start_tick, int velocity) {
        this.note_value = note_value;
        this.start_tick = start_tick;
        this.velocity = velocity;
    }

    public int calcDuration(long stop_tick) {
        return (int) (stop_tick - start_tick);
    }

    public int getVelocity() {
        return velocity;
    }

    public long getStart_tick() {
        return start_tick;
    }

    public int getNote_value() {
        return note_value;
    }

    // Using hash function would be faster
    // @TODO implement with hashing
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MyMidiNote)) {
            return false;
        }
        if (((MyMidiNote) obj).getNote_value() != this.note_value) {
            return false;
        }
        if (((MyMidiNote) obj).getStart_tick() != this.start_tick) {
            return false;
        }
        if (((MyMidiNote) obj).getVelocity() != this.velocity) {
            return false;
        }
        return true;
    }
}
