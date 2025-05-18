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

import uk.ac.hope.mcse.android.coursework.R; // For R.layout.list_item_journal_entry
import uk.ac.hope.mcse.android.coursework.model.JournalEntry;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.JournalEntryViewHolder> {

    private List<JournalEntry> entries;
    // TODO (Later): Add an interface for item click listener

    // Constructor
    public JournalEntryAdapter(List<JournalEntry> entriesList /*, OnItemClickListener listener */) {
        this.entries = entriesList != null ? entriesList : new ArrayList<>();
    }

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflates the item layout (list_item_journal_entry.xml)
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_journal_entry, parent, false);
        return new JournalEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {
        // Gets the current entry
        JournalEntry currentEntry = entries.get(position);

        // Binds data to the views in the ViewHolder
        holder.titleTextView.setText(currentEntry.getTitle());
        holder.dateTextView.setText(currentEntry.getFormattedDate());
        holder.previewTextView.setText(currentEntry.getContent()); // Content might be long, preview handles it

        // TODO (Later): Set up click listener for the item view
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
        notifyDataSetChanged(); // Notifies the RecyclerView that the data has changed
    }

    // ViewHolder class to hold references to the views for each item
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

    // TODO (Later): Define an interface for click events
    // }
}