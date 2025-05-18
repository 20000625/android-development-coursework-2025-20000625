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
import androidx.lifecycle.ViewModelProvider; // Imports ViewModelProvider
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry; // Imports the model
import uk.ac.hope.mcse.android.coursework.vm.JournalViewModel; // Imports the ViewModel

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private Calendar selectedDateCalendar = Calendar.getInstance();
    private JournalViewModel journalViewModel; // Declares ViewModel

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

        // Initialises ViewModel - scope it to the Activity to share with FirstFragment
        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        updateDateDisplay();
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
                // Creates a new JournalEntry object
                JournalEntry newEntry = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());

                // Adds the new entry using the ViewModel
                journalViewModel.addJournalEntry(newEntry);

                Toast.makeText(getContext(), "Entry '" + title + "' saved!", Toast.LENGTH_SHORT).show();

                NavHostFragment.findNavController(SecondFragment.this).navigateUp();
            }
        });
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yy", Locale.getDefault());
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}