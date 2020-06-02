package org.mad.transit.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.model.Zone;

public class ZoneRepository {

    private static final String NAME = "name";

    public static Zone findZoneById(ContentResolver contentResolver, Long id) {
        Zone zone = null;
        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_ZONE,
                null,
                DatabaseHelper.ID + " = ?",
                new String[]{ id.toString() },
                null);

        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(NAME));
            zone = new Zone(id, name);
            cursor.close();
        }else{
            Log.e("Find zone by Id", "Cursor is null");
        }
        return zone;
    }
}
