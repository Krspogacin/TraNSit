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

        Stop stop11 = Stop.builder()
                .title("Bulevar Kralja Petra I - Sajam")
                .location(new Location(45.30263703365396, 19.824762046337128))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop12 = Stop.builder()
                .title("Bulevar Kralja Petra I - Mašinska Škola")
                .location(new Location(45.302335182632184, 19.82469767332077))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();


        Stop stop13 = Stop.builder()
                .title("Bulevar Kralja Petra I - Bulevar Oslobođenja")
                .location(new Location(45.302342728927314, 19.824139773845673))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop14 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.30091646130248, 19.823394119739532))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop15 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.29973919750302, 19.8227396607399))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop16 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.29870529823723, 19.822353422641754))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop17 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.297233657279136, 19.82220321893692))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop18 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.29658084021481, 19.82224613428116))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop19 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.295180843019764, 19.822648465633392))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop110 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.29299209890505, 19.82341557741165))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop111 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.290610800313104, 19.82423633337021))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        Stop stop112 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .location(new Location(45.28883325235971, 19.824853241443634))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(0)))
                .build();

        RoutePart part12 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(8)
                .stops(Arrays.asList(stop11, stop12, stop13, stop14, stop15, stop16, stop17, stop18, stop19, stop110, stop111, stop112))
                .build();

        RoutePart part13 = RoutePart.builder()
                .travelType(WALK)
                .duration(4)
                .build();

        Route route1 = Route.builder()
                .totalDuration(14)
                .parts(Arrays.asList(part11, part12, part13))
                .departureStop("Bulevar Kralja Petra I - Sajam")
                .nextDeparture("12:38")
                .totalPrice(65)
                .build();

        RoutePart part21 = RoutePart.builder()
                .travelType(WALK)
                .duration(9)
                .build();

        Stop stop21 = Stop.builder()
                .title("Kisačka - Bulevar Jaše Tomića")
                .location(new Location(45.265770, 19.835368))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(1)))
                .build();

        Stop stop22 = Stop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .location(new Location(45.262605, 19.839737))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(1)))
                .build();

        RoutePart part22 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(4)
                .stops(Arrays.asList(stop21, stop22))
                .build();

        Route route2 = Route.builder()
                .totalDuration(17)
                .parts(Arrays.asList(part21, part22))
                .departureStop("Kisačka - Bulevar Jaše Tomića")
                .nextDeparture("12:41")
                .totalPrice(65)
                .build();

        RoutePart part31 = RoutePart.builder()
                .travelType(WALK)
                .duration(3)
                .build();

        Stop stop3 = Stop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .location(new Location(45.258915, 19.837543))
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .build();

        RoutePart part32 = RoutePart.builder()
                .travelType(BUS)
                .lineNumber(3)
                .stops(Collections.singletonList(stop3))
                .build();

        Stop stop4 = Stop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .location(new Location(45.258875, 19.837066))
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(2), LinesFragmentViewModel.getLines().get(3)))
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
