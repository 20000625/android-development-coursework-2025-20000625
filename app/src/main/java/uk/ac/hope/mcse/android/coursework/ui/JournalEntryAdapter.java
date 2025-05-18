// Path: app/src/main/java/uk/ac/hope/mcse/android/coursework/ui/JournalEntryAdapter.java
package uk.ac.hope.mcse.android.coursework.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uk.ac.hope.mcse.android.coursework.R;
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> entries;
    private final OnItemClickListener listener; // Listener instance

    // Defines an interface for click events
    public interface OnItemClickListener {
        void onItemClick(JournalEntry entry);
    }

    // Modified Constructor to accept the listener
    public JournalEntryAdapter(List<JournalEntry> entriesList, OnItemClickListener listener) {
        this.entries = entriesList != null ? entriesList : new ArrayList<>();
        this.listener = listener; // Store the listener
    }

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_journal_entry, parent, false);
        return new JournalEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {
        JournalEntry currentEntry = entries.get(position);
        holder.titleTextView.setText(currentEntry.getTitle());
        holder.dateTextView.setText(currentEntry.getFormattedDate());
        holder.previewTextView.setText(currentEntry.getContent());

        // Sets up click listener for the entire item view
        // This will call the onItemClick method of the listener passed from FirstFragment
        holder.itemView.setOnClickListener(view -> {
            // Ensures listener is not null and position is valid
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onItemClick(entries.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    // Method to update the list of entries in the adapter
    public void setEntries(List<JournalEntry> newEntries) {
        this.entries.clear();
        if (newEntries != null) {
            this.entries.addAll(newEntries);
        }
        notifyDataSetChanged();
    }

    // ViewHolder class remains the same
    static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView previewTextView;

        public JournalEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_textview_entry_title);
            dateTextView = itemView.findViewById(R.id.item_textview_entry_date);
            previewTextView = itemView.findViewById(R.id.item_textview_entry_preview);
        }
    }
}