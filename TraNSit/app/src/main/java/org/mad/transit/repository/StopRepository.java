package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StopRepository {

    private final ContentResolver contentResolver;
    private final ZoneRepository zoneRepository;
    private final LocationRepository locationRepository;

    @Inject
    public StopRepository(ContentResolver contentResolver, ZoneRepository zoneRepository, LocationRepository locationRepository) {
        this.contentResolver = contentResolver;
        this.zoneRepository = zoneRepository;
        this.locationRepository = locationRepository;
    }

    public List<Stop> findAll() {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_STOP,
                null,
                null,
                null,
                null);

        if (cursor == null) {
            return null;
        }

        List<Stop> stops = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
            String title = cursor.getString(cursor.getColumnIndex(Constants.TITLE));
            Location location = this.locationRepository.findById(cursor.getString(cursor.getColumnIndex(Constants.LOCATION)));
            Stop stop = Stop.builder()
                    .id(id)
                    .location(location)
                    .title(title)
                    .build();
            stops.add(stop);
        }

        cursor.close();

        return stops;
    }

    public Stop findById(Long id) {
        Stop stop = null;
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_STOP,
                null,
                Constants.ID + " = ?",
                new String[]{id.toString()},
                null);

        if (cursor != null) {
            cursor.moveToFirst();

            String title = cursor.getString(cursor.getColumnIndex(Constants.TITLE));
            Zone zone = this.zoneRepository.findZoneById(cursor.getLong(cursor.getColumnIndex(Constants.ZONE)));
            Location location = this.locationRepository.findById(cursor.getString(cursor.getColumnIndex(Constants.LOCATION)));
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

    public List<Stop> findAllByLineIdAndLineDirection(Long lineId, LineDirection direction) {
        List<Stop> stops = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                new String[]{Constants.STOP},
                Constants.LINE + " = ? and " + Constants.DIRECTION + " = ?",
                new String[]{lineId.toString(), direction.toString()},
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long stopId = cursor.getLong(cursor.getColumnIndex(Constants.STOP));
                Stop stop = this.findById(stopId);
                if (stop != null) {
                    stops.add(stop);
                } else {
                    Log.e("Retrieve line stops", "Stop is null");
                }
            }
            cursor.close();
        } else {
            Log.e("Retrieve line stops", "Cursor is null");
        }
        return stops;
    }
}
