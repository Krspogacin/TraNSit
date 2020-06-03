package org.mad.transit.repository;

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

public class PastDirectionRepository {

    private static final DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag("sr-RS"));
    private static final String ID = "id";
    private static final String START_LOCATION = "start_location";
    private static final String END_LOCATION = "end_location";
    private static final String DATE = "date";
    private static final String ID_SELECTION = "id = ?";
    private static final String START_AND_END_LOCATION_SELECTION = "start_location = ? and end_location = ?";

    public static List<PastDirection> findAll(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return null;
        }

        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, null, null, null, null);

        if (cursor == null) {
            return null;
        }

        List<PastDirection> pastDirections = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(ID));
            long startLocationId = cursor.getLong(cursor.getColumnIndex(START_LOCATION));
            long endLocationId = cursor.getLong(cursor.getColumnIndex(END_LOCATION));
            String date = cursor.getString(cursor.getColumnIndex(DATE));

            Location startLocation = LocationRepository.findById(contentResolver, String.valueOf(startLocationId));
            Location endLocation = LocationRepository.findById(contentResolver, String.valueOf(endLocationId));

            PastDirection pastDirection = new PastDirection(id, startLocation, endLocation, date);
            pastDirections.add(pastDirection);
        }

        cursor.close();

        return pastDirections;
    }

    public static PastDirection findByStartLocationAndEndLocation(ContentResolver contentResolver, Long startLocationId, Long endLocationId) {

        if (contentResolver == null || startLocationId == null || endLocationId == null) {
            return null;
        }

        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS,
                null,
                START_AND_END_LOCATION_SELECTION,
                new String[]{startLocationId.toString(), endLocationId.toString()},
                null);

        if (cursor == null) {
            return null;
        }

        PastDirection pastDirection = null;
        if (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(ID));
            String date = cursor.getString(cursor.getColumnIndex(DATE));

            Location startLocation = LocationRepository.findById(contentResolver, String.valueOf(startLocationId));
            Location endLocation = LocationRepository.findById(contentResolver, String.valueOf(endLocationId));

            pastDirection = new PastDirection(id, startLocation, endLocation, date);
        }

        cursor.close();

        return pastDirection;
    }

    public static void save(ContentResolver contentResolver, Long startLocationId, Long endLocationId) {

        if (startLocationId == null || endLocationId == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(START_LOCATION, startLocationId);
        contentValues.put(END_LOCATION, endLocationId);
        contentValues.put(DATE, dateFormat.format(new Date()));
        contentResolver.insert(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues);
    }

    public static void update(ContentResolver contentResolver, PastDirection pastDirection) {

        if (pastDirection == null || pastDirection.getId() == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(DATE, dateFormat.format(new Date()));
        contentResolver.update(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues, ID_SELECTION, new String[]{pastDirection.getId().toString()});
    }

    public static void deleteAll(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return;
        }

        contentResolver.delete(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, null, null);
    }
}