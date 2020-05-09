package org.mad.transit.model;

import androidx.lifecycle.ViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LinesFragmentViewModel extends ViewModel {
    public static ArrayList<Line> getLines() {
        ArrayList<Line> lines = new ArrayList<>();

        DateFormat dateFormat = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        Line line1 = new Line("1", "Klisa - Centar - Liman 1", LineType.CITY, dateFormat.format(new Date(new Date().getTime() + 5 * 60 * 1000)));
        Line line2 = new Line("2", "Centar - Novo naselje", LineType.CITY, dateFormat.format(new Date(new Date().getTime() + 8 * 60 * 1000)));
        Line line3 = new Line("3", "Petrovaradin - Centar - Detelinara", LineType.CITY, dateFormat.format(new Date(new Date().getTime() + 6 * 60 * 1000)));
        Line line4 = new Line("8", "Novo naselje - Centar - Liman 1 (Štrand)", LineType.CITY, dateFormat.format(new Date(new Date().getTime() + 4 * 60 * 1000)));

        lines.add(line1);
        lines.add(line2);
        lines.add(line3);
        lines.add(line4);

        return lines;
    }
}