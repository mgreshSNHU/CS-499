package com.example.impetus;

/**
 * Represents a single weigh-in record.
 */
public class WeighInEntry {

    // Weigh-in data
    private final long id;
    private final String date;
    private final String time;
    private final double weight;
    private final int mood;
    private final String notes;

    /**
     * Creates a weigh-in entry using values retrieved from or
     * prepared for the local SQLite database.
     */
    public WeighInEntry(
            long id,
            String date,
            String time,
            double weight,
            int mood,
            String notes
    ) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.weight = weight;
        this.mood = mood;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public double getWeight() {
        return weight;
    }

    public int getMood() {
        return mood;
    }

    public String getNotes() {
        return notes;
    }
}