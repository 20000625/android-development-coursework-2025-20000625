// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/FirstFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // Imports ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList; // For initializing adapter with an empty list
import java.util.List; // Imports the List

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry; // Imports the model
import uk.ac.hope.mcse.android.coursework.ui.JournalEntryAdapter; // Imports the adapter
import uk.ac.hope.mcse.android.coursework.vm.JournalViewModel; // Imports the ViewModel

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private JournalEntryAdapter journalAdapter;
    private JournalViewModel journalViewModel;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialises ViewModel - scope it to the Activity to share with SecondFragment
        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        // Setup RecyclerView
        binding.recyclerviewJournalEntries.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialises Adapter with an empty list initially and set item click listener (TODO later)
        journalAdapter = new JournalEntryAdapter(new ArrayList<>() /*, entry -> {
            // TODO: Handle item click: Navigate to SecondFragment with entry data for viewing/editing

        } */);
        binding.recyclerviewJournalEntries.setAdapter(journalAdapter);

        // Observes LiveData from ViewModel
        journalViewModel.getAllEntries().observe(getViewLifecycleOwner(), entries -> {
            // Updates the adapter's data
            journalAdapter.setEntries(entries);
            // Updates visibility of the empty placeholder
            updateEmptyViewVisibility(entries == null || entries.isEmpty());
        });
    }

    private void updateEmptyViewVisibility(boolean isEmpty) {
        if (isEmpty) {
            binding.recyclerviewJournalEntries.setVisibility(View.GONE);
            binding.textviewEmptyJournalPlaceholder.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerviewJournalEntries.setVisibility(View.VISIBLE);
            binding.textviewEmptyJournalPlaceholder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}