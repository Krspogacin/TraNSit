package org.mad.transit.model;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModel;

public class TimetableViewModel extends ViewModel {
    public static List<DepartureTime> getTimeTables(){
        List<DepartureTime> departureTimes = new ArrayList<>();


        DepartureTime d1 = DepartureTime.builder()
                .formattedValue("05:00")
                .build();
        DepartureTime d2 = DepartureTime.builder()
                .formattedValue("05:22")
                .build();
        DepartureTime d3 = DepartureTime.builder()
                .formattedValue("05:45")
                .build();
        DepartureTime d4 = DepartureTime.builder()
                .formattedValue("06:07")
                .build();
        DepartureTime d5 = DepartureTime.builder()
                .formattedValue("06:30")
                .build();
        DepartureTime d6 = DepartureTime.builder()
                .formattedValue("06:52")
                .build();
        DepartureTime d7 = DepartureTime.builder()
                .formattedValue("07:07")
                .build();
        DepartureTime d8 = DepartureTime.builder()
                .formattedValue("07:22")
                .build();
        DepartureTime d9 = DepartureTime.builder()
                .formattedValue("07:37")
                .build();
        DepartureTime d10 = DepartureTime.builder()
                .formattedValue("07:52")
                .build();
        DepartureTime d11 = DepartureTime.builder()
                .formattedValue("08:07")
                .build();
        DepartureTime d12 = DepartureTime.builder()
                .formattedValue("08:22")
                .build();
        DepartureTime d13 = DepartureTime.builder()
                .formattedValue("08:37")
                .build();
        DepartureTime d14 = DepartureTime.builder()
                .formattedValue("08:52")
                .build();

        departureTimes.add(d1);
        departureTimes.add(d2);
        departureTimes.add(d3);
        departureTimes.add(d4);
        departureTimes.add(d5);
        departureTimes.add(d6);
        departureTimes.add(d7);
        departureTimes.add(d8);
        departureTimes.add(d9);
        departureTimes.add(d10);
        departureTimes.add(d11);
        departureTimes.add(d12);
        departureTimes.add(d13);
        departureTimes.add(d14);

        return departureTimes;
    }
}
