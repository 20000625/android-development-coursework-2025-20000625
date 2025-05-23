// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/db/AppDatabase.java
package uk.ac.hope.mcse.android.coursework.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

// Annotates the class to be a Room database, listing all entities and setting the version.
@Database(entities = {JournalEntry.class}, version = 2, exportSchema = false) // <-- Increment version
public abstract class AppDatabase extends RoomDatabase {

    // Abstract method for Room to generate an implementation for your DAO.
    public abstract JournalEntryDao journalEntryDao();

    // Singleton pattern to prevent multiple instances of the database opening at the same time.
    private static volatile AppDatabase INSTANCE;

    // Number of threads for the database operations ExecutorService.
    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService for running database operations (like inserts, updates, deletes)
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQLite handles this well with ALTER TABLE
            database.execSQL("ALTER TABLE journal_entries ADD COLUMN latitude REAL");
            database.execSQL("ALTER TABLE journal_entries ADD COLUMN longitude REAL");
        }
    };

    // Method to get the singleton instance of the database.
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) { // synchronised block to make it thread-safe
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "journal_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}