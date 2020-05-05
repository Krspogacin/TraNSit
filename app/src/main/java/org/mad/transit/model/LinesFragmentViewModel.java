package org.mad.transit.model;

import java.util.ArrayList;

import androidx.lifecycle.ViewModel;

public class LinesFragmentViewModel extends ViewModel {
    public static ArrayList<Line> getLines() {
        ArrayList<Line> lines = new ArrayList<>();
        Line line1 = new Line("1", "Klisa - Centar - Liman 1", LineType.CITY);
        Line line2 = new Line("2", "Centar - Novo naselje", LineType.CITY);
        Line line3 = new Line("3", "Petrovaradin - Centar - Detelinara", LineType.CITY);

        lines.add(line1);
        lines.add(line2);
        lines.add(line3);

        return lines;
    }
}
