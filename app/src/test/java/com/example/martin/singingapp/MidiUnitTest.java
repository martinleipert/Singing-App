package com.example.martin.singingapp;

import com.example.martin.singingapp.MidiProcessing.IMidiListener;
import com.example.martin.singingapp.MidiProcessing.MidiHandling;
import com.example.martin.singingapp.MidiProcessing.MyMidiNote;

import org.junit.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MidiUnitTest {
    @Test
    public void Midi_Read() {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(
                "/data/data/singingapp/Adeste_Fideles_sheet_music_sample.mid");

        MidiHandling test = new MidiHandling(new DummyLogger());

        test.startProcessingMidi(resource.getPath());

        try {
            TimeUnit.SECONDS.sleep(60);
        }
        catch (InterruptedException ex) {

        }
    }

    private class DummyLogger implements IMidiListener
    {
        @Override
        public void receiveText(String text) {
            System.out.println("TEXT: " + text);
        }

        @Override
        public void receiveNoteStart(MyMidiNote note) {
            System.out.println("START NOTE - VELOCITY: " + String.valueOf(note.getNote_value()) + " - " + String.valueOf(note.getVelocity()));
        }

        @Override
        public void receiveNoteStop(MyMidiNote note) {
            System.out.println("STOP NOTE - VELOCITY: " + String.valueOf(note.getNote_value()) + " - " + String.valueOf(note.getVelocity()));
        }

        @Override
        public void receiveMidiStop() {

        }

    }
}

