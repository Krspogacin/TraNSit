package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.Location;
import org.mad.transit.model.PastDirection;
import org.mad.transit.util.Constants;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mad.transit.util.Constants.DATE_FORMAT;

@Singleton
public class PastDirectionRepository {

    private final ContentResolver contentResolver;
    private final LocationRepository locationRepository;

    @Inject
    public PastDirectionRepository(ContentResolver contentResolver, LocationRepository locationRepository) {
        this.contentResolver = contentResolver;
        this.locationRepository = locationRepository;
    }

    public List<PastDirection> findAll() {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, null, null, null, null);

        if (cursor == null) {
            return null;
        }

        List<PastDirection> pastDirections = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
            long startLocationId = cursor.getLong(cursor.getColumnIndex(Constants.START_LOCATION));
            long endLocationId = cursor.getLong(cursor.getColumnIndex(Constants.END_LOCATION));
            String date = cursor.getString(cursor.getColumnIndex(Constants.DATE));

            Location startLocation = this.locationRepository.findById(String.valueOf(startLocationId));
            Location endLocation = this.locationRepository.findById(String.valueOf(endLocationId));

            PastDirection pastDirection = new PastDirection(id, startLocation, endLocation, date);
            pastDirections.add(pastDirection);
        }

        cursor.close();

        Collections.sort(pastDirections, this.getDateComparator());

        return pastDirections;
    }

    public PastDirection findByStartLocationAndEndLocation(Long startLocationId, Long endLocationId) {
        if (startLocationId == null || endLocationId == null) {
            return null;
        }

        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS,
                null,
                Constants.START_AND_END_LOCATION_SELECTION,
                new String[]{startLocationId.toString(), endLocationId.toString()},
                null);

        if (cursor == null) {
            return null;
        }

        PastDirection pastDirection = null;
        if (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
            String date = cursor.getString(cursor.getColumnIndex(Constants.DATE));

            Location startLocation = this.locationRepository.findById(String.valueOf(startLocationId));
            Location endLocation = this.locationRepository.findById(String.valueOf(endLocationId));

            pastDirection = new PastDirection(id, startLocation, endLocation, date);
        }

        cursor.close();

        return pastDirection;
    }

    public void save(Long startLocationId, Long endLocationId) {
        if (startLocationId == null || endLocationId == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.START_LOCATION, startLocationId);
        contentValues.put(Constants.END_LOCATION, endLocationId);
        contentValues.put(Constants.DATE, DATE_FORMAT.format(new Date()));
        this.contentResolver.insert(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues);
    }

    public void update(PastDirection pastDirection) {
        if (pastDirection == null || pastDirection.getId() == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(Constants.DATE, DATE_FORMAT.format(new Date()));
        this.contentResolver.update(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, contentValues, Constants.ID_SELECTION, new String[]{pastDirection.getId().toString()});
    }

    public void deleteAll() {
        this.contentResolver.delete(DBContentProvider.CONTENT_URI_PAST_DIRECTIONS, null, null);
    }

    private Comparator<PastDirection> getDateComparator() {
        return (o1, o2) -> {
            try {
                Date date1 = DATE_FORMAT.parse(o1.getDate());
                Date date2 = DATE_FORMAT.parse(o2.getDate());
                return date1.before(date2) ? 1 : date1.after(date2) ? -1 : 0;
            } catch (ParseException e) {
                return 0;
            }
        };
    }
}