// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/SecondFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentSecondBinding;
// TODO: Later, import your JournalEntry model and ViewModel

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private Calendar selectedDateCalendar = Calendar.getInstance(); // To store the selected date

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateDateDisplay(); // Display current date initially

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());

        binding.buttonSaveEntry.setOnClickListener(v -> {
            String title = binding.edittextEntryTitle.getText().toString().trim();
            String content = binding.edittextEntryContent.getText().toString().trim();

            if (title.isEmpty()) {
                binding.textInputLayoutTitle.setError("Title cannot be empty");
                // Or use Toast: Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            } else {
                binding.textInputLayoutTitle.setError(null); // Clear error
            }

            if (content.isEmpty()) {
                binding.textInputLayoutContent.setError("Content cannot be empty");
                // Or use Toast: Toast.makeText(getContext(), "Content cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            } else {
                binding.textInputLayoutContent.setError(null); // Clear error
            }

            // TODO: Create JournalEntry object
            // JournalEntry newEntry = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());

            // TODO: Pass this entry to ViewModel to save it
            Toast.makeText(getContext(), "Entry '" + title + "' would be saved for " + binding.textviewEntryDate.getText(), Toast.LENGTH_LONG).show();


            // Navigate back to the FirstFragment
            NavHostFragment.findNavController(SecondFragment.this).navigateUp();
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, dayOfMonth) -> {
            selectedDateCalendar.set(Calendar.YEAR, year);
            selectedDateCalendar.set(Calendar.MONTH, month);
            selectedDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateDisplay();
        };

        new DatePickerDialog(getContext(), dateSetListener,
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}