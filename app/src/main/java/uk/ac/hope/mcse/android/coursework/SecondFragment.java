package uk.ac.hope.mcse.android.coursework;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.core.content.ContextCompat;
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
            if (Boolean.TRUE.equals(fineLocationGranted) || Boolean.TRUE.equals(coarseLocationGranted)) {
                fetchLocation();
            } else {
                Toast.makeText(getContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                updateLocationDisplayAndInteractivity();
            }
        });

        setupPrimaryActionButton(); // Calsl new method to set up the action button

        if (currentEntryId != -1L) {
            journalViewModel.getEntryById(currentEntryId).observe(getViewLifecycleOwner(), journalEntry -> {
                if (journalEntry != null) {
                    entryToEdit = journalEntry;
                    populateFieldsForEditing(entryToEdit);
                    // Updates action button again in case entryToEdit was null initially
                    setupPrimaryActionButton();
                } else {
                    if (currentEntryId != -1L) {
                        Toast.makeText(getContext(), "Entry not found or has been deleted.", Toast.LENGTH_LONG).show();
                        NavHostFragment.findNavController(SecondFragment.this).navigateUp();
                    }
                }
            });
        } else {
            binding.edittextEntryTitle.setText("");
            binding.edittextEntryContent.setText("");
            selectedDateCalendar = Calendar.getInstance();
            updateDateDisplay();
            entryToEdit = null;
            currentLatitude = null;
            currentLongitude = null;
            updateLocationDisplayAndInteractivity();
            setupPrimaryActionButton(); // Set up for "Save"
        }

        binding.buttonChangeDate.setOnClickListener(v -> showDatePickerDialog());
        binding.buttonAddLocation.setOnClickListener(v -> requestLocationPermissions());
        binding.textviewLocationStatus.setOnClickListener(v -> openLocationInMap());
    }

    private void setupPrimaryActionButton() {
        if (binding == null) return;

        if (currentEntryId != -1L) { // EDITING/VIEWING MODE
            binding.buttonActionPrimary.setText(getString(R.string.delete_entry_button_text));
            binding.buttonActionPrimary.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_delete));
            // Sets style for delete if different (e.g., outlined error color)
            binding.buttonActionPrimary.setOnClickListener(v -> confirmDeleteEntry());
            binding.buttonActionPrimary.setVisibility(View.VISIBLE);
        } else {
            binding.buttonActionPrimary.setText(getString(R.string.save_entry));
            binding.buttonActionPrimary.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_save));
            // Resets to default style if needed
            binding.buttonActionPrimary.setOnClickListener(v -> saveOrUpdateEntry());
            binding.buttonActionPrimary.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                if (currentEntryId != -1L) {
                    actionBar.setTitle(getString(R.string.first_fragment_label)); // Titles for existing entries
                } else {
                    actionBar.setTitle(getString(R.string.title_new_entry));
                }
            }
        }
        updateLocationDisplayAndInteractivity();
        setupPrimaryActionButton(); // Re-setup button in case fragment is resumed
    }

    private void populateFieldsForEditing(JournalEntry entry) {
        binding.edittextEntryTitle.setText(entry.getTitle());
        binding.edittextEntryContent.setText(entry.getContent());
        selectedDateCalendar.setTimeInMillis(entry.getEntryDateMillis());
        updateDateDisplay();
        currentLatitude = entry.getLatitude();
        currentLongitude = entry.getLongitude();
        updateLocationDisplayAndInteractivity();
    }

    private void updateLocationDisplayAndInteractivity() {
        if (binding == null) return;

        if (currentLatitude != null && currentLongitude != null) {
            binding.textviewLocationStatus.setText(String.format(Locale.getDefault(), "Location: %.4f, %.4f (Tap to view)", currentLatitude, currentLongitude));
            binding.textviewLocationStatus.setClickable(true);
            binding.buttonAddLocation.setVisibility(View.GONE);
        } else {
            binding.textviewLocationStatus.setText(getString(R.string.no_location_added));
            binding.textviewLocationStatus.setClickable(false);
            binding.buttonAddLocation.setVisibility(View.VISIBLE);
        }
    }

    private void openLocationInMap() {
        if (currentLatitude != null && currentLongitude != null) {
            String label = (entryToEdit != null && entryToEdit.getTitle() != null && !entryToEdit.getTitle().isEmpty())
                    ? entryToEdit.getTitle()
                    : "Journal Entry Location";
            String uriString = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    currentLatitude, currentLongitude, currentLatitude, currentLongitude, Uri.encode(label));
            Uri gmmIntentUri = Uri.parse(uriString);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(getContext(), "No map application found.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "No location data to show on map.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveOrUpdateEntry() {
        // This method is now only called for NEW entries if the primary action button is "Save"
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
            JournalEntry newEntry = new JournalEntry(title, content, selectedDateCalendar.getTimeInMillis());
            newEntry.setLatitude(currentLatitude);
            newEntry.setLongitude(currentLongitude);
            journalViewModel.saveJournalEntry(newEntry);
            Toast.makeText(getContext(), "Entry '" + title + "' saved!", Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yy", Locale.getDefault());
        binding.textviewEntryDate.setText(sdf.format(selectedDateCalendar.getTime()));
    }

    private void requestLocationPermissions() {
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchLocation() {
        if (binding == null || getContext() == null || !isAdded()) { // More safety checks
            return;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            updateLocationDisplayAndInteractivity();
            return;
        }

        binding.textviewLocationStatus.setText(R.string.fetching_location);
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @NonNull @Override public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return new CancellationTokenSource().getToken();
            }
            @Override public boolean isCancellationRequested() { return false; }
        }).addOnSuccessListener(requireActivity(), location -> {
            if (!isAdded() || binding == null) return; // Checks if fragment is still added
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                Toast.makeText(getContext(), R.string.location_captured, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.could_not_get_location, Toast.LENGTH_LONG).show();
                currentLatitude = null;
                currentLongitude = null;
            }
            updateLocationDisplayAndInteractivity();
        }).addOnFailureListener(requireActivity(), e -> {
            if (!isAdded() || binding == null) return; // Checks if fragment is still added
            Toast.makeText(getContext(), getString(R.string.error_getting_location_message, e.getMessage()), Toast.LENGTH_LONG).show();
            currentLatitude = null;
            currentLongitude = null;
            updateLocationDisplayAndInteractivity();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
