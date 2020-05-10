package org.mad.transit.model;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModel;

public class TimetableViewModel extends ViewModel {
    public static List<DepartureTime> getTimeTables(){
        List<DepartureTime> departureTimes = new ArrayList<>();


        DepartureTime d1 = new DepartureTime(5, 0, "05:00");
        DepartureTime d2 = new DepartureTime(5, 22, "05:22");
        DepartureTime d3 = new DepartureTime(5, 45, "05:45");
        DepartureTime d4 = new DepartureTime(6, 7, "06:07");
        DepartureTime d5 = new DepartureTime(6, 30, "06:30");
        DepartureTime d6 = new DepartureTime(6, 52, "06:52");
        DepartureTime d7 = new DepartureTime(7, 7, "07:07");
        DepartureTime d8 = new DepartureTime(7, 22, "07:22");
        DepartureTime d9 = new DepartureTime(7, 37, "07:37");
        DepartureTime d10 = new DepartureTime(7, 52, "07:52");
        DepartureTime d11 = new DepartureTime(8, 7, "08:07");
        DepartureTime d12 = new DepartureTime(8, 22, "08:22");
        DepartureTime d13 = new DepartureTime(8, 37,"08:37");
        DepartureTime d14 = new DepartureTime(8, 52, "08:52");

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
