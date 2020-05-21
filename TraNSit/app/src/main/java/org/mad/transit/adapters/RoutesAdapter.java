package org.mad.transit.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.Route;
import org.mad.transit.model.RoutePart;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static org.mad.transit.model.TravelType.WALK;

public class RoutesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final List<Route> routes;
    private final OnItemClickListener onItemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public RoutesAdapter(Activity context, List<Route> routes, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.routes = routes;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.routes_bottom_sheet_list_item, parent, false);
        return new RecyclerViewViewHolder(rootView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Route route = routes.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;
        viewHolder.totalDuration.setText(String.valueOf(route.getTotalDuration()));
        viewHolder.partsContainer.removeAllViews();

        for (int i = 0; i < route.getParts().size(); i++) {
            RoutePart part = route.getParts().get(i);

            if (WALK == part.getTravelType()) {
                View walkDurationView = context.getLayoutInflater().inflate(R.layout.walk_duration, null);
                TextView walkDuration = walkDurationView.findViewById(R.id.walk_duration_number);
                walkDuration.setText(String.valueOf(part.getDuration()));
                viewHolder.partsContainer.addView(walkDurationView);
            } else {
                View lineNumberView = context.getLayoutInflater().inflate(R.layout.line_number, null);
                TextView lineNumber = lineNumberView.findViewById(R.id.stop_line_small_number);
                lineNumber.setText(String.valueOf(part.getLineNumber()));
                viewHolder.partsContainer.addView(lineNumberView);
            }

            if (i < route.getParts().size() - 1) {
                ImageView arrowIcon = new ImageView(context);
                arrowIcon.setImageResource(R.drawable.ic_keyboard_arrow_right_primary_24dp);
                viewHolder.partsContainer.addView(arrowIcon);
            }
        }

        viewHolder.departureStop.setText(context.getString(R.string.departure_stop, route.getDepartureStop()));
        viewHolder.nextDeparture.setText(context.getString(R.string.next_departure, route.getNextDeparture()));
        viewHolder.totalPrice.setText(context.getString(R.string.total_price, route.getTotalPrice()));
        viewHolder.itemView.setBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    private class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private final TextView totalDuration;
        private final LinearLayout partsContainer;
        private final TextView departureStop;
        private final TextView nextDeparture;
        private final TextView totalPrice;

        private RecyclerViewViewHolder(@NonNull View itemView, final OnItemClickListener onItemClickListener) {
            super(itemView);
            totalDuration = itemView.findViewById(R.id.total_duration);
            partsContainer = itemView.findViewById(R.id.parts_container);
            departureStop = itemView.findViewById(R.id.departure_stop);
            nextDeparture = itemView.findViewById(R.id.next_departure);
            totalPrice = itemView.findViewById(R.id.total_price);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    notifyItemChanged(selectedPosition);
                    selectedPosition = position;
                    notifyItemChanged(selectedPosition);
                    onItemClickListener.onItemClick(position);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
