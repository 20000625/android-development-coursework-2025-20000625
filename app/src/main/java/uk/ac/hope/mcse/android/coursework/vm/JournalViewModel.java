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
        journalEntriesLiveData = new MutableLiveData<>(new ArrayList<>(entriesList)); // Initialises with a new list copy
        // addDummyData(); // used for testing
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return journalEntriesLiveData;
    }

    // Modified to handle both add and update for an in-memory list
    public void saveJournalEntry(JournalEntry entry) {
        if (entry == null) return;

        boolean entryExists = false;
        for (int i = 0; i < entriesList.size(); i++) {
            if (entriesList.get(i).getId() == entry.getId()) {
                entriesList.set(i, entry); // Replaces existing entry (update)
                entryExists = true;
                break;
            }
        }

        if (!entryExists) {
            entriesList.add(0, entry); // Adds new entry to the beginning
        }

        // Posts a new list to LiveData to trigger observers
        journalEntriesLiveData.setValue(new ArrayList<>(entriesList));
    }

    // Gets an entry by its ID
    public JournalEntry getEntryById(long entryId) {
        for (JournalEntry entry : entriesList) {
            if (entry.getId() == entryId) {
                return entry;
            }
        }
        return null; // Return null if not found
    }

    private void addDummyData() {
        // Ensures IDs are unique if using this with actual additions
        long now = System.currentTimeMillis();
        saveJournalEntry(new JournalEntry("First Day", "This is my first journal entry!", now - (3L * 24 * 60 * 60 * 1000)));
        saveJournalEntry(new JournalEntry("Android Thoughts", "Learning Android is fun.", now - (1L * 24 * 60 * 60 * 1000)));
        saveJournalEntry(new JournalEntry("Today's Progress", "Implemented ViewModel and Adapter.", now));
    }
}