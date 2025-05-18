// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/FirstFragment.java
package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager; // Needed for RecyclerView

// TODO (Later): Import NavHostFragment, your JournalEntry model, JournalEntryAdapter, and ViewModel

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    // TODO (Later): Declare your JournalEntryAdapter and ViewModel here


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

        // Setup RecyclerView
        binding.recyclerviewJournalEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO (Later): Initialize your adapter:

        // TODO (Later): Initialize ViewModel:

        // TODO (Later): Observe LiveData from ViewModel to update the adapter and empty view:

        updateEmptyViewVisibility(true);
    }

    // Helper method to show/hide the empty placeholder TextView
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
        binding = null; // Important for ViewBinding in Fragments
    }
}