package org.mad.transit.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.mad.transit.R;
import org.mad.transit.model.Stop;

import java.util.List;

public class SingleLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final List<Stop> lineStops;
    private final OnItemClickListener onItemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public SingleLineAdapter(Activity context, List<Stop> lineStops, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.lineStops = lineStops;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(this.context).inflate(R.layout.single_line_bottom_sheet_list_item, parent, false);
        return new RecyclerViewViewHolder(rootView, this.onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Stop stop = this.lineStops.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;

        viewHolder.stopTitle.setText(stop.getTitle());
        viewHolder.itemView.setBackgroundColor(selectedPosition == position? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return this.lineStops.size();
    }

    private class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView stopTitle;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            this.stopTitle = itemView.findViewById(R.id.line_stop_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    notifyItemChanged(selectedPosition);
                    selectedPosition = position;
                    notifyItemChanged(selectedPosition);
                    onItemClickListener.onItemClick(RecyclerViewViewHolder.this.getAdapterPosition());
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
