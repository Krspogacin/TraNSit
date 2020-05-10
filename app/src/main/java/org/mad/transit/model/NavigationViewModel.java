package org.mad.transit.model;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NavigationViewModel extends ViewModel {

    public static List<NavigationStop> getNavigationStops() {

        NavigationStop stop1 = NavigationStop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.261530)
                .longitude(19.836049)
                .passed(true)
                .build();
        NavigationStop stop2 = NavigationStop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258915)
                .longitude(19.837543)
                .passed(true)
                .build();
        NavigationStop stop3 = NavigationStop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .lines(Collections.singletonList(LinesFragmentViewModel.getLines().get(2)))
                .latitude(45.258875)
                .longitude(19.837066)
                .minutes(4)
                .build();

        NavigationStop stop4 = NavigationStop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .lines(Arrays.asList(LinesFragmentViewModel.getLines().get(0), LinesFragmentViewModel.getLines().get(3)))
                .latitude(45.262605)
                .longitude(19.839737)
                .minutes(6)
                .build();

        List<NavigationStop> navigationStops = new ArrayList<>();
        navigationStops.add(stop1);
        navigationStops.add(stop2);
        navigationStops.add(stop3);
        navigationStops.add(stop4);

        return navigationStops;
    }
}
