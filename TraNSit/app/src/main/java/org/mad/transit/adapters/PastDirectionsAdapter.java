package org.mad.transit.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.model.PastDirection;

import java.util.ArrayList;
import java.util.List;

public class PastDirectionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private List<PastDirection> pastDirections;
    private final OnItemClickListener onItemClickListener;

    public PastDirectionsAdapter(Activity context, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.pastDirections = new ArrayList<>();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.past_direction_item, parent, false);
        return new RecyclerViewViewHolder(rootView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PastDirection pastDirection = this.pastDirections.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;
        viewHolder.pastDirectionStartLocationName.setText(this.context.getString(R.string.start_location_name, pastDirection.getStartLocation().getName()));
        viewHolder.pastDirectionEndLocationName.setText(this.context.getString(R.string.end_location_name, pastDirection.getEndLocation().getName()));
        viewHolder.pastDirectionDate.setText(this.context.getString(R.string.date, pastDirection.getDate()));
    }

    @Override
    public int getItemCount() {
        return this.pastDirections.size();
    }

    public List<PastDirection> getPastDirections() {
        return this.pastDirections;
    }

    public void setPastDirections(List<PastDirection> pastDirections) {
        this.pastDirections = pastDirections;
        this.notifyDataSetChanged();
    }

    public Activity getContext() {
        return this.context;
    }

    private static class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView pastDirectionStartLocationName;
        private final TextView pastDirectionEndLocationName;
        private final TextView pastDirectionDate;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            this.pastDirectionStartLocationName = itemView.findViewById(R.id.past_direction_start_location_name);
            this.pastDirectionEndLocationName = itemView.findViewById(R.id.past_direction_end_location_name);
            this.pastDirectionDate = itemView.findViewById(R.id.past_direction_date);

            if (onItemClickListener == null) {
                return;
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(PastDirectionsAdapter.RecyclerViewViewHolder.this.getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}