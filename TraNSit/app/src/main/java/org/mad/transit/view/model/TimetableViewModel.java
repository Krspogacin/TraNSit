package org.mad.transit.view.model;

import androidx.lifecycle.ViewModel;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Timetable;
import org.mad.transit.repository.TimetableRepository;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TimetableViewModel extends ViewModel {

    private Map<String, Timetable> timetableMap;
    private final TimetableRepository timetableRepository;

    @Inject
    public TimetableViewModel(TimetableRepository timetableRepository) {
        this.timetableRepository = timetableRepository;
    }

    public Map<String, Timetable> getTimetableMap() {
        if (this.timetableMap == null) {
            this.timetableMap = new HashMap<>();
        }
        return this.timetableMap;
    }


    public void loadTimetableData(Long lineId, LineDirection lineDirection) {
        if (lineId == null || lineDirection == null) {
            return;
        }

        this.timetableMap = this.findAllByLineIdAndLineDirection(lineId, lineDirection);
    }

    public Map<String, Timetable> findAllByLineIdAndLineDirection(Long lineId, LineDirection lineDirection) {
        return this.timetableRepository.findAllByLineIdAndLineDirection(lineId, lineDirection);
    }
}
