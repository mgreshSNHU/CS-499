package com.example.impetus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter used by the Dashboard to display recent weigh-ins.
 */
public class WeighInAdapter
        extends RecyclerView.Adapter<WeighInAdapter.WeighInViewHolder> {

    /**
     * Callback for editing a weigh-in entry.
     */
    public interface OnEditClickListener {
        void onEditClick(int position);
    }

    /**
     * Callback for deleting a weigh-in entry.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    private final List<WeighInEntry> entries;
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public WeighInAdapter(
            List<WeighInEntry> entries,
            OnEditClickListener editClickListener,
            OnDeleteClickListener deleteClickListener
    ) {
        this.entries = entries;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    /**
     * Inflates the layout used for each recent weigh-in row.
     */
    @NonNull
    @Override
    public WeighInViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_weigh_in,
                        parent,
                        false
                );

        return new WeighInViewHolder(view);
    }

    /**
     * Binds a weigh-in entry to the corresponding dashboard row.
     */
    @Override
    public void onBindViewHolder(
            @NonNull WeighInViewHolder holder,
            int position
    ) {
        WeighInEntry entry = entries.get(position);

        holder.textDate.setText(entry.getDate());
        holder.textTime.setText(entry.getTime());

        holder.textWeight.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f lb",
                        entry.getWeight()
                )
        );

        holder.buttonEdit.setOnClickListener(view -> {
            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                editClickListener.onEditClick(adapterPosition);
            }
        });

        holder.buttonDelete.setOnClickListener(view -> {
            int adapterPosition =
                    holder.getBindingAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                deleteClickListener.onDeleteClick(adapterPosition);
            }
        });
    }

    /**
     * Returns the number of weigh-ins currently displayed.
     */
    @Override
    public int getItemCount() {
        return entries.size();
    }

    /**
     * Holds references to the views used by a single dashboard row.
     */
    public static class WeighInViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView textDate;
        private final TextView textTime;
        private final TextView textWeight;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public WeighInViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            textDate =
                    itemView.findViewById(
                            R.id.textEntryDate
                    );

            textTime =
                    itemView.findViewById(
                            R.id.textEntryTime
                    );

            textWeight =
                    itemView.findViewById(
                            R.id.textEntryWeight
                    );

            buttonEdit =
                    itemView.findViewById(
                            R.id.buttonEditEntry
                    );

            buttonDelete =
                    itemView.findViewById(
                            R.id.buttonDeleteEntry
                    );
        }
    }
}