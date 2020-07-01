package org.mad.transit.adapters;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.islamkhsh.CardSliderAdapter;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
import org.mad.transit.dto.ActionDto;
import org.mad.transit.dto.ActionType;
import org.mad.transit.model.Location;
import org.mad.transit.model.NavigationStop;
import org.mad.transit.util.Constants;
import org.mad.transit.util.LocationsUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NavigationCardsAdapter extends CardSliderAdapter<NavigationCardsAdapter.NavigationCardsViewHolder> {

    private static final int WALK_ACTION_TYPE = 1;
    private static final int BUS_ACTION_TYPE = 2;
    private final Activity context;
    private final List<List<ActionDto>> routeParts;
    private final Map<Integer, List<NavigationStop>> navigationStops;
    private final Map<Long, String> startTimes;

    public NavigationCardsAdapter(Activity context, List<List<ActionDto>> routeParts, Map<Integer, List<NavigationStop>> navigationStops, Map<Long, String> startTimes) {
        this.context = context;
        this.routeParts = routeParts;
        this.navigationStops = navigationStops;
        this.startTimes = startTimes;
    }

    @NonNull
    @Override
    public NavigationCardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView;
        if (viewType == WALK_ACTION_TYPE) {
            rootView = LayoutInflater.from(this.context).inflate(R.layout.navigation_bottom_sheet_walk_action, parent, false);
        } else {
            rootView = LayoutInflater.from(this.context).inflate(R.layout.navigation_bottom_sheet_bus_action, parent, false);
        }
        return new NavigationCardsViewHolder(rootView);
    }

    @Override
    public int getItemCount() {
        return this.routeParts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return this.routeParts.get(position).get(0).getType() == ActionType.WALK ? WALK_ACTION_TYPE : BUS_ACTION_TYPE;
    }

    @Override
    public void bindVH(@NotNull NavigationCardsViewHolder navigationCardsViewHolder, int position) {
        List<ActionDto> actions = this.routeParts.get(position);

        navigationCardsViewHolder.itemView.setTag(position);

        ActionDto firstAction = actions.get(0);
        if (firstAction.getType() == ActionType.WALK) {
            TextView navigationWalkMessageTextView = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_walk_message);
            StringBuilder walkTimeBuilder = new StringBuilder();
            walkTimeBuilder.append(firstAction.getDuration());
            if (firstAction.getDuration() == 1) {
                walkTimeBuilder.append(" minut");
            } else {
                walkTimeBuilder.append(" minuta");
            }
            navigationWalkMessageTextView.setText(this.context.getString(R.string.navigation_walk_message, walkTimeBuilder.toString()));

            TextView navigationWalkStopTitleTextView = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_ride_destination_title);
            ImageView navigationWalkBusIcon = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_walk_destination_icon);

            if (firstAction.getStop() != null) {
                navigationWalkStopTitleTextView.setText(firstAction.getStop().getTitle());
                navigationWalkBusIcon.setImageResource(R.drawable.bus_icon);
            } else {
                Location endLocation = firstAction.getEndLocation();
                if (endLocation.getName() == null) {
                    try {
                        endLocation.setName(LocationsUtil.retrieveAddressFromLatAndLng(this.context, endLocation.getLatitude(), endLocation.getLongitude()));
                        navigationWalkStopTitleTextView.setText(endLocation.getName());
                    } catch (IOException e) {
                        navigationWalkStopTitleTextView.setText(String.format("%s, %s", endLocation.getLatitude(), endLocation.getLongitude()));
                    }
                } else {
                    navigationWalkStopTitleTextView.setText(endLocation.getName());
                }
                navigationWalkBusIcon.setImageResource(R.drawable.finish_icon);
            }
        } else {
            ImageView lineIcon = navigationCardsViewHolder.itemView.findViewById(R.id.stop_line_small_icon);
            ImageViewCompat.setImageTintList(lineIcon, ColorStateList.valueOf(Constants.getLineColor(firstAction.getLine().getId())));
            TextView lineNumber = navigationCardsViewHolder.itemView.findViewById(R.id.stop_line_small_number);
            lineNumber.setText(String.valueOf(firstAction.getLine().getNumber()));

            TextView navigationWaitBusArriveTime = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_wait_bus_arrive_time);

            String startTime = this.startTimes.get(firstAction.getLine().getId());
            navigationWaitBusArriveTime.setText(startTime);

            StringBuilder numberOfStopsBuilder = new StringBuilder();

            int numberOfStops = actions.size();
            numberOfStopsBuilder.append(numberOfStops).append(" ");

            int lastDigit = numberOfStops % 10;
            if (lastDigit == 1 && numberOfStops != 11) {
                numberOfStopsBuilder.append("stanicu");
            } else if ((lastDigit == 2 || lastDigit == 3 || lastDigit == 4) && numberOfStops != 12 && numberOfStops != 13 && numberOfStops != 14) {
                numberOfStopsBuilder.append("stanice");
            } else {
                numberOfStopsBuilder.append("stanica");
            }

            TextView navigationRideStopsMessage = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_ride_stops_message);
            navigationRideStopsMessage.setText(this.context.getString(R.string.navigation_ride_stops_message, numberOfStopsBuilder.toString()));

            TextView navigationDestinationTitle = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_destination_title);
            navigationDestinationTitle.setText(actions.get(actions.size() - 1).getStop().getTitle());

            RecyclerView recyclerView = navigationCardsViewHolder.itemView.findViewById(R.id.navigation_bus_action_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.context));

            int counter = 0;
            String arriveTime = startTime;
            for (NavigationStop navigationStop : this.navigationStops.get(position)) {
                navigationStop.setArriveTime(arriveTime, actions.get(counter++).getDuration());
                arriveTime = navigationStop.getArriveTime();
            }

            NavigationAdapter navigationAdapter = new NavigationAdapter(this.context, this.navigationStops.get(position));
            recyclerView.setAdapter(navigationAdapter);
        }
    }

    public static class NavigationCardsViewHolder extends RecyclerView.ViewHolder {
        private NavigationCardsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public List<List<ActionDto>> getRouteParts() {
        return this.routeParts;
    }
}
