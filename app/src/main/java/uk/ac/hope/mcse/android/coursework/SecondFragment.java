// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/SecondFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        // Retrieves arguments using the generated Args class
        if (getArguments() != null) {
            currentEntryId = SecondFragmentArgs.fromBundle(getArguments()).getJournalEntryId();
        }

        if (currentEntryId != -1L) {
            entryToEdit = journalViewModel.getEntryById(currentEntryId);
            if (entryToEdit != null) {
                populateFieldsForEditing(entryToEdit);
                // Optionally change button text or screen title
                binding.buttonSaveEntry.setText(R.string.save_entry);
            } else {
                Toast.makeText(getContext(), "Error: Could not load entry for editing.", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                return;
            }
        } else {
            // Adds a new entry, initialises date display to current date
            selectedDateCalendar = Calendar.getInstance();
            updateDateDisplay();
        }

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());

        binding.buttonSaveEntry.setOnClickListener(v -> {
            String title = binding.edittextEntryTitle.getText().toString().trim();
            String content = binding.edittextEntryContent.getText().toString().trim();

            boolean isValid = true;
            if (title.isEmpty()) {
                binding.textInputLayoutTitle.setError(getString(R.string.error_title_empty));
                isValid = false;
            } else {
                binding.textInputLayoutTitle.setError(null);
            }

            if (content.isEmpty()) {
                binding.textInputLayoutContent.setError(getString(R.string.error_content_empty));
                isValid = false;
            } else {
                binding.textInputLayoutContent.setError(null);
            }

            if (isValid) {
                JournalEntry entryToSave;
                String successMessage;

                // Updates an existing entry
                if (entryToEdit != null) {
                    entryToSave = entryToEdit;
                    entryToSave.setTitle(title);
                    entryToSave.setContent(content);
                    entryToSave.setEntryDateMillis(selectedDateCalendar.getTimeInMillis());
                    successMessage = "Entry '" + title + "' updated!";
                } else {
                    entryToSave = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());
                    successMessage = "Entry '" + title + "' saved!";
                }

                journalViewModel.saveJournalEntry(entryToSave); // Uses the unified save method
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(SecondFragment.this).navigateUp();
            }
        });
    }

    // Helper method to populate fields when editing an entry
    private void populateFieldsForEditing(JournalEntry entry) {
        binding.edittextEntryTitle.setText(entry.getTitle());
        binding.edittextEntryContent.setText(entry.getContent());
        selectedDateCalendar.setTimeInMillis(entry.getEntryDateMillis());
        updateDateDisplay();
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()); // Changed to yyyy
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}