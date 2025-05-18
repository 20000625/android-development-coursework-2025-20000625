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
        journalEntriesLiveData = new MutableLiveData<>(new ArrayList<>(entriesList));
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return journalEntriesLiveData;
    }

    public void saveJournalEntry(JournalEntry entry) {
        if (entry == null) return;
        boolean entryExists = false;
        for (int i = 0; i < entriesList.size(); i++) {
            if (entriesList.get(i).getId() == entry.getId()) {
                entriesList.set(i, entry);
                entryExists = true;
                break;
            }
        }
        if (!entryExists) {
            entriesList.add(0, entry);
        }
        journalEntriesLiveData.setValue(new ArrayList<>(entriesList));
    }

    public JournalEntry getEntryById(long entryId) {
        for (JournalEntry entry : entriesList) {
            if (entry.getId() == entryId) {
                return entry;
            }
        }
        return null;
    }

    // New method to delete a journal entry
    public void deleteJournalEntry(JournalEntry entryToDelete) {
        if (entryToDelete == null) return;

        boolean removed = false;
        for (int i = 0; i < entriesList.size(); i++) {
            if (entriesList.get(i).getId() == entryToDelete.getId()) {
                entriesList.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            // Posts the updated list to LiveData
            journalEntriesLiveData.setValue(new ArrayList<>(entriesList));
        }
    }

}