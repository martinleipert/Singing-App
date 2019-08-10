package com.example.martin.singingapp.Feedback;

import java.util.ArrayList;

/*
    Class to calculate and store the feedback
 */

public class SingingFeedback {

    // List for all feedback values
    // Not really necessary
    private ArrayList<Boolean> matched;
    private ArrayList<Double> distance;

    // Storage for the counters
    // The notes matched precisely
    private int matched_count;

    // The sum over the distance values calculated
    private int score_counter;

    // Number of signal values
    private int no_note_ticks;

    // Limit for the Inliers of perfectly matched tones
    private double inlier_limit;

    // Limit for the norm which is more tolerant then the perfect match
    private double norm_limit;

    public SingingFeedback() {

        matched = new ArrayList<Boolean>();
        distance = new ArrayList<Double>();

        // Initialize with 0 -> Will be summed up
        no_note_ticks = 0;
        matched_count = 0;
        score_counter = 0;

        // Initialize the limits
        inlier_limit = 0.5;
        norm_limit = 2.5;

    }

    // Evaluate
    // Takes the pitch value as note
    // Takes the midi note value
    public void evaluate(double note_pitch, double note_midi) {
        // Add one to the ticks
        no_note_ticks++;

        // Case < 0 means the note is probably below limit -> So no pitch detected so low volume
        if (note_pitch < 0) {
            return;
        }

        // Calculate the norm to count the score
        double point = note_score(note_pitch, note_midi, inlier_limit, norm_limit);

        distance.add(point);

        // Boolean check if the tone was matched
        if (checkInlier(note_pitch, note_midi, inlier_limit)) {
            this.matched_count++;
            this.matched.add(true);
        }
        else {
            this.matched.add(false);
        }
    }

    // Helper for distance calculation pitch MIDI
    private double note_distance(double note1, double note2) {
        double dist = Math.abs(note1 - note2);
        return dist;
    }

    // Check if the Note is an Inlier
    private boolean checkInlier(double note_pitch, double note_midi, double dist_limit) {
        double dist = note_distance(note_pitch, note_midi);

        if (dist < dist_limit) {
            return true;
        }
        else
        {
            return false;
        }
    }

    private double note_score(double note_pitch, double note_midi) {
        return note_score(note_pitch, note_midi, this.inlier_limit, this.norm_limit);
    }

    // Calculate the note score
    private double note_score(double note_pitch, double note_midi, double cutoff_lower,
                              double cutoff_upper) {

        // Get distance
        double dist = note_distance(note_pitch, note_midi);

        // Calculate the L2 Norm for the part which is not perfectly matched
        double norm_val = dist - cutoff_lower;

        if (norm_val > 0) {
            norm_val = norm_val * norm_val / ((cutoff_upper - cutoff_lower) * (cutoff_upper - cutoff_lower));
        }
        else
        {
            // Perfectly matched is a score of 1
            return 1;
        }
        // Return points
        return (norm_val > 1) ? (0) : (1 - norm_val);
    }

    // Calculator function which normalizes the score
    public int get_score() {
        return (100 * score_counter) / no_note_ticks;
    }

    // Calculator function which normalizes the matches
    public int get_matched_percentage() {
        return (100 * matched_count) / no_note_ticks;
    }

    // Calc rating from the score and matched percentage
    public int calcRating() {
        int score = this.get_score();
        int matched = this.get_matched_percentage();

        // If cascade to get the star rating
        if ((score > 90) && (matched > 75)) {
            return 5;
        }
        else if ((score > 80) && (matched > 65)) {
            return 4;
        }
        else if ((score > 70) && (matched > 55)) {
            return 3;
        }
        else if ((score > 60) && (matched > 45))
        {
            return 2;
        }
        else
        {
            return 1;
        }
    }
}