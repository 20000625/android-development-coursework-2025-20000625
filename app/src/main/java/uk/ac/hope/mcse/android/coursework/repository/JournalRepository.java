// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/repository/JournalRepository.java
package uk.ac.hope.mcse.android.coursework.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.db.AppDatabase;
import uk.ac.hope.mcse.android.coursework.db.JournalEntryDao;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

public class JournalRepository {

    private JournalEntryDao journalEntryDao;
    private LiveData<List<JournalEntry>> allEntries;

    public JournalRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        journalEntryDao = db.journalEntryDao();
        allEntries = journalEntryDao.getAllEntries(); // LiveData query, Room handles background thread
    }

    // Room executes all queries on a separate thread. Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<JournalEntry>> getAllEntries() {
        return allEntries;
    }

    public LiveData<JournalEntry> getEntryById(long entryId) {
        return journalEntryDao.getEntryById(entryId); // LiveData query
    }

    public void insert(JournalEntry journalEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            journalEntryDao.insert(journalEntry);
        });
    }

    public void update(JournalEntry journalEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            journalEntryDao.update(journalEntry);
        });
    }

    public void delete(JournalEntry journalEntry) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            journalEntryDao.delete(journalEntry);
        });
    }
}