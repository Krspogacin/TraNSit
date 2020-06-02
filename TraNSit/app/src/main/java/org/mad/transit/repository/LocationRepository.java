package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.Location;

public class LocationRepository {

    private static final String NAME = "name";
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";

    public static Location findLocationById(ContentResolver contentResolver, String id) {
        Cursor locationCursor = contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                DatabaseHelper.ID + " = ?",
                new String[]{String.valueOf(id)},
                null);

        locationCursor.moveToFirst();
        String name = locationCursor.getString(locationCursor.getColumnIndex(NAME));
        double latitude = locationCursor.getDouble(locationCursor.getColumnIndex(LATITUDE));
        double longitude = locationCursor.getDouble(locationCursor.getColumnIndex(LONGITUDE));

        locationCursor.close();

        return new Location(name, latitude, longitude);
    }

    public static Long saveLocation(Context context, Location location) {
        Cursor cursor = context.getContentResolver().query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                LATITUDE + " = ? and " + LONGITUDE + " = ?",
                new String[]{location.getLatitude().toString(), location.getLongitude().toString()},
                null);

        long id;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            if (name == null || name.isEmpty()) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(NAME, location.getName());
                context.getContentResolver().update(DBContentProvider.CONTENT_URI_LOCATION, contentValues, DatabaseHelper.ID + " = ?", new String[]{Long.toString(id)});
            }
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NAME, location.getName());
            contentValues.put(LATITUDE, location.getLatitude());
            contentValues.put(LONGITUDE, location.getLongitude());
            Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
            id = Long.parseLong(uri.getLastPathSegment());
        }

        cursor.close();

        return id;
    }
}
