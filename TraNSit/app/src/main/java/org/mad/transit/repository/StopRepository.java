package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.LineDirection;
import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;
import org.mad.transit.search.LineStopDirection;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<Stop> stops = new ArrayList<>();

        if (cursor != null) {
            List<Location> locations = this.locationRepository.findAll();
            Map<Long, Location> locationsMap = new HashMap<>();
            for (Location location : locations) {
                locationsMap.put(location.getId(), location);
            }

            List<Zone> zones = this.zoneRepository.findAll();
            Map<Long, Zone> zonesMap = new HashMap<>();
            for (Zone zone : zones) {
                zonesMap.put(zone.getId(), zone);
            }

            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                String title = cursor.getString(cursor.getColumnIndex(Constants.TITLE));
                long locationId = cursor.getLong(cursor.getColumnIndex(Constants.LOCATION));
                long zoneId = cursor.getLong(cursor.getColumnIndex(Constants.ZONE));
                stops.add(Stop.builder()
                        .id(id)
                        .location(locationsMap.get(locationId))
                        .zone(zonesMap.get(zoneId))
                        .title(title)
                        .build());
            }
            cursor.close();
        }
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
            Zone zone = this.zoneRepository.findById(cursor.getLong(cursor.getColumnIndex(Constants.ZONE)));
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
            List<Stop> allStops = this.findAll();
            Map<Long, Stop> stopsMap = new HashMap<>();
            for (Stop stop : allStops) {
                stopsMap.put(stop.getId(), stop);
            }

            while (cursor.moveToNext()) {
                long stopId = cursor.getLong(cursor.getColumnIndex(Constants.STOP));
                stops.add(stopsMap.get(stopId));
            }
            cursor.close();
        } else {
            Log.e("Retrieve line stops", "Cursor is null");
        }
        return stops;
    }

    public List<LineStopDirection> findAllLinesStopsDirections() {
        List<LineStopDirection> linesStopsDirections = new ArrayList<>();
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_LINE_STOPS,
                new String[]{Constants.STOP, Constants.LINE, Constants.DIRECTION},
                null,
                null,
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long stopId = cursor.getLong(cursor.getColumnIndex(Constants.STOP));
                Long lineId = cursor.getLong(cursor.getColumnIndex(Constants.LINE));
                String direction = cursor.getString(cursor.getColumnIndex(Constants.DIRECTION));

                linesStopsDirections.add(LineStopDirection.builder()
                        .lineId(lineId)
                        .stopId(stopId)
                        .direction(LineDirection.valueOf(direction))
                        .build());
            }
            cursor.close();
        } else {
            Log.e("Lines stops directions", "Cursor is null");
        }
        return linesStopsDirections;
    }
}
