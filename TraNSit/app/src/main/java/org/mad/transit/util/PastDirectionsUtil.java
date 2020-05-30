package org.mad.transit.util;

import android.content.ContentResolver;
import android.database.Cursor;

import org.mad.transit.model.Location;
import org.mad.transit.model.PastDirection;

import java.util.ArrayList;
import java.util.List;

public class PastDirectionsUtil {

    public static List<PastDirection> retrievePastDirectionsFromCursor(ContentResolver contentResolver, Cursor cursor) {
        List<PastDirection> pastDirections = new ArrayList<>();
        while (cursor.moveToNext()) {
            long startLocationId = cursor.getLong(cursor.getColumnIndex("start_location"));
            long endLocationId = cursor.getLong(cursor.getColumnIndex("end_location"));
            String date = cursor.getString(cursor.getColumnIndex("date"));

            Location startLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(startLocationId));
            Location endLocation = LocationsUtil.findLocationById(contentResolver, String.valueOf(endLocationId));

            PastDirection pastDirection = new PastDirection(startLocation, endLocation, date);
            pastDirections.add(pastDirection);
        }
        cursor.close();
        return pastDirections;
    }
}