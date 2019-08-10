package com.example.martin.singingapp;

import com.example.martin.singingapp.PitchProcessing.IPitchSignalListener;
import com.example.martin.singingapp.PitchProcessing.SignalProcessor;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PitchProcessorTest {
    @Test
    public void signalProcessingWorks() {
        SignalProcessor processor = new SignalProcessor();
        processor.registerCustomEventListener(new DummyLogger());
        processor.startDetection();

        try {
            TimeUnit.SECONDS.sleep(10);
        }
        catch (InterruptedException ex) {

        }

    }

    private class DummyLogger implements IPitchSignalListener {
        @Override

        public void onPitchSignal(float note_signal, long timer_ms) {
            System.out.println("Signal & Time: " + String.valueOf(note_signal) + " - " + String.valueOf(timer_ms));
        }
    }
}