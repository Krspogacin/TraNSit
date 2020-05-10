package org.mad.transit.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static org.mad.transit.model.TravelType.BUS;
import static org.mad.transit.model.TravelType.WALK;

public class RoutesViewModel extends ViewModel {
    private final MutableLiveData<List<Route>> routesLiveData;
    private final List<Route> routes;

    public RoutesViewModel() {
        routes = new ArrayList<>();
        populateList();
        routesLiveData = new MutableLiveData<>();
        routesLiveData.setValue(routes);
    }

    public MutableLiveData<List<Route>> getRoutesLiveData() {
        return routesLiveData;
    }

    private void populateList() {

        RoutePart part11 = RoutePart.builder()
                .travelType(WALK)
                .duration(5)
                .build();

        RoutePart part12 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(1)
                .build();

        RoutePart part13 = RoutePart.builder()
                .travelType(WALK)
                .duration(4)
                .build();

        Route route1 = Route.builder()
                .totalDuration(14)
                .parts(Arrays.asList(part11, part12, part13))
                .departureStop("Kisačka - Bulevar Kralja Petra I")
                .nextDeparture("12:38")
                .totalPrice(65)
                .build();

        RoutePart part21 = RoutePart.builder()
                .travelType(WALK)
                .duration(9)
                .build();

        RoutePart part22 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(4)
                .build();

        Route route2 = Route.builder()
                .totalDuration(17)
                .parts(Arrays.asList(part21, part22))
                .departureStop("Bulеvar Oslobođenja - Bulevar Kralja Petra I")
                .nextDeparture("12:41")
                .totalPrice(65)
                .build();

        RoutePart part31 = RoutePart.builder()
                .travelType(WALK)
                .duration(3)
                .build();

        RoutePart part32 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(3)
                .build();

        RoutePart part33 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(9)
                .build();

        RoutePart part34 = RoutePart.builder()
                .travelType(WALK)
                .duration(4)
                .build();

        Route route3 = Route.builder()
                .totalDuration(27)
                .parts(Arrays.asList(part31, part32, part33, part34))
                .departureStop("Vojvode Bojovića - Oš Ivo Lola Ribar")
                .nextDeparture("12:34")
                .totalPrice(130)
                .build();

        routes.add(route1);
        routes.add(route2);
        routes.add(route3);
    }
}
