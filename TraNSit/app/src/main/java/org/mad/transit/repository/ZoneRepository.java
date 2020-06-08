package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.Zone;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ZoneRepository {

    private final ContentResolver contentResolver;

    @Inject
    public ZoneRepository(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public List<Zone> findAll() {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_ZONE,
                null,
                null,
                null,
                null);

        List<Zone> zones = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
                String name = cursor.getString(cursor.getColumnIndex(Constants.NAME));
                zones.add(Zone.builder()
                        .id(id)
                        .name(name)
                        .build());
            }
            cursor.close();
        }
        return zones;
    }

    public Zone findById(Long id) {
        Zone zone = null;
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_ZONE,
                null,
                Constants.ID + " = ?",
                new String[]{id.toString()},
                null);

        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(Constants.NAME));
            zone = new Zone(id, name);
            cursor.close();
        } else {
            Log.e("Find zone by Id", "Cursor is null");
        }
        return zone;
    }
}
