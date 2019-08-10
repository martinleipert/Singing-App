package com.example.martin.singingapp.Feedback;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.martin.singingapp.R;

/*

    Martin Leipert
    martin.leipert@fau.de

    Activity for the feedback provided
    Three types
    The percentage of tones matched precisely
    The score calculated from the distance to the right tone
    And a star rating which combnes both

 */

public class Feedback extends AppCompatActivity {

    // Storage variables for points and stars
    private int rating;
    private int matched_percentage;
    private int score;

    // Output elements
    private ProgressBar matched_view;
    private TextView score_view;
    private RatingBar rating_view;

    // Start activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_feedback);

        // Get points from the Bundle
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String value = extras.getString("key");
                //The key argument here must match that used in the other activity
                this.rating = extras.getInt("rating");
                this.matched_percentage = extras.getInt("matched_percentage");
                this.score = extras.getInt("score");
            }
            else {
                return;
            }
        }
        else {

            this.rating = savedInstanceState.getInt("rating");
            this.matched_percentage = savedInstanceState.getInt("matched_percentage");
            this.score = savedInstanceState.getInt("score");
        }

        // Find the view elements
        this.score_view = findViewById(R.id.textView_score);
        this.matched_view = findViewById(R.id.progressBar_matchedTones);
        this.rating_view = findViewById(R.id.ratingBar_overall);

        // Set the maximum values
        this.rating_view.setMax(5);
        this.matched_view.setMax(100);

        // Create string for the points
        String score_string = String.valueOf(this.score) + " of 100 points";

        // Set the ratings
        this.rating_view.setNumStars(this.rating);
        this.matched_view.setProgress(this.matched_percentage);
        this.score_view.setText(score_string);
    }


    // Here comes the main content
    @Override
    protected void onStart() {
        super.onStart();

        // Find the Corresponding v
        this.score_view = findViewById(R.id.textView_score);
        this.matched_view = findViewById(R.id.progressBar_matchedTones);
        this.rating_view = findViewById(R.id.ratingBar_overall);

        // Set the stars for the overall rating
        this.rating_view.setNumStars(this.rating);

        // Set the matched points progress Bar
        this.matched_view.setMax(100);
        this.matched_view.setProgress(this.matched_percentage);

        String scoreText = String.valueOf(this.score) + " of 100 points achieved";
        this.score_view.setText(scoreText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
