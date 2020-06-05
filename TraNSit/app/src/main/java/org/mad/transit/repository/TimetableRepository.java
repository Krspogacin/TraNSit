package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Timetable;
import org.mad.transit.model.TimetableDay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableRepository {

    private static final String LINE = "line";
    private static final String DIRECTION = "direction";
    private static final String DAY = "day";

    public static Map<String, Timetable> retrieveLineTimetables(ContentResolver contentResolver, Long lineId, LineDirection direction){
        Map<String, Timetable> timetables = new HashMap<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_TIMETABLE,
                null,
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[] { lineId.toString(), direction.toString()},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long timetableId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
                String dayType = cursor.getString(cursor.getColumnIndex(DAY));
                List<DepartureTime> departureTimes = DepartureTimeRepository.findByTimetableId(contentResolver, timetableId);
                Timetable timetable = Timetable.builder()
                        .id(timetableId)
                        .day(TimetableDay.valueOf(dayType))
                        .departureTimes(departureTimes)
                        .direction(direction)
                        .build();

                timetables.put(dayType, timetable);
            }
            cursor.close();
        }else{
            Log.e("Retrieve line", "Cursor is null");
        }
        return timetables;
    }
}
