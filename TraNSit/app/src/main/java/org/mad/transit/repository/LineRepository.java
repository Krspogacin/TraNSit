package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.Line;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.LineType;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;

import java.util.ArrayList;
import java.util.List;

public class LineRepository {

    //Line, LineStops, LineLocations
    private static final String NAME = "name";
    private static final String NUMBER = "number";
    private static final String TYPE = "type";
    private static final String LINE = "line";
    private static final String DIRECTION = "direction";
    private static final String STOP = "stop";
    private static final String LOCATION = "location";

    public static List<Line> retrieveLines(ContentResolver contentResolver) {
        List<Line> lines = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE,
                null,
                null,
                null,
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
                String title = cursor.getString(cursor.getColumnIndex(NAME));
                String number = cursor.getString(cursor.getColumnIndex(NUMBER));
                String type = cursor.getString(cursor.getColumnIndex(TYPE));
                Line line = Line.builder()
                        .id(id)
                        .number(number)
                        .title(title)
                        .type(LineType.valueOf(type))
                        .build();

                lines.add(line);
            }
            cursor.close();
        } else {
            Log.e("Retrieve lines", "Cursor is null");
        }
        return lines;
    }

    public static boolean checkIfDirectionBExist(ContentResolver contentResolver, Long lineId) {
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                null,
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[]{lineId.toString(), LineDirection.B.toString()},
                null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            } else {
                cursor.close();
                return false;
            }
        } else {
            Log.e("Check if B exist", "Cursor is null");
            return false;
        }
    }


    public static List<Stop> retrieveLinesStops(ContentResolver contentResolver, Long lineId, LineDirection direction) {
        List<Stop> stops = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                new String[]{STOP},
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[]{lineId.toString(), direction.toString()},
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long stopId = cursor.getLong(cursor.getColumnIndex(STOP));
                Stop stop = StopRepository.findStopById(contentResolver, stopId);
                if (stop != null) {
                    stops.add(stop);
                } else {
                    Log.e("Retrieve line stops", "Stop is null");
                }
            }
        } else {
            Log.e("Retrieve line stops", "Cursor is null");
        }
        return stops;
    }

    public static List<Location> retrieveLineLocations(ContentResolver contentResolver, Long lineId, LineDirection direction) {
        List<Location> locations = new ArrayList<>();
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_LINE_LOCATIONS,
                new String[]{LOCATION},
                LINE + " = ? and " + DIRECTION + " = ?",
                new String[]{lineId.toString(), direction.toString()},
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String stopId = cursor.getString(cursor.getColumnIndex(LOCATION));
                Location location = LocationRepository.findById(contentResolver, stopId);
                locations.add(location);
            }
        } else {
            Log.e("Retrieve line locations", "Cursor is null");
        }
        return locations;
    }
}
