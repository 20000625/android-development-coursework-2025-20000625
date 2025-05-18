// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/vm/JournalViewModel.java
package uk.ac.hope.mcse.android.coursework.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

public class JournalViewModel extends ViewModel {

    private final MutableLiveData<List<JournalEntry>> journalEntriesLiveData;

    private final ArrayList<JournalEntry> entriesList;

    public JournalViewModel() {
        entriesList = new ArrayList<>();
        journalEntriesLiveData = new MutableLiveData<>(entriesList); // Initialises with the current list

        // TODO (Optional): Add some dummy data for initial testing if you like
    }

    // Public LiveData that Fragments can observe. This is immutable from the outside.
    public LiveData<List<JournalEntry>> getAllEntries() {
        return journalEntriesLiveData;
    }

    // Method to add a new journal entry
    public void addJournalEntry(JournalEntry entry) {
        if (entry != null) {
            entriesList.add(0, entry); // Adds to the beginning of the list for newest first
            journalEntriesLiveData.setValue(new ArrayList<>(entriesList)); // Posts a new list to trigger observers
        }
    }

    // TODO (Later): Implement methods for updating and deleting entries

    private void addDummyData() {
        addJournalEntry(new JournalEntry("First Day", "This is my first journal entry using the app!", System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000) )); // 3 days ago
        addJournalEntry(new JournalEntry("Thoughts on Android", "Learning Android development is fun and challenging.", System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000))); // 1 day ago
        addJournalEntry(new JournalEntry("Today's Progress", "Made good progress on the ViewModel today!", System.currentTimeMillis()));
    }
}