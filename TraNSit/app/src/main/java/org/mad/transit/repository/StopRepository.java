package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;

import java.util.ArrayList;
import java.util.List;

public class StopRepository {

    private static final String TITLE = "title";
    private static final String ZONE = "zone";
    private static final String LOCATION = "location";
    private static final String LINE = "line";
    private static final String DIRECTION = "direction";
    private static final String STOP = "stop";

    public static Stop findById(ContentResolver contentResolver, Long id) {
        Stop stop = null;
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_STOP,
                null,
                DatabaseHelper.ID + " = ?",
                new String[]{id.toString()},
                null);

        if (cursor != null) {
            cursor.moveToFirst();

            String title = cursor.getString(cursor.getColumnIndex(TITLE));
            Zone zone = ZoneRepository.findZoneById(contentResolver, cursor.getLong(cursor.getColumnIndex(ZONE)));
            Location location = LocationRepository.findById(contentResolver, cursor.getString(cursor.getColumnIndex(LOCATION)));
            stop = Stop.builder()
                    .id(id)
                    .location(location)
                    .zone(zone)
                    .title(title)
                    .build();
            cursor.close();
        } else {
            Log.e("Find stop by ID", "Cursor is null");
        }
        return stop;
    }

    public static List<Stop> retrieveLineStops(ContentResolver contentResolver, Long lineId, LineDirection direction){
        List<Stop> stops = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                new String[] { STOP },
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[] { lineId.toString(), direction.toString()},
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long stopId = cursor.getLong(cursor.getColumnIndex(STOP));
                Stop stop = StopRepository.findById(contentResolver, stopId);
                if (stop != null) {
                    stops.add(stop);
                }else{
                    Log.e("Retrieve line stops", "Stop is null");
                }
            }
        }else{
            Log.e("Retrieve line stops", "Cursor is null");
        }
        return stops;
    }
}
