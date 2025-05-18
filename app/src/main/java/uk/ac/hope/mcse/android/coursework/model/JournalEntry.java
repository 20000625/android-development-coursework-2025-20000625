// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/model/JournalEntry.java
package uk.ac.hope.mcse.android.coursework.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public class JournalEntry {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis()); // Simple ID generator

    private long id;
    private String title;
    private String content;
    private long entryDateMillis; // Stores date as milliseconds for easy sorting/storage

    // Constructor for new entries
    public JournalEntry(String title, String content, long entryDateMillis) {
        this.id = ID_GENERATOR.incrementAndGet(); // Assigns a unique ID
        this.title = title;
        this.content = content;
        this.entryDateMillis = entryDateMillis;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getEntryDateMillis() {
        return entryDateMillis;
    }

    public void setEntryDateMillis(long entryDateMillis) {
        this.entryDateMillis = entryDateMillis;
    }

    // Helper to get a formatted date string
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(entryDateMillis));
    }

    @Override
    public String toString() {
        return title + " (" + getFormattedDate() + ")";
    }
}
