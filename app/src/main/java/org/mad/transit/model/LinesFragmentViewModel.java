package org.mad.transit.model;

import androidx.lifecycle.ViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LinesFragmentViewModel extends ViewModel {
    public static ArrayList<Line> getLines() {
        ArrayList<Line> lines = new ArrayList<>();

        ArrayList<Stop> lineStops = new ArrayList<>();
        Stop stop1 = Stop.builder()
                .title("Bulevar Kralja Petra I - Dom Zdravlja Zov")
                .latitude(45.261530)
                .longitude(19.836049)
                .build();
        Stop stop2 = Stop.builder()
                .title("Vojvode Bojovića - Socijalno")
                .latitude(45.258915)
                .longitude(19.837543)
                .build();
        Stop stop3 = Stop.builder()
                .title("Vojvode Bojovića - OŠ Ivo Lola Ribar")
                .latitude(45.258875)
                .longitude(19.837066)
                .build();
        Stop stop4 = Stop.builder()
                .title("Kisačka - Bulevar Kralja Petra I")
                .latitude(45.262605)
                .longitude(19.839737)
                .build();
        lineStops.add(stop1);
        lineStops.add(stop2);
        lineStops.add(stop3);
        lineStops.add(stop4);

        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        Line line1 = Line.builder()
                .number("1")
                .title("Klisa - Centar - Liman 1")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 5 * 60 * 1000)))
                .stops(lineStops)
                .build();

        Line line2 = Line.builder()
                .number("2")
                .title("Centar - Novo naselje")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 8 * 60 * 1000)))
                .stops(lineStops)
                .build();

        Line line3 = Line.builder()
                .number("3")
                .title("Petrovaradin - Centar - Detelinara")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 6 * 60 * 1000)))
                .stops(lineStops)
                .build();

        Line line4 = Line.builder()
                .number("8")
                .title("Novo naselje - Centar - Liman 1 (Štrand)")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 4 * 60 * 1000)))
                .stops(lineStops)
                .build();

        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);

        return lines;
    }
}