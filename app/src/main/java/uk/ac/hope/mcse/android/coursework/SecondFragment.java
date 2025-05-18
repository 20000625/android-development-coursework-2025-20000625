package uk.ac.hope.mcse.android.coursework;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private long currentEntryId = -1L; // Default to -1 for new entry
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

        // Retrieves the entry ID passed from FirstFragment
        if (getArguments() != null) {
            currentEntryId = SecondFragmentArgs.fromBundle(getArguments()).getJournalEntryId();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
            Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
            if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                fetchLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                binding.textviewLocationStatus.setText(R.string.location_permission_denied); // Use string resource
            }
        });

        if (currentEntryId != -1L) {
            binding.buttonSaveEntry.setText(getString(R.string.update_entry_button_text));
            binding.buttonDeleteEntry.setVisibility(View.VISIBLE);
            journalViewModel.getEntryById(currentEntryId).observe(getViewLifecycleOwner(), journalEntry -> {
                if (journalEntry != null) {
                    entryToEdit = journalEntry;
                    populateFieldsForEditing(entryToEdit);
                } else {
                    if (currentEntryId != -1L) { // Avoid toast if ID was already -1
                        Toast.makeText(getContext(), "Entry not found or has been deleted.", Toast.LENGTH_LONG).show();
                        NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                    }
                }
            });
        } else {
            binding.buttonSaveEntry.setText(getString(R.string.save_entry));
            binding.edittextEntryTitle.setText("");
            binding.edittextEntryContent.setText("");
            selectedDateCalendar = Calendar.getInstance();
            updateDateDisplay();
            binding.buttonDeleteEntry.setVisibility(View.GONE);
            binding.buttonAddLocation.setVisibility(View.VISIBLE); // Ensures visible for new entries
            binding.textviewLocationStatus.setText(getString(R.string.no_location_added));
            currentLatitude = null;
            currentLongitude = null;
            entryToEdit = null;
        }

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonSaveEntry.setOnClickListener(v -> saveOrUpdateEntry());
        binding.buttonDeleteEntry.setOnClickListener(v -> confirmDeleteEntry());
        binding.buttonAddLocation.setOnClickListener(v -> requestLocationPermissions());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set the ActionBar title
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                if (currentEntryId != -1L) {
                    if (entryToEdit != null && entryToEdit.getTitle() != null && !entryToEdit.getTitle().isEmpty()) {
                        actionBar.setTitle(entryToEdit.getTitle());
                    } else {
                        actionBar.setTitle(getString(R.string.title_edit_entry));
                    }
                } else {
                    actionBar.setTitle(getString(R.string.title_new_entry));
                }
            }
        }
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
            binding.buttonAddLocation.setVisibility(View.GONE); // Hides if location exists
        } else {
            binding.textviewLocationStatus.setText(getString(R.string.no_location_added));
            binding.buttonAddLocation.setVisibility(View.VISIBLE); // Shows if no location
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

            if (entryToEdit != null && currentEntryId != -1L) { // Editing existing
                entryToSave = entryToEdit;
                entryToSave.setTitle(title);
                entryToSave.setContent(content);
                entryToSave.setEntryDateMillis(selectedDateCalendar.getTimeInMillis());
                // Only update location if new location data is available from a fresh fetch.
                entryToSave.setLatitude(currentLatitude);
                entryToSave.setLongitude(currentLongitude);
                successMessage = "Entry '" + title + "' updated!";
            } else { // Creating new
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
            binding.textviewLocationStatus.setText(R.string.location_permission_not_granted); // Use string resource
            return;
        }

        binding.textviewLocationStatus.setText(R.string.fetching_location); // Use string resource
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull @Override public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return new CancellationTokenSource().getToken();
            }
            @Override public boolean isCancellationRequested() { return false; }
        }).addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                binding.textviewLocationStatus.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f", currentLatitude, currentLongitude));
                Toast.makeText(getContext(), R.string.location_captured, Toast.LENGTH_SHORT).show(); // Use string resource
                binding.buttonAddLocation.setVisibility(View.GONE); // Hide button after capturing location
            } else {
                binding.textviewLocationStatus.setText(R.string.could_not_get_location); // Use string resource
                Toast.makeText(getContext(), R.string.failed_to_get_location_ensure_services, Toast.LENGTH_LONG).show(); // Use string resource
                currentLatitude = null;
                currentLongitude = null;
            }
        }).addOnFailureListener(requireActivity(), e -> {
            binding.textviewLocationStatus.setText(R.string.error_fetching_location); // Use string resource
            Toast.makeText(getContext(), getString(R.string.error_getting_location_message, e.getMessage()), Toast.LENGTH_LONG).show(); // Use string resource
            currentLatitude = null;
            currentLongitude = null;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
