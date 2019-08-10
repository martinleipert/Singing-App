package com.example.martin.singingapp;

import android.os.Environment;
import android.util.Log;

import com.androidplot.xy.XYSeries;
import com.example.martin.singingapp.Feedback.SingingFeedback;
import com.example.martin.singingapp.MidiProcessing.IMidiListener;
import com.example.martin.singingapp.MidiProcessing.MidiEventPrinter;
import com.example.martin.singingapp.MidiProcessing.MidiLyrics;
import com.example.martin.singingapp.MidiProcessing.MyMidiNote;
import com.example.martin.singingapp.PitchProcessing.IPitchSignalListener;
import com.example.martin.singingapp.PitchProcessing.SignalProcessor;


import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class DataMerger implements IMidiListener, IPitchSignalListener {

    // The delay for the midi processing to start
    private int delay_ticks;

    // The tick duration of the Pitch Processor
    // -> Step of time between two signals
    private final int tick_duration = 46;

    // The main activity to send a message if the Midi processing has finished
    private IFinishedSingingObserver main_activity;

    // Lyrics receiver which gets the Lyrics to sing and already sung (also the Main Activity)
    private LyricsReceiver textReceiver;

    // Processing of the pitch signal
    private SignalProcessor signal_processor;
    // Pitch SIgnal in the Plot
    private SingingSeries pitch_series;

    // Midi Processor
    private MidiEventPrinter midi_handler;
    // Midi SIgnal in the Plot
    private SingingSeries midi_series;

    private ArrayList<MidiLyrics> current_lyrics;


    // Calculate the Feedback
    private SingingFeedback feedback;

    // Storage for the Midi Data
    private ArrayList<Long> x_data_pitch;
    private ArrayList<Double> y_data_pitch;

    // Storage for the Midi data
    private ArrayList<Long> x_data_midi;
    private ArrayList<Double> y_data_midi;

    // Current Midi Notes which are active
    private ArrayList<MyMidiNote> current_midi_notes;

    // Dictionary to story Midi notes according to their starting point
    private Dictionary<Long, MyMidiNote> midi_note_archive; // long, MyMidiNote>


    private boolean midi_ended;
    private boolean stopped;

    private Timer lyrics_timer;
    // endregion Attributes

    // region Constructor

    public DataMerger(IFinishedSingingObserver main_activity) {
        this.main_activity = main_activity;

        this.textReceiver = (LyricsReceiver) main_activity;
        // Set up pitch processing

        this.signal_processor = new SignalProcessor();
        signal_processor.registerCustomEventListener(this);
        this.pitch_series = new SingingSeries("pitch", 200);

        this.feedback = new SingingFeedback();

        this.midi_handler = new MidiEventPrinter(this, "MidiEventPrinter");
        this.midi_series = new SingingSeries("midi", 265);

        this.x_data_pitch = new ArrayList<Long>(0);
        this.y_data_pitch = new ArrayList<Double>(0);

        this.x_data_midi = new ArrayList<Long>(0);
        this.y_data_midi = new ArrayList<Double>(0);

        this.current_lyrics = new ArrayList<MidiLyrics>();

        this.current_midi_notes = new ArrayList<MyMidiNote>();

        // Timer that updates the Lyrics
        this.lyrics_timer = new Timer();
        this.lyrics_timer.scheduleAtFixedRate(new TimerTask() {
                                      @Override
                                      public void run() {
                                          updateLyrics();
                                      }
                                  },
                0,
                100);


        this.delay_ticks = 65;

        // INitialize the truth values for stop handling
        this.midi_ended = false;
        this.stopped = false;
    }

    // endregion

    // region Handle Pitch Signal
    // Handle input of the pitch processor
    @Override
    public void onPitchSignal(float signal, long timer_ms) {
        // Log.d("TIME - PITCH", String.valueOf(timer_ms) + " - " + String.valueOf(signal));

        int delay = delay_ticks * tick_duration;

        // Handle the delay in the signal
        if ((timer_ms - delay) > 0) {
            x_data_pitch.add(timer_ms - delay);
            y_data_pitch.add((double) signal);
        }

        // If the midi signal hasn't ended add the data
        if (!midi_ended) {
            double current_midi;

            // @TODO improve over proof of concept
            if (this.current_midi_notes.size() == 0) {
                current_midi = 0;
            } else {
                current_midi = (double) this.current_midi_notes.get(0).note_value;
            }

            x_data_midi.add(timer_ms);
            y_data_midi.add(current_midi);
        }

        // If the Pitch signal reached the start of the pitch signal evaluate :-)
        if ((timer_ms - delay) > 0) {
            double corresponding_midi = y_data_midi.get(y_data_midi.size() - this.delay_ticks + 2);

            // Compare the tones
            feedback.evaluate(signal, corresponding_midi);

            this.update_series(timer_ms, timer_ms - delay, corresponding_midi, signal);
        }

        // If the Midi has ended and the pitch reached this point then end the processing
        if (this.midi_ended && ((timer_ms - delay) > this.x_data_midi.get(this.x_data_midi.size() - 1))) {
            if (!stopped) {
                stopped = true;
                this.signal_processor.stopDetection();
                main_activity.onFinishedSinging();
            }
        }

    }

    // endregion Handle Pitch Signal

    // region Handle Midi Signal
    @Override
    public void receiveNoteStart(MyMidiNote note) {
        Log.d("MIDI - MyMidiNote - Velocity", String.valueOf(note.note_value) + " - " + String.valueOf(note.getVelocity()) + " ms");

        ListIterator<MyMidiNote> list_iter = current_midi_notes.listIterator();

        list_iter.add(note);
    }

    @Override
    public void receiveNoteStop(MyMidiNote note) {
        Log.d("MIDI - MyMidiNote - Velocity", String.valueOf(note.note_value) + " - " + String.valueOf(note.getVelocity()) + " ms");

        for (ListIterator<MyMidiNote> list_iter = current_midi_notes.listIterator(); list_iter.hasNext(); ) {
            MyMidiNote lnote = list_iter.next();
            if (lnote.equals(note)){
                list_iter.remove();
                midi_note_archive.put(note.getStart_tick(), note);
                break;
            }
        }
    }

    // Add the Lyrics to the currently stored
    @Override
    public void receiveLyrics(MidiLyrics lyrics) {

        ListIterator<MidiLyrics> iter = this.current_lyrics.listIterator(this.current_lyrics.size());
        iter.add(lyrics);


    }

    // Trigeered by timer signal every some miliseconds
    private void updateLyrics() {
        // Get the delay
        long delay = this.delay_ticks*46;

        // Get current timing and position of the singer
        long timing = System.currentTimeMillis();
        long cur_position = timing - delay;

        // Get the cut threshold for sung lyrics
        long timing_threshold = cur_position - delay;

        // Two strings for storage
        String sung_lyrics = "";
        String lyrics_to_sing = "";

        // Iterate over the lyrics to order them by their timing
        ListIterator<MidiLyrics> iter = this.current_lyrics.listIterator();

        // Start the Iteration
        while (iter.hasNext()) {
            MidiLyrics midiLyrics = iter.next();

            // Get the timing of the Lyrics
            long lyr_timing = midiLyrics.getTiming();

            // Check the threshold to remove -> Only in case of line end
            if (lyr_timing < timing_threshold) {
                if (midiLyrics.getText().contains("\n")) {
                    MidiLyrics last_to_remove = midiLyrics;

                    // Iterate over all notes before, these may be removed
                    ListIterator<MidiLyrics> remove_iter = this.current_lyrics.listIterator();
                    while (remove_iter.hasNext()) {
                        MidiLyrics to_remove = remove_iter.next();

                        if (to_remove.equals(last_to_remove)) {
                            iter = this.current_lyrics.listIterator();
                            iter.next();
                            iter.remove();
                            break;
                        }
                        else {
                            remove_iter.remove();
                        }
                    }
                }
            }
            // If its below the threshold for already sung add it there
            else if ((timing_threshold <= lyr_timing) && (lyr_timing < cur_position)) {
                sung_lyrics = sung_lyrics + midiLyrics.getText();
            }
            // Add it to the lyrics to sing
            else if (cur_position < lyr_timing) {
                lyrics_to_sing = lyrics_to_sing + midiLyrics.getText();
            }
        }

        this.textReceiver.receiveLyrics(sung_lyrics, lyrics_to_sing);

    }

    @Override
    public void receiveMidiStop() {
        // this.midi_handler.stopProcessingMidi();
        Log.d("MIDI STOP", "Stopped");

        this.midi_ended = true;
    }
    // endregion Handle Midi Signal

    // region Processing
    public void startProcessing(String path_to_midi) {
        //Go ahead with recording audio now
        // The audio needs to be stored in the downloadsdirectory of the Device!
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // this.midi_handler.startProcessingMidi(downloads.getAbsolutePath() + "/Adeste_Fideles_sheet_music_sample.mid");
        this.midi_handler.startProcessingMidi(
                downloads.getAbsolutePath() + "/Dancing Queen Modifiziert.mid");

        signal_processor.startDetection();


        // this.midi_handler.startProcessingMidi(downloads.getAbsolutePath() + "My_Heart_Will_Go_On.mid");
    }

    // endregion Processing

    // region Output

    public XYSeries getMidiSeries() {
        return this.midi_series;
    }

    public XYSeries getPitchSeries() {
        return this.pitch_series;
    }

    private void update_series(long x_midi, long x_pitch, double midi_note, double pitch_note) {
        this.midi_series.update(x_midi, midi_note);
        this.pitch_series.update(x_pitch, pitch_note);
    }

    // Use this for the main activity to pass the feedback to the Feedback Activity
    public SingingFeedback getFeedback() {
        return this.feedback;
    }

    // endregion Output
}
