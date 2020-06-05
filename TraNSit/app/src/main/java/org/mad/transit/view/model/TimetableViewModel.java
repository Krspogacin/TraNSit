package org.mad.transit.view.model;

import org.mad.transit.model.Timetable;

import java.util.HashMap;
import java.util.Map;

import androidx.lifecycle.ViewModel;

public class TimetableViewModel extends ViewModel {
    public static Map<String, Timetable> timetableMap = new HashMap<>();
}
