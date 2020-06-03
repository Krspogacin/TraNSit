package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.Location;

public class LocationRepository {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

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
}
