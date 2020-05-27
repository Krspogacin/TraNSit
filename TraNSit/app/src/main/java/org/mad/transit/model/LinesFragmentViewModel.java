package org.mad.transit.model;

import androidx.lifecycle.ViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class LinesFragmentViewModel extends ViewModel {
    public static ArrayList<Line> getLines() {
        ArrayList<Line> lines = new ArrayList<>();

        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        Line line1 = Line.builder()
                .number("1")
                .title("Klisa - Centar - Liman 1")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 5 * 60 * 1000)))
                .build();

        Line line2 = Line.builder()
                .number("2")
                .title("Centar - Novo naselje")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 8 * 60 * 1000)))
                .build();

        Line line3 = Line.builder()
                .number("3")
                .title("Petrovaradin - Centar - Detelinara")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 6 * 60 * 1000)))
                .build();

        Line line4 = Line.builder()
                .number("8")
                .title("Novo naselje - Centar - Liman 1 (Štrand)")
                .type(LineType.CITY)
                .nextDeparture(dateFormat.format(new Date(new Date().getTime() + 4 * 60 * 1000)))
                .build();

        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);

        return lines;
    }

    public static List<Line> getLinesByNumbers(Set<String> lineNumbers) {
        List<Line> lines = new ArrayList<>();
        ArrayList<Line> allLines = getLines();
        for (Line line : allLines) {
            if (lineNumbers.contains(line.getNumber())) {
                lines.add(line);
            }
        }
        return lines;
    }
}