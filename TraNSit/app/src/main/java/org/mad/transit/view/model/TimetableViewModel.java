package org.mad.transit.view.model;

import androidx.lifecycle.ViewModel;

import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Timetable;
import org.mad.transit.repository.TimetableRepository;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Setter;

@Singleton
public class TimetableViewModel extends ViewModel {

    private Map<String, Timetable> timetableMap;
    private final TimetableRepository timetableRepository;

    @Setter
    private Long lineId;

    @Setter
    private LineDirection lineDirection;

    @Inject
    public TimetableViewModel(TimetableRepository timetableRepository) {
        this.timetableRepository = timetableRepository;
    }

    public Map<String, Timetable> getTimetableMap() {
        if (this.timetableMap == null) {
            this.timetableMap = new HashMap<>();
            this.loadTimetableData();
        }
        return this.timetableMap;
    }


    public void loadTimetableData() {
        if (this.lineId == null || this.lineDirection == null) {
            return;
        }

        this.timetableMap = this.timetableRepository.findAllByLineIdAndLineDirection(this.lineId, this.lineDirection);
    }
}
