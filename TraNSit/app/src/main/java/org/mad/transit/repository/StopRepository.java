package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;

public class StopRepository {

    private static final String TITLE = "title";
    private static final String ZONE = "zone";
    private static final String LOCATION = "location";

    public static Stop findStopById(ContentResolver contentResolver, Long id) {
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
}
