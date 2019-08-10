package com.example.martin.singingapp.MidiProcessing;

import com.pdrogfer.mididroid.MidiFile;
import com.pdrogfer.mididroid.event.*;
import com.pdrogfer.mididroid.event.meta.*;
import com.pdrogfer.mididroid.util.MidiEventListener;
import com.pdrogfer.mididroid.util.MidiProcessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

//
//  Martin Leipert
//  martin.leipert@fau.de
//
// Handler for the MidiEvents which also functions as an Adapter Pattern
// Adapt the Midi Output to what we need and what IMidiListener requires to be implemented :-)



// This class will print any event it receives to the console
public class MidiEventPrinter implements MidiEventListener
{

    // Class specific
    private String mLabel;

    // MidiDroid specific
    // Class from which we receive our events
    private MidiProcessor midi_processor;

    // Information receiver
    // The data merger which fuses data from the processed pitch and the Midi File
    private IMidiListener information_receiver;

    // List of currently playing Midi Notes
    private ArrayList<MyMidiNote> note_list;

    // Constructor receiving the object which desires to get the information and a label
    public MidiEventPrinter(IMidiListener information_receiver, String label)
    {
        // Create Note List
        this.note_list = new ArrayList<>();

        // Set obtained values
        this.information_receiver = information_receiver;
        this.mLabel = label;
    }

    // Triggered when Midi Processing starts
    // Empty in our case
    @Override
    public void onStart(boolean fromBeginning) { }


    // Triggered in case that Midi processing is finished
    @Override
    public void onStop(boolean finished)
    {
        System.out.println("onStop called");
        this.information_receiver.receiveMidiStop();
    }

    // Handle the events obtained
    @Override
    public void onEvent(MidiEvent event, long ms)
    {
        // Event: Starts playing a note
        if (event instanceof NoteOn) {
            NoteOn note_on_event = (NoteOn) event;

            // Get the relevant information from the event
            int note_value = note_on_event.getNoteValue();
            int note_velocity = note_on_event.getVelocity();
            long start_ms = System.currentTimeMillis();

            // Create a MidiNote class which serves as storage for our required information
            MyMidiNote note = new MyMidiNote(note_value, start_ms, note_velocity);

            // Use iterator to add to list for thread safety
            ListIterator<MyMidiNote> note_iterator = this.note_list.listIterator();
            note_iterator.add(note);

            // Send to the Observer
            information_receiver.receiveNoteStart(note);

        // Event triggered if a note stops playing
        } else if (event instanceof NoteOff) {

            NoteOff note_off_event = (NoteOff) event;

            // Get the value to find the note in the currenty played notes
            int note_value = note_off_event.getNoteValue();

            // Use iterator in note list to ensure thread safety
            ListIterator<MyMidiNote> note_iterator = this.note_list.listIterator();

            while (note_iterator.hasNext()) {

                // Search the correct note and remove it from the list
                MyMidiNote tmp_note = note_iterator.next();

                if ((tmp_note.getNote_value() == note_value) && tmp_note.getStart_tick() < System.currentTimeMillis() - 10) {
                    note_iterator.remove();

                    information_receiver.receiveNoteStop(tmp_note);
                }
            }
        } else if (event instanceof  Lyrics) {
            Lyrics text_event = (Lyrics) event;

            // Send the exact Lyrics in an object to connect them with time point
            MidiLyrics lyrics = new MidiLyrics(System.currentTimeMillis(), text_event.getLyric());
            information_receiver.receiveLyrics(lyrics);
        }
    }

    // Starter Method
    // Process the Midi file
    // filepath -> Path to the midi file to process
    public void startProcessingMidi(String filepath) {
        MidiFile midi;
        try {
            File input = new File(filepath);
            midi = new MidiFile(input);
        }
        catch (IOException io_ex) {
            System.out.print("IO Exception: " + io_ex.toString());

            return;
        }

        // Create a new MidiProcessor:
        midi_processor = new MidiProcessor(midi);

        // Register to the beginning of a note
        midi_processor.registerEventListener(this, NoteOn.class);

        // Register to the end of a Note played
        midi_processor.registerEventListener(this, NoteOff.class);

        // Register to Lyrics
        midi_processor.registerEventListener(this, Lyrics.class);

        // Start the processor:
        midi_processor.start();
    }
}