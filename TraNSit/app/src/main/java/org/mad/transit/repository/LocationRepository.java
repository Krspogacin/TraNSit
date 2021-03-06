package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocationRepository {

    private final ContentResolver contentResolver;

    @Inject
    public LocationRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<Location> findAll() {
        Cursor locationCursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                null,
                null,
                null);

        List<Location> locations = new ArrayList<>();

        if (locationCursor != null) {
            while (locationCursor.moveToNext()) {
                long id = locationCursor.getLong(locationCursor.getColumnIndex(Constants.ID));
                String name = locationCursor.getString(locationCursor.getColumnIndex(Constants.NAME));
                double latitude = locationCursor.getDouble(locationCursor.getColumnIndex(Constants.LATITUDE));
                double longitude = locationCursor.getDouble(locationCursor.getColumnIndex(Constants.LONGITUDE));
                locations.add(Location.builder()
                        .id(id)
                        .name(name)
                        .latitude(latitude)
                        .longitude(longitude)
                        .build());
            }
            locationCursor.close();
        }
        return locations;
    }

    public Location findById(String locationId) {
        Cursor locationCursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                Constants.ID + " = ?",
                new String[]{String.valueOf(locationId)},
                null);

        if (locationCursor == null) {
            return null;
        }

        Location location = null;
        if (locationCursor.moveToFirst()) {
            long id = locationCursor.getLong(locationCursor.getColumnIndex(Constants.ID));
            String name = locationCursor.getString(locationCursor.getColumnIndex(Constants.NAME));
            double latitude = locationCursor.getDouble(locationCursor.getColumnIndex(Constants.LATITUDE));
            double longitude = locationCursor.getDouble(locationCursor.getColumnIndex(Constants.LONGITUDE));
            location = new Location(id, name, latitude, longitude);
        }

        locationCursor.close();

        return location;
    }

    public Long save(Location location) {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION,
                null,
                Constants.LATITUDE + " = ? and " + Constants.LONGITUDE + " = ?",
                new String[]{location.getLatitude().toString(), location.getLongitude().toString()},
                null);

        if (cursor == null) {
            return null;
        }

        long id;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
            String name = cursor.getString(cursor.getColumnIndex(Constants.NAME));
            if ((name == null || name.isEmpty()) && location.getName() != null) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.NAME, location.getName());
                this.contentResolver.update(DBContentProvider.CONTENT_URI_LOCATION, contentValues, Constants.ID + " = ?", new String[]{Long.toString(id)});
            }
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.NAME, location.getName());
            contentValues.put(Constants.LATITUDE, location.getLatitude());
            contentValues.put(Constants.LONGITUDE, location.getLongitude());
            Uri uri = this.contentResolver.insert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
            id = Long.parseLong(uri.getLastPathSegment());
        }

        cursor.close();

        return id;
    }

    public List<Location> findAllByLineIdAndLineDirection(Long lineId, LineDirection direction) {
        List<Location> locations = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE_LOCATIONS,
                new String[]{Constants.LOCATION},
                Constants.LINE + " = ? and " + Constants.DIRECTION + " = ?",
                new String[]{lineId.toString(), direction.toString()},
                null);
        if (cursor != null) {
            List<Location> allLocations = this.findAll();
            Map<Long, Location> locationsMap = new HashMap<>();
            for (Location location : allLocations) {
                locationsMap.put(location.getId(), location);
            }

            while (cursor.moveToNext()) {
                long locationId = cursor.getLong(cursor.getColumnIndex(Constants.LOCATION));
                locations.add(locationsMap.get(locationId));
            }
            cursor.close();
        } else {
            Log.e("Retrieve line locations", "Cursor is null");
        }
        return locations;
    }
}
