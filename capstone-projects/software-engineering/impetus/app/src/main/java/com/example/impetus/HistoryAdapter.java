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
 * RecyclerView adapter used by HistoryActivity to display saved weigh-ins.
 */
public class HistoryAdapter
        extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

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

    public HistoryAdapter(
            List<WeighInEntry> entries,
            OnEditClickListener editClickListener,
            OnDeleteClickListener deleteClickListener
    ) {
        this.entries = entries;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    /**
     * Inflates the layout used for each history row.
     */
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_history_weigh_in,
                        parent,
                        false
                );

        return new HistoryViewHolder(view);
    }

    /**
     * Binds a weigh-in entry to the corresponding history row.
     */
    @Override
    public void onBindViewHolder(
            @NonNull HistoryViewHolder holder,
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
     * Holds references to the views used by a single history row.
     */
    public static class HistoryViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView textDate;
        private final TextView textTime;
        private final TextView textWeight;
        private final ImageButton buttonEdit;
        private final ImageButton buttonDelete;

        public HistoryViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            textDate =
                    itemView.findViewById(
                            R.id.textHistoryDate
                    );

            textTime =
                    itemView.findViewById(
                            R.id.textHistoryTime
                    );

            textWeight =
                    itemView.findViewById(
                            R.id.textHistoryWeight
                    );

            buttonEdit =
                    itemView.findViewById(
                            R.id.buttonHistoryEdit
                    );

            buttonDelete =
                    itemView.findViewById(
                            R.id.buttonHistoryDelete
                    );
        }
    }
}