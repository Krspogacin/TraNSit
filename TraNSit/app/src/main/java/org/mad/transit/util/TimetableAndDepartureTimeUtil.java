package org.mad.transit.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.dto.LineTimetableDto;
import org.mad.transit.sync.InitializeDatabaseTask;

import java.util.List;

public class TimetableAndDepartureTimeUtil {

    public static void retrieveTimetablesAndDepartureTimes(ContentResolver contentResolver, List<LineTimetableDto> lineTimetables) {
        Long lineId;
        DBContentProvider.database.beginTransaction();
        for (LineTimetableDto lineTimetable : lineTimetables) {
            lineId = InitializeDatabaseTask.lineIdsMap.get(lineTimetable.getName());
            ContentValues entry;
            if (lineTimetable.getTimeTable().getWorkday() != null) {
                if (!lineTimetable.getTimeTable().getWorkday().isEmpty()) {
                    entry = new ContentValues();
                    entry.put("day", "WORKDAY");
                    entry.put("line", lineId);
                    entry.put("direction", lineTimetable.getDirection());
                    Uri uri = contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                    long timetableId = Long.parseLong(uri.getLastPathSegment());

                    ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getWorkday().size()];
                    for (int i = 0; i < lineTimetable.getTimeTable().getWorkday().size(); i++) {
                        entry = new ContentValues();
                        entry.put("formatted_value", lineTimetable.getTimeTable().getWorkday().get(i));
                        entry.put("timetable", timetableId);
                        contentValues[i] = entry;
                    }
                    contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                }
            }
            if (lineTimetable.getTimeTable().getSaturday() != null) {
                if (!lineTimetable.getTimeTable().getSaturday().isEmpty()) {
                    entry = new ContentValues();
                    entry.put("day", "SATURDAY");
                    entry.put("line", lineId);
                    entry.put("direction", lineTimetable.getDirection());
                    Uri uri = contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                    long timetableId = Long.parseLong(uri.getLastPathSegment());

                    ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSaturday().size()];
                    for (int i = 0; i < lineTimetable.getTimeTable().getSaturday().size(); i++) {
                        entry = new ContentValues();
                        entry.put("formatted_value", lineTimetable.getTimeTable().getSaturday().get(i));
                        entry.put("timetable", timetableId);
                        contentValues[i] = entry;
                    }
                    contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                }
            }
            if (lineTimetable.getTimeTable().getSunday() != null) {
                if (!lineTimetable.getTimeTable().getSunday().isEmpty()) {
                    entry = new ContentValues();
                    entry.put("day", "SUNDAY");
                    entry.put("line", lineId);
                    entry.put("direction", lineTimetable.getDirection());
                    Uri uri = contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                    long timetableId = Long.parseLong(uri.getLastPathSegment());

                    ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSunday().size()];
                    for (int i = 0; i < lineTimetable.getTimeTable().getSunday().size(); i++) {
                        entry = new ContentValues();
                        entry.put("formatted_value", lineTimetable.getTimeTable().getSunday().get(i));
                        entry.put("timetable", timetableId);
                        contentValues[i] = entry;
                    }
                    contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                }
            }
        }
        Log.i("TraNSit", "GET TIMETABLE FINISHED");
        DBContentProvider.database.setTransactionSuccessful();
        DBContentProvider.database.endTransaction();
    }
}