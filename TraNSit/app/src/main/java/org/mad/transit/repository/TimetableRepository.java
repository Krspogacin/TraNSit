package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Timetable;
import org.mad.transit.model.TimetableDay;
import org.mad.transit.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TimetableRepository {

    private final ContentResolver contentResolver;
    private final DepartureTimeRepository departureTimeRepository;

    @Inject
    public TimetableRepository(ContentResolver contentResolver, DepartureTimeRepository departureTimeRepository) {
        this.contentResolver = contentResolver;
        this.departureTimeRepository = departureTimeRepository;
    }

    public Map<String, Timetable> findAllByLineIdAndLineDirection(Long lineId, LineDirection direction) {
        Map<String, Timetable> timetables = new HashMap<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_TIMETABLE,
                null,
                Constants.LINE + " = ? and " + Constants.DIRECTION + " = ?",
                new String[]{lineId.toString(), direction.toString()},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long timetableId = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                String dayType = cursor.getString(cursor.getColumnIndex(Constants.DAY));
                List<DepartureTime> departureTimes = this.departureTimeRepository.findAllByTimetableId(timetableId);
                Timetable timetable = Timetable.builder()
                        .id(timetableId)
                        .day(TimetableDay.valueOf(dayType))
                        .departureTimes(departureTimes)
                        .direction(direction)
                        .build();

                timetables.put(dayType, timetable);
            }
            cursor.close();
        } else {
            Log.e("Retrieve line", "Cursor is null");
        }
        return timetables;
    }

    public void deleteAll() {
        this.contentResolver.delete(DBContentProvider.CONTENT_URI_TIMETABLE, null, null);
    }
}
