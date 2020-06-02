package org.mad.transit.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.lifecycle.ViewModel;

public class LinesFragmentViewModel extends ViewModel {
    private static List<Line> lines = new ArrayList<>();

    public static void setLines(List<Line> lines) {
        LinesFragmentViewModel.lines = lines;
    }

    public static List<Line> getLines() {
        return lines;
    }

    public static List<Line> getLinesByNumbers(Set<String> lineNumbers) {
        List<Line> lines = new ArrayList<>();
        List<Line> allLines = getLines();
        for (Line line : allLines) {
            if (lineNumbers.contains(line.getNumber())) {
                lines.add(line);
            }
        }
        return lines;
    }
}