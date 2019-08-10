package com.example.martin.singingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Timer;
import java.util.TimerTask;

import com.androidplot.Plot;
import com.androidplot.PlotListener;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import com.example.martin.singingapp.Feedback.Feedback;
import com.example.martin.singingapp.Feedback.SingingFeedback;

public class MainActivity extends AppCompatActivity implements PlotListener, IFinishedSingingObserver, LyricsReceiver {

    private DataMerger data_merger;

    private XYPlot plot;

    private Timer timer;

    private TextView textView;

    private String currentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.data_merger = new DataMerger(this);

        this.currentText = new String("");

        setContentView(R.layout.activity_main);

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // Start the plot update every 200ms
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updatePlot();
            }
        },
                0,
                200
        );

        // Initialize the TextView
        textView = (TextView) findViewById(R.id.textView);
        textView.setText("Dummy Text");
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextSize(16);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }


    private final int MY_PERMISSIONS_READ_EXTERNAL_STORAGE = 1;

    // Here comes the main contentList
    @Override
    protected void onStart() {
        super.onStart();


        // @TODO Start button before the actual start and a countdown

        if (requestStoragePermissions() && requestAudioPermissions()) {

            this.data_merger.startProcessing("");
            setupPlot();
        }

    }

    @Override
    public void onBeforeDraw(Plot source, Canvas canvas) {
        // write-lock each active series for writes
    }

    @Override
    public void onAfterDraw(Plot source, Canvas canvas) {
        // unlock any locked seriesString composite_text = new String("");
    }

    // Update function for the plot which is trigered by a timer
    private void updatePlot() {
        plot.redraw();
    }

    @Override
    public void receiveLyrics(String already_passed, String to_sing) {
        final String formatted_text = "<font color=\"#FFFFFF\"><b>" + already_passed + "</b></font> <font color=\"#f39c12\"><b>" + to_sing + "</b></font>";

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(Html.fromHtml(formatted_text, Html.FROM_HTML_MODE_LEGACY));
            }
        });
    }

    private void setupPlot() {
        XYSeries series1;
        // @TODO Concurrent Modification Exception

        // series1 = new SimpleXYSeries(x_data_pitch, y_data_pitch, "Pitch");

        series1 = this.data_merger.getPitchSeries();

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.point_plot);
        series1Format.setPointLabelFormatter(null);


        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);


        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        // XYSeries series2 = new SimpleXYSeries(x_data_midi, y_data_midi, "Midi");
        XYSeries series2 = this.data_merger.getMidiSeries();

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series2Format =
                new LineAndPointFormatter(this, R.xml.point_plot_midi);
        series2Format.setPointLabelFormatter(null);

        // add a new series' to the xyplot:
        plot.addSeries(series2, series2Format);

        plot.setRangeBoundaries(10, 90, BoundaryMode.FIXED );
        // upper boundary of the plot will always be 50 or higher

    }

    //Requesting run-time permissions
    //Create placeholder for user's consent to record_audio permission.
    //This will be used in handling callback
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;


    // Request permissions function
    private boolean requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        else {
            return true;
        }

        //If permission is granted, then go ahead recording audio
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            return true;

        }

        return false;
    }

    //Request permission handling callback...
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private boolean requestStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_EXTERNAL_STORAGE);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_READ_EXTERNAL_STORAGE);
            }
        }
        //If permission is granted, then go ahead recording audio
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            return true;

        }
        else {
            return false;
        }
    }

    @Override
    public void onFinishedSinging() {

        // Calculate the feedback
        SingingFeedback feedback = this.data_merger.getFeedback();
        int rating = feedback.calcRating();
        int matched_percentage = feedback.get_matched_percentage();
        int score = feedback.get_score();

        // Integer value -> Pack the Bundle to pass to the activity
        Bundle feedback_bundle = new Bundle();
        feedback_bundle.putInt("rating", rating);
        feedback_bundle.putInt("matched_percentage", matched_percentage);
        feedback_bundle.putInt("score", score);

        // Prepare the context for the start of the activity
        Intent intent = new Intent(MainActivity.this, Feedback.class);

        // Pack the Bundle to the intent
        intent.putExtras(feedback_bundle);

        // Start the Activity
        startActivity(intent);

    }
}
