package org.mad.transit.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.ActionType;
import org.mad.transit.dto.RouteDto;
import org.mad.transit.model.Stop;
import org.mad.transit.util.Constants;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

public class RoutesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Activity context;
    private final List<RouteDto> routes;
    private final OnItemClickListener onItemClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public RoutesAdapter(Activity context, List<RouteDto> routes, OnItemClickListener onItemClickListener) {
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
        RouteDto route = routes.get(position);
        RecyclerViewViewHolder viewHolder = (RecyclerViewViewHolder) holder;
        viewHolder.totalDuration.setText(String.valueOf(route.getTotalDuration()));
        viewHolder.partsContainer.removeAllViews();

        Stop firstStop = null;
        for (int i = 0; i < route.getActions().size(); i++) {
            ActionDto action = route.getActions().get(i);

            boolean shouldShowNextAction = true;
            if (ActionType.WALK == action.getType()) {
                View walkDurationView = context.getLayoutInflater().inflate(R.layout.walk_duration, null);
                TextView walkDuration = walkDurationView.findViewById(R.id.walk_duration_number);
                walkDuration.setText(String.valueOf(action.getDuration()));
                viewHolder.partsContainer.addView(walkDurationView);
            } else {
                // don't show the line again if it is the same as the previous one
                if (!(i > 0 && isPreviousActionWithSameLine(route.getActions().get(i - 1), action))) {
                    View lineNumberView = context.getLayoutInflater().inflate(R.layout.line_number, null);
                    TextView lineNumber = lineNumberView.findViewById(R.id.stop_line_small_number);
                    lineNumber.setText(String.valueOf(action.getLine().getNumber()));
                    ImageView lineIcon = lineNumberView.findViewById(R.id.stop_line_small_icon);
                    ImageViewCompat.setImageTintList(lineIcon, ColorStateList.valueOf(Constants.getLineColor(action.getLine().getId())));
                    viewHolder.partsContainer.addView(lineNumberView);
                } else {
                    shouldShowNextAction = false;
                }
            }

            if (i < route.getActions().size() - 1 && shouldShowNextAction) {
                ImageView arrowIcon = new ImageView(context);
                arrowIcon.setImageResource(R.drawable.ic_keyboard_arrow_right_primary_24dp);
                viewHolder.partsContainer.addView(arrowIcon);
            }

            if (firstStop == null && action.getStop() != null) {
                firstStop = action.getStop();
            }
        }

        viewHolder.departureStop.setText(context.getString(R.string.departure_stop, firstStop != null ? firstStop.getTitle() : "/"));
        viewHolder.nextDeparture.setText(context.getString(R.string.departure_time, route.getNextDeparture() != null ? route.getNextDeparture() : "/"));
        viewHolder.totalPrice.setText(context.getString(R.string.total_price, route.getTotalPrice()));
        viewHolder.itemView.setBackgroundColor(selectedPosition == position ? Color.LTGRAY : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return routes != null ? routes.size() : 0;
    }

    private boolean isPreviousActionWithSameLine(ActionDto previousAction, ActionDto currentAction) {
        return ActionType.BUS == previousAction.getType() && previousAction.getLine().getId().equals(currentAction.getLine().getId());
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                notifyItemChanged(selectedPosition);
                selectedPosition = position;
                notifyItemChanged(selectedPosition);
                onItemClickListener.onItemClick(position);
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
