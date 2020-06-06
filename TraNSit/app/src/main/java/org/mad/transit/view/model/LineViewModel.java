package org.mad.transit.view.model;

import androidx.lifecycle.ViewModel;

import org.mad.transit.model.Line;
import org.mad.transit.repository.LineRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LineViewModel extends ViewModel {

    private List<Line> lines;
    private final LineRepository lineRepository;

    @Inject
    public LineViewModel(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    public List<Line> getLines() {
        if (this.lines == null) {
            this.lines = this.lineRepository.findAll();
        }
        return this.lines;
    }

    public List<Line> getLinesByNumbers(Set<String> lineNumbers) {
        List<Line> lines = new ArrayList<>();
        for (Line line : this.getLines()) {
            if (lineNumbers.contains(line.getNumber())) {
                lines.add(line);
            }
        }
        return lines;
    }
}