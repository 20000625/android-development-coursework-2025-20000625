// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/SecondFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;
import uk.ac.hope.mcse.android.coursework.vm.JournalViewModel;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private Calendar selectedDateCalendar = Calendar.getInstance();
    private JournalViewModel journalViewModel;
    private long currentEntryId = -1L; // Stores ID of entry being edited, -1L for new entry
    private JournalEntry entryToEdit = null; // Stores the full entry object being edited

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialises ViewModel (scoped to the Activity)
        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        // Retrieves arguments using the generated Args class
        // Ensures you have rebuilt the project after adding arguments to nav_graph.xml
        if (getArguments() != null) {
            currentEntryId = SecondFragmentArgs.fromBundle(getArguments()).getJournalEntryId();
        }

        if (currentEntryId != -1L) {
            // Observes the LiveData for the specific entry from the ViewModel
            journalViewModel.getEntryById(currentEntryId).observe(getViewLifecycleOwner(), new Observer<JournalEntry>() {
                @Override
                public void onChanged(JournalEntry journalEntry) {
                    // This check is important because LiveData might emit null initially or if the entry is deleted from another source while being observed.
                    if (journalEntry != null) {
                        entryToEdit = journalEntry; // Stores the loaded entry
                        populateFieldsForEditing(entryToEdit);
                        binding.buttonSaveEntry.setText(getString(R.string.save_entry));
                        binding.buttonDeleteEntry.setVisibility(View.VISIBLE); // Shows delete button
                    } else {
                        // Entry with given ID not found in the database, or was deleted.
                        if (currentEntryId != -1L) {
                            Toast.makeText(getContext(), "Entry not found or has been deleted.", Toast.LENGTH_LONG).show();
                            NavHostFragment.findNavController(SecondFragment.this).navigateUp(); // Goes back
                        }
                    }
                }
            });
        } else {
            selectedDateCalendar = Calendar.getInstance(); // Ensures date is current for new entry
            updateDateDisplay();
            binding.buttonDeleteEntry.setVisibility(View.GONE); // Hides delete button for new entries
        }

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonSaveEntry.setOnClickListener(v -> saveOrUpdateEntry());
        binding.buttonDeleteEntry.setOnClickListener(v -> confirmDeleteEntry());
    }

    private void populateFieldsForEditing(JournalEntry entry) {
        binding.edittextEntryTitle.setText(entry.getTitle());
        binding.edittextEntryContent.setText(entry.getContent());
        selectedDateCalendar.setTimeInMillis(entry.getEntryDateMillis());
        updateDateDisplay();
    }

    private void saveOrUpdateEntry() {
        String title = binding.edittextEntryTitle.getText().toString().trim();
        String content = binding.edittextEntryContent.getText().toString().trim();

        boolean isValid = true;
        if (title.isEmpty()) {
            binding.textInputLayoutTitle.setError(getString(R.string.error_title_empty));
            isValid = false;
        } else {
            binding.textInputLayoutTitle.setError(null); // Clears error
        }

        if (content.isEmpty()) {
            binding.textInputLayoutContent.setError(getString(R.string.error_content_empty));
            isValid = false;
        } else {
            binding.textInputLayoutContent.setError(null); // Clears error
        }

        if (isValid) {
            JournalEntry entryToSave;
            String successMessage;

            if (entryToEdit != null && currentEntryId != -1L) { // Checks if a user is in edit mode
                entryToSave = entryToEdit; // Uses the existing entry object (which has the correct ID)
                entryToSave.setTitle(title);
                entryToSave.setContent(content);
                entryToSave.setEntryDateMillis(selectedDateCalendar.getTimeInMillis());
                // ViewModel's saveJournalEntry will handle update because ID is set
                successMessage = "Entry '" + title + "' updated!";
            } else {
                // Creates a new entry; Room will auto-generate the ID on insert
                entryToSave = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());
                successMessage = "Entry '" + title + "' saved!";
            }

            journalViewModel.saveJournalEntry(entryToSave); // Calls ViewModel to save or update
            Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(SecondFragment.this).navigateUp(); // Goes back to list
        }
    }

    private void confirmDeleteEntry() {
        if (entryToEdit == null) { // Safety check, delete button should only be visible if entryToEdit is not null
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.confirm_delete, (dialog, which) -> {
                    journalViewModel.deleteJournalEntry(entryToEdit);
                    Toast.makeText(getContext(), R.string.entry_deleted_toast, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                })
                .setNegativeButton(R.string.cancel_delete, null) // User clicked "Cancel"
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        };

        new DatePickerDialog(requireContext(), dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()); // Corrected yyyy
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}