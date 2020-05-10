package org.mad.transit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static org.mad.transit.model.TravelType.BUS;
import static org.mad.transit.model.TravelType.WALK;

public class RoutesViewModel extends ViewModel implements Serializable {
    private static final long serialVersionUID = 5210076089726438772L;
    private final List<Route> routes;

    public RoutesViewModel() {
        routes = new ArrayList<>();
        populateList();
    }

    public MutableLiveData<List<Route>> getRoutesLiveData() {
        return new MutableLiveData<>(routes);
    }

    private void populateList() {

        RoutePart part11 = RoutePart.builder()
                .travelType(WALK)
                .duration(5)
                .build();

        Stop stop1 = Stop.builder()
                .name("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .latitude(45.261530)
                .longitude(19.836049)
                .build();

        RoutePart part12 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(1)
                .stops(Collections.singletonList(stop1))
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

        Stop stop2 = Stop.builder()
                .name("Kisačka - Bulevar Kralja Petra I")
                .latitude(45.262605)
                .longitude(19.839737)
                .build();

        RoutePart part22 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(4)
                .stops(Collections.singletonList(stop2))
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

        Stop stop3 = Stop.builder()
                .name("Vojvode Bojovića - Socijalno")
                .latitude(45.258915)
                .longitude(19.837543)
                .build();

        RoutePart part32 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(3)
                .stops(Collections.singletonList(stop3))
                .build();

        Stop stop4 = Stop.builder()
                .name("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .latitude(45.258875)
                .longitude(19.837066)
                .build();

        RoutePart part33 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(9)
                .stops(Collections.singletonList(stop4))
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
