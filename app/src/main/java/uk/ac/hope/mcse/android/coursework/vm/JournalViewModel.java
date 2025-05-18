// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/vm/JournalViewModel.java
package uk.ac.hope.mcse.android.coursework.vm;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.JournalEntry;
import uk.ac.hope.mcse.android.coursework.repository.JournalRepository;

public class JournalViewModel extends AndroidViewModel { // Extends AndroidViewModel

    private JournalRepository repository;
    private final LiveData<List<JournalEntry>> allEntries;

    public JournalViewModel(Application application) {
        super(application);
        repository = new JournalRepository(application);
        allEntries = repository.getAllEntries();
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return allEntries;
    }

    // This is a method to save / insert a new journal entry (calls repository)
    public void saveJournalEntry(JournalEntry entry) {
        // If ID is 0 or not set, it's an insert.
        // If ID is set (from an existing entry), it's an update.
        // The DAO's OnConflictStrategy.REPLACE handles this if inserting an existing ID.
        if (entry.getId() == 0) { // Assuming new entries have ID 0 before DB assigns one
            repository.insert(entry);
        } else {
            repository.update(entry);
        }
    }

    // Method to get an entry by its ID (calls repository)
    // This now returns LiveData<JournalEntry> directly from DAO via Repository
    public LiveData<JournalEntry> getEntryById(long entryId) {
        return repository.getEntryById(entryId);
    }

    // Method to delete a journal entry (calls repository)
    public void deleteJournalEntry(JournalEntry entryToDelete) {
        repository.delete(entryToDelete);
    }
}