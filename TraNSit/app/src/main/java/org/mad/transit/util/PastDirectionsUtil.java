package org.mad.transit.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.Location;
import org.mad.transit.model.PastDirection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastDirectionsUtil {

    private static final DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag("sr-RS"));

    public static List<PastDirection> getPastDirectionsFromCursor(ContentResolver contentResolver, Cursor cursor) {
        List<PastDirection> pastDirections = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex("id"));
            long startLocationId = cursor.getLong(cursor.getColumnIndex("start_location"));
            long endLocationId = cursor.getLong(cursor.getColumnIndex("end_location"));
            String date = cursor.getString(cursor.getColumnIndex("date"));

            Location startLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(startLocationId));
            Location endLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(endLocationId));

            PastDirection pastDirection = new PastDirection(id, startLocation, endLocation, date);
            pastDirections.add(pastDirection);
        }
        cursor.close();
        return pastDirections;
    }

    public static PastDirection findPastDirectionByStartLocationAndEndLocation(ContentResolver contentResolver, Long startLocationId, Long endLocationId) {
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS,
                null,
                "start_location = ? and end_location = ?",
                new String[]{startLocationId.toString(), endLocationId.toString()},
                null);

        if (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex("id"));
            String date = cursor.getString(cursor.getColumnIndex("date"));

            Location startLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(startLocationId));
            Location endLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(endLocationId));

            return new PastDirection(id, startLocation, endLocation, date);
        } else {
            return null;
        }
    }

    public static void insertPastDirection(ContentResolver contentResolver, Long startLocationId, Long endLocationId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("start_location", startLocationId);
        contentValues.put("end_location", endLocationId);
        contentValues.put("date", dateFormat.format(new Date()));
        contentResolver.insert(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues);
    }

    public static void updatePastDirectionDate(ContentResolver contentResolver, PastDirection pastDirection) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", dateFormat.format(new Date()));
        contentResolver.update(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues, "id = ?", new String[]{pastDirection.getId().toString()});
    }
}