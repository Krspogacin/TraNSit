package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;

import java.util.ArrayList;
import java.util.List;

public class DepartureTimeRepository {

    private static final String TIMETABLE = "timetable";
    private static final String FORMATTED_VALUE = "formatted_value";

    public static List<DepartureTime> findByTimetableId(ContentResolver contentResolver, Long timetableId){
        List<DepartureTime> departureTimes = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_DEPARTURE_TIME,
                null,
                TIMETABLE + " = ?",
                new String[] { timetableId.toString() },
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
                String formattedValue = cursor.getString(cursor.getColumnIndex(FORMATTED_VALUE));
                DepartureTime departureTime = DepartureTime.builder()
                        .id(id)
                        .formattedValue(formattedValue)
                        .build();
                departureTimes.add(departureTime);
            }
            cursor.close();
        }else{
            Log.e("Find departure time", "Cursor is null");
        }
        return departureTimes;
    }
}
