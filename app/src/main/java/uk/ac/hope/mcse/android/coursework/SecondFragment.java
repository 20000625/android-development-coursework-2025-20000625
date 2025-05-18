// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/SecondFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
    private Calendar selectedDateCalendar = Calendar.getInstance(); // Defaults to current date
    private JournalViewModel journalViewModel;
    private long currentEntryId = -1L; // Defaults to -1, indicating a new entry
    private JournalEntry entryToEdit = null; // Holds the entry being edited, null for a new entry

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

        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        // Attempts to get journalEntryId from navigation arguments
        if (getArguments() != null) {
            currentEntryId = SecondFragmentArgs.fromBundle(getArguments()).getJournalEntryId();
        }

        if (currentEntryId != -1L) {
            // Observes LiveData from ViewModel for this specific entry.
            journalViewModel.getEntryById(currentEntryId).observe(getViewLifecycleOwner(), new Observer<JournalEntry>() {
                @Override
                public void onChanged(JournalEntry journalEntry) {
                    if (journalEntry != null) {
                        entryToEdit = journalEntry;
                        populateFieldsForEditing(entryToEdit);
                        binding.buttonDeleteEntry.setVisibility(View.VISIBLE); // Shows delete button
                    } else {
                        // This case could happen if the entry was deleted just before navigating here, or if currentEntryId is somehow invalid.
                        if (currentEntryId != -1L) {
                            Toast.makeText(getContext(), "Entry not found or has been deleted.", Toast.LENGTH_LONG).show();
                            NavHostFragment.findNavController(SecondFragment.this).navigateUp(); // Goes back
                        }
                    }
                }
            });
        } else {
            // Ensures fields are clear and date is current.
            entryToEdit = null; // Ensures no stale entry object is referenced

            // 1. Clears the title field
            binding.edittextEntryTitle.setText("");

            // 2. Clears the content field
            binding.edittextEntryContent.setText("");

            // 3. Resets the calendar to the current date and time
            selectedDateCalendar = Calendar.getInstance();

            // 4. Updates the date display TextView to show the current date
            updateDateDisplay();

            // 5. Ensures delete button is hidden (as it's a new entry)
            binding.buttonDeleteEntry.setVisibility(View.GONE);

            // Ensures save button has the default "Save Entry" text (or similar)
            binding.buttonSaveEntry.setText(getString(R.string.save_entry));
        }

        // Listener for the "Change Date" button
        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());

        // Listener for the "Save Entry" button
        binding.buttonSaveEntry.setOnClickListener(v -> saveOrUpdateEntry());

        // Listener for the "Delete Entry" button (its visibility is handled above)
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
            binding.textInputLayoutTitle.setError(null); // Clear error
        }

        if (content.isEmpty()) {
            binding.textInputLayoutContent.setError(getString(R.string.error_content_empty));
            isValid = false;
        } else {
            binding.textInputLayoutContent.setError(null); // Clear error
        }

        if (isValid) {
            JournalEntry entryToSave;
            String successMessage;

            if (entryToEdit != null && currentEntryId != -1L) {
                entryToSave = entryToEdit;
                entryToSave.setTitle(title);
                entryToSave.setContent(content);
                entryToSave.setEntryDateMillis(selectedDateCalendar.getTimeInMillis());
                successMessage = "Entry '" + title + "' updated!";
            } else {
                entryToSave = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());
                successMessage = "Entry '" + title + "' saved!";
            }

            journalViewModel.saveJournalEntry(entryToSave); // ViewModel handles add or update logic
            Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(SecondFragment.this).navigateUp(); // Navigates back
        }
    }

    private void confirmDeleteEntry() {
        if (entryToEdit == null) { // This should not be callable if delete button isn't visible
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
                .setNegativeButton(R.string.cancel_delete, null) // No action on cancel
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month); // Month is 0-indexed in Calendar
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay(); // Updates the TextView with the newly selected date
        };

        new DatePickerDialog(requireContext(),
                dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()); // Changed to yyyy for clarity
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to prevent memory leaks with ViewBinding
    }
}