// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/db/JournalEntryDao.java
package uk.ac.hope.mcse.android.coursework.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

@Dao
public interface JournalEntryDao {

    // This inserts a journal entry.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(JournalEntry journalEntry); // For single sync operations


    // Updates an existing journal entry
    @Update
    void update(JournalEntry journalEntry);

    // Deletes a specific journal entry
    @Delete
    void delete(JournalEntry journalEntry);

    // Gets a specific journal entry by its ID.
    // Returns LiveData so UI can observe changes.
    @Query("SELECT * FROM journal_entries WHERE id = :entryId")
    LiveData<JournalEntry> getEntryById(long entryId);

    // Gets all journal entries, ordered by date (newest first).
    // Returns LiveData so UI can observe changes.
    @Query("SELECT * FROM journal_entries ORDER BY entry_date_millis DESC")
    LiveData<List<JournalEntry>> getAllEntries();
}