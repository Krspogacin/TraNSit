package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.DepartureTime;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DepartureTimeRepository {

    private final ContentResolver contentResolver;

    @Inject
    public DepartureTimeRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<DepartureTime> findAllByTimetableId(Long timetableId) {
        List<DepartureTime> departureTimes = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_DEPARTURE_TIME,
                null,
                Constants.TIMETABLE + " = ?",
                new String[]{timetableId.toString()},
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                String formattedValue = cursor.getString(cursor.getColumnIndex(Constants.FORMATTED_VALUE));
                DepartureTime departureTime = DepartureTime.builder()
                        .id(id)
                        .formattedValue(formattedValue)
                        .build();
                departureTimes.add(departureTime);
            }
            cursor.close();
        } else {
            Log.e("Find departure time", "Cursor is null");
        }
        return departureTimes;
    }

    public void deleteAll() {
        this.contentResolver.delete(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, null, null);
    }
}
