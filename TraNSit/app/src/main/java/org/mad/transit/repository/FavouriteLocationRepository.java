package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.FavouriteLocation;
import org.mad.transit.model.Location;

import java.util.ArrayList;
import java.util.List;

public class FavouriteLocationRepository {

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String LOCATION = "location";
    private static final String TITLE_SELECTION = "title LIKE ?";
    private static final String ID_SELECTION = "id = ?";

    public static List<FavouriteLocation> findAll(ContentResolver contentResolver) {
        return findAllWithSelection(contentResolver, null, null);
    }

    public static List<FavouriteLocation> findAllByTitleContaining(ContentResolver contentResolver, String query) {
        return findAllWithSelection(contentResolver, TITLE_SELECTION, new String[]{query.toLowerCase() + "%"});
    }

    public static void save(ContentResolver contentResolver, FavouriteLocation favouriteLocation) {
        if (contentResolver == null || favouriteLocation == null || favouriteLocation.getTitle() == null || favouriteLocation.getLocation() == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        if (favouriteLocation.getId() != null) {
            contentValues.put(ID, favouriteLocation.getId());
        }
        contentValues.put(TITLE, favouriteLocation.getTitle());
        contentValues.put(LOCATION, favouriteLocation.getLocation().getId());
        contentResolver.insert(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, contentValues);
    }

    private static List<FavouriteLocation> findAllWithSelection(ContentResolver contentResolver, String selection, String[] selectionArgs) {
        if (contentResolver == null) {
            return null;
        }

        Cursor cursor = contentResolver.query(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, null, selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        List<FavouriteLocation> favouriteLocations = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(ID));
            String title = cursor.getString(cursor.getColumnIndex(TITLE));
            long locationId = cursor.getLong(cursor.getColumnIndex(LOCATION));

            Location location = LocationRepository.findById(contentResolver, String.valueOf(locationId));

            FavouriteLocation favouriteLocation = new FavouriteLocation(id, title, location);
            favouriteLocations.add(favouriteLocation);
        }

        cursor.close();

        return favouriteLocations;
    }

    public static void deleteById(ContentResolver contentResolver, String id) {
        if (contentResolver == null || id == null) {
            return;
        }

        contentResolver.delete(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, ID_SELECTION, new String[]{id});
    }

    public static void deleteAll(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return;
        }

        contentResolver.delete(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, null, null);
    }
}