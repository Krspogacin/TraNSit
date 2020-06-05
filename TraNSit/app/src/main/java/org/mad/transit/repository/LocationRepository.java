package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationRepository {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String LINE = "line";
    private static final String DIRECTION = "direction";
    private static final String LOCATION = "location";

    public static Location findById(ContentResolver contentResolver, String locationId) {
        Cursor locationCursor = contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                DatabaseHelper.ID + " = ?",
                new String[]{String.valueOf(locationId)},
                null);

        if (locationCursor == null) {
            return null;
        }

        Location location = null;
        if (locationCursor.moveToFirst()) {
            long id = locationCursor.getLong(locationCursor.getColumnIndex(ID));
            String name = locationCursor.getString(locationCursor.getColumnIndex(NAME));
            double latitude = locationCursor.getDouble(locationCursor.getColumnIndex(LATITUDE));
            double longitude = locationCursor.getDouble(locationCursor.getColumnIndex(LONGITUDE));
            location = new Location(id, name, latitude, longitude);
        }

        locationCursor.close();

        return location;
    }

    public static Long save(ContentResolver contentResolver, Location location) {
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                LATITUDE + " = ? and " + LONGITUDE + " = ?",
                new String[]{location.getLatitude().toString(), location.getLongitude().toString()},
                null);

        if (cursor == null) {
            return null;
        }

        long id;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            if (name == null || name.isEmpty()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(NAME, location.getName());
                contentResolver.update(DBContentProvider.CONTENT_URI_LOCATION, contentValues, DatabaseHelper.ID + " = ?", new String[]{Long.toString(id)});
            }
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NAME, location.getName());
            contentValues.put(LATITUDE, location.getLatitude());
            contentValues.put(LONGITUDE, location.getLongitude());
            Uri uri = contentResolver.insert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
            id = Long.parseLong(uri.getLastPathSegment());
        }

        cursor.close();

        return id;
    }

    public static List<Location> retrieveLineLocations(ContentResolver contentResolver, Long lineId, LineDirection direction){
        List<Location> locations = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE_LOCATIONS,
                new String[] { LOCATION },
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[] { lineId.toString(), direction.toString()},
                null);
        if (cursor != null){
            while (cursor.moveToNext()) {
                String locationId = cursor.getString(cursor.getColumnIndex(LOCATION));
                Location location = findById(contentResolver, locationId);
                locations.add(location);
            }
        }else{
            Log.e("Retrieve line locations", "Cursor is null");
        }
        return locations;
    }
}
