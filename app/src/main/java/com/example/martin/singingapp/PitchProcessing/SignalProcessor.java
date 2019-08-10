package com.example.martin.singingapp.PitchProcessing;

import java.util.*;

import be.tarsos.dsp.*;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.*;

//
//  Martin Leipert
//  martin.leipert@fau.de
//
// Signal Processor
// -> Processes the Pitch
// -> Acts as an Adapter towards the IPitchSignalListener Interface
//    May be observed by the OnPitchSignalListeners

public class SignalProcessor {

    private ArrayList<IPitchSignalListener> mListener;

    // Store the detected Pitch
    private ArrayList<Float> pitch_list;
    private ArrayList<Double> note_list;
    private ArrayList<Long> milisecond_list;
    // Store the audio thread to stop it via method
    private Thread audio_thread;

    private long start_ms;

    // Reference note A4 with its frequency 440 Hz
    final int reference = 440;


    // Constructor to initialize the List
    public SignalProcessor() {
        pitch_list = new ArrayList<Float>(0);
        note_list = new ArrayList<Double>(0);
        milisecond_list = new ArrayList<Long>(0);
        mListener = new ArrayList<IPitchSignalListener>(0);
    }

    // Accessor Method for the Pitch List
    public ArrayList<Float> getPitch_list() {
        return pitch_list;
    }

    // Adds an event Listiner to the Pitch
    public void registerCustomEventListener(IPitchSignalListener event_listener) {
        mListener.add(event_listener);
    }

    // Remove this event listener again
    public void unregisterCustomEventListener(IPitchSignalListener event_listener) {
        mListener.remove(event_listener);
    }


    // Start the Pitch detection
    public void startDetection() {

        // Audio Input -> The Microphone
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        // Handle the pitch output and store it into local array
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            // Handle the detected Pitch at each tick
            @Override
            public void handlePitch(PitchDetectionResult result,AudioEvent e) {
                final float pitchInHz = result.getPitch();
                long timer_ms = System.currentTimeMillis() - start_ms;

                // Calculate the note from the pitch
                // 12 * log2( f_note / f_ref) + 69
                double note = 12 * Math.log( Math.abs( pitchInHz) / reference ) / Math.log(2) + 69;

                // @TODO pack output to a class
                // Add Signals to their list
                // Pitch
                pitch_list.add(pitchInHz);
                // MyMidiNote
                note_list.add(note);
                // Time
                milisecond_list.add(timer_ms);

                for (IPitchSignalListener listener : mListener)
                {
                    // TODO maybe replace the Pitch Signal by an Object
                    listener.onPitchSignal((float) note, timer_ms);
                }
            }
        };

        // Start the processor with the YIN pitch tracking algorithm
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);

        // Start the Processing
        // Use a Thread
        audio_thread = new Thread(dispatcher,"Audio Dispatcher");
        audio_thread.start();

        // Store the start timing
        start_ms = System.currentTimeMillis();
    }


    // Interrupt the Thread
    public void stopDetection() {
        audio_thread.interrupt();
    }

}
