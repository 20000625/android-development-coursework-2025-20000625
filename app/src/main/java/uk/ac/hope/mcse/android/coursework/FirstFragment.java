// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/FirstFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
// TODO: Later, import NavHostFragment, your JournalEntry model, adapter, and ViewModel

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    // TODO: Declare your JournalEntryAdapter and ViewModel here

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.recyclerviewJournalEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: Initialize your adapter: journalAdapter = new JournalEntryAdapter(...);
        // TODO: Set adapter to RecyclerView: binding.recyclerviewJournalEntries.setAdapter(journalAdapter);

        // TODO: Initialize ViewModel
        // TODO: Observe LiveData from ViewModel and update adapter
        // For now, let's just show/hide the empty placeholder
        updateEmptyViewVisibility(true); // Assume empty initially
    }

    // TODO: Call this method when the list data changes
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