package com.example.martin.singingapp;

import com.androidplot.xy.XYSeries;

import java.util.ArrayList;


/*

    Martin Leipert
    martin.leipert@fau.de

    Singing Series is required for the Plotting with Androidplot
    It stores the data of the signals and the plotting framework may access them directly
    for a live and permamently updated plot

 */

public class SingingSeries implements XYSeries {

    // Storage for the data
    private ArrayList<Long> x_data;
    private ArrayList<Double> y_data;

    private String title;

    // Size of the plotted section 200*46ms = 10 seconds
    private int section_size;

    // Constructors
    public SingingSeries(String title) {
        this(title, 200);
    }

    public SingingSeries(String title, int section_size) {
        super();

        this.title = title;
        this.section_size = section_size;


        this.x_data = new ArrayList<>(this.section_size);
        this.y_data = new ArrayList<>(this.section_size);

        // Initial padding with zeros
        for (int i = 0; i < this.section_size; i++) {
            this.x_data.add(46L*i);
            this.y_data.add(0d);
        }
    }

    // Getter for the title
    @Override
    public String getTitle() {
        return title;
    }

    // Return size of the data to plot
    @Override
    public int size() {
        return (x_data.size() < section_size) ? (x_data.size()) : (section_size);
    }

    // Get x data at certain index
    @Override
    public Number getX(int index) {
        // return index;
        return x_data.get(getSpanIndex() + index);
    }

    // Get the Y data
    @Override
    public Number getY(int index) {
        return y_data.get(getSpanIndex() + index);
    }

    // Get Last index
    private int getLastIndex() {
        return (y_data.size() > section_size) ? section_size : y_data.size();
    }

    // Get First index
    private int getSpanIndex() {
        if (this.x_data.size() > section_size) {
            return getLastIndex() - section_size;
        } else {
            return 0;
        }
    }

    // Update during the running Plot
    // Important : Use iterators to avoid excepions
    public void update(long x, double y) {
        this.x_data.listIterator().add(x);
        this.y_data.listIterator().add(y);
    }
}
