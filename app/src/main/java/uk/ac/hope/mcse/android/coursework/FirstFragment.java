// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/FirstFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;
import uk.ac.hope.mcse.android.coursework.ui.JournalEntryAdapter;
import uk.ac.hope.mcse.android.coursework.vm.JournalViewModel;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding; // ViewBinding instance
    private JournalEntryAdapter journalAdapter; // Adapter for the RecyclerView
    private JournalViewModel journalViewModel; // ViewModel instance

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflates the layout for this fragment using ViewBinding
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Returns the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialises ViewModel. Scoped to the Activity to allow sharing with SecondFragment.
        journalViewModel = new ViewModelProvider(requireActivity()).get(JournalViewModel.class);

        // Setup RecyclerView
        binding.recyclerviewJournalEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerviewJournalEntries.setHasFixedSize(true);

        // Initialises Adapter with an empty list and the item click listener
        // The adapter will be updated by LiveData observation
        journalAdapter = new JournalEntryAdapter(new ArrayList<>(), new JournalEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JournalEntry entry) {
                FirstFragmentDirections.ActionFirstFragmentToSecondFragment action =
                        FirstFragmentDirections.actionFirstFragmentToSecondFragment();
                action.setJournalEntryId(entry.getId()); // Pass the ID of the clicked entry

                NavHostFragment.findNavController(FirstFragment.this).navigate(action);
            }
        });
        binding.recyclerviewJournalEntries.setAdapter(journalAdapter);

        // Observes LiveData from ViewModel for changes in the journal entries list
        journalViewModel.getAllEntries().observe(getViewLifecycleOwner(), new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(List<JournalEntry> entries) {
                // Updates the adapter's data when the LiveData changes
                if (entries != null) {
                    journalAdapter.setEntries(entries);
                }
                // Updates visibility of the empty placeholder view
                updateEmptyViewVisibility(entries == null || entries.isEmpty());
            }
        });
    }

    // Helper method to show/hide the empty placeholder view
    private void updateEmptyViewVisibility(boolean isEmpty) {

        if (binding.groupEmptyState != null) { // If using the enhanced Group for empty state
            if (isEmpty) {
                binding.recyclerviewJournalEntries.setVisibility(View.GONE);
                binding.groupEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerviewJournalEntries.setVisibility(View.VISIBLE);
                binding.groupEmptyState.setVisibility(View.GONE);
            }
        } else if (binding.textviewEmptyJournalPlaceholder != null) { // If using just the TextView
            if (isEmpty) {
                binding.recyclerviewJournalEntries.setVisibility(View.GONE);
                binding.textviewEmptyJournalPlaceholder.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerviewJournalEntries.setVisibility(View.VISIBLE);
                binding.textviewEmptyJournalPlaceholder.setVisibility(View.GONE);
            }
        } else {
            if (isEmpty) {
                binding.recyclerviewJournalEntries.setVisibility(View.GONE);
            } else {
                binding.recyclerviewJournalEntries.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Releases the binding instance when the view is destroyed to prevent memory leaks
        binding = null;
    }
}