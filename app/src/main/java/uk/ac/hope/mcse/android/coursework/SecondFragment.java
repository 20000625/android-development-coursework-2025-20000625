package uk.ac.hope.mcse.android.coursework;

import android.Manifest; // Required for Manifest.permission
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager; // Required for PackageManager
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Required for ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts; // Required for ActivityResultContracts
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat; // Required for ActivityCompat
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnTokenCanceledListener;

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
    private long currentEntryId = -1L;
    private JournalEntry entryToEdit = null;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private Double currentLatitude = null;
    private Double currentLongitude = null;

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

        if (getArguments() != null) {
            currentEntryId = SecondFragmentArgs.fromBundle(getArguments()).getJournalEntryId();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (fineLocationGranted != null && fineLocationGranted) {
                // Precise location access granted.
                fetchLocation();
            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                // Only approximate location access granted.
                fetchLocation();
            } else {
                // No location access granted.
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                binding.textviewLocationStatus.setText("Location permission denied.");
            }
        });


        if (currentEntryId != -1L) {
            journalViewModel.getEntryById(currentEntryId).observe(getViewLifecycleOwner(), journalEntry -> {
                if (journalEntry != null) {
                    entryToEdit = journalEntry;
                    populateFieldsForEditing(entryToEdit);
                    binding.buttonSaveEntry.setText(getString(R.string.save_entry));
                    binding.buttonDeleteEntry.setVisibility(View.VISIBLE);
                } else {
                    // This case handles if the entry was deleted while we were away
                    if (currentEntryId != -1L) { // Check to prevent toast on initial load if id is bad
                        Toast.makeText(getContext(), "Entry not found or has been deleted.", Toast.LENGTH_LONG).show();
                        NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                    }
                }
            });
        } else {
            // Ensures fields are clear and date is current
            binding.edittextEntryTitle.setText("");
            binding.edittextEntryContent.setText("");
            selectedDateCalendar = Calendar.getInstance();
            updateDateDisplay();
            binding.buttonDeleteEntry.setVisibility(View.GONE);
            entryToEdit = null;
            binding.textviewLocationStatus.setText("No location added.");
            currentLatitude = null;
            currentLongitude = null;
        }

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonSaveEntry.setOnClickListener(v -> saveOrUpdateEntry());
        binding.buttonDeleteEntry.setOnClickListener(v -> confirmDeleteEntry());

        binding.buttonAddLocation.setOnClickListener(v -> {
            requestLocationPermissions();
        });
    }

    private void populateFieldsForEditing(JournalEntry entry) {
        binding.edittextEntryTitle.setText(entry.getTitle());
        binding.edittextEntryContent.setText(entry.getContent());
        selectedDateCalendar.setTimeInMillis(entry.getEntryDateMillis());
        updateDateDisplay();
        currentLatitude = entry.getLatitude();
        currentLongitude = entry.getLongitude();
        if (currentLatitude != null && currentLongitude != null) {
            binding.textviewLocationStatus.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f", currentLatitude, currentLongitude));
        } else {
            binding.textviewLocationStatus.setText("No location added.");
        }
    }

    private void saveOrUpdateEntry() {
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

            if (entryToEdit != null && currentEntryId != -1L) { // Editing existing entry
                entryToSave = entryToEdit;
                entryToSave.setTitle(title);
                entryToSave.setContent(content);
                entryToSave.setEntryDateMillis(selectedDateCalendar.getTimeInMillis());
                entryToSave.setLatitude(currentLatitude);
                entryToSave.setLongitude(currentLongitude);
                successMessage = "Entry '" + title + "' updated!";
            } else { // Creating new entry
                entryToSave = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());
                entryToSave.setLatitude(currentLatitude);
                entryToSave.setLongitude(currentLongitude);
                successMessage = "Entry '" + title + "' saved!";
            }

            journalViewModel.saveJournalEntry(entryToSave);
            Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(SecondFragment.this).navigateUp();
        }
    }

    private void confirmDeleteEntry() {
        if (entryToEdit == null) {
            Toast.makeText(getContext(), "No entry selected to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(getString(R.string.delete_confirmation_message_format, entryToEdit.getTitle()))
                .setPositiveButton(R.string.confirm_delete, (dialog, which) -> {
                    journalViewModel.deleteJournalEntry(entryToEdit);
                    Toast.makeText(getContext(), R.string.entry_deleted_toast, Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                })
                .setNegativeButton(R.string.cancel_delete, null)
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    private void requestLocationPermissions() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            binding.textviewLocationStatus.setText("Permission not granted to fetch location.");
            return;
        }

        binding.textviewLocationStatus.setText("Fetching location...");
        // Ensures the device has location services enabled.
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                // This simple CancellationTokenSource should be sufficient for most cases.
                return new CancellationTokenSource().getToken();
            }

            @Override
            public boolean isCancellationRequested() {
                return false; // Returns true if you want to support cancellation from your side.
            }
        }).addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                binding.textviewLocationStatus.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f", currentLatitude, currentLongitude));
                Toast.makeText(getContext(), "Location captured!", Toast.LENGTH_SHORT).show();
            } else {
                // This can happen if location is turned off on the device, or if it times out.
                binding.textviewLocationStatus.setText("Could not get current location. Try again.");
                Toast.makeText(getContext(), "Failed to get current location. Ensure location services are enabled.", Toast.LENGTH_LONG).show();
                currentLatitude = null; // Clear any potentially stale data
                currentLongitude = null;
            }
        }).addOnFailureListener(requireActivity(), e -> {
            binding.textviewLocationStatus.setText("Error fetching location.");
            Toast.makeText(getContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_LONG).show();
            currentLatitude = null; // Clear any potentially stale data
            currentLongitude = null;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to prevent memory leaks with view binding
    }
}