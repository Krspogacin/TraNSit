package org.mad.transit.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import org.mad.transit.database.DBContentProvider;
import org.mad.transit.model.FavouriteLocation;
import org.mad.transit.model.Location;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FavouriteLocationRepository {

    private final ContentResolver contentResolver;
    private final LocationRepository locationRepository;

    @Inject
    public FavouriteLocationRepository(ContentResolver contentResolver, LocationRepository locationRepository) {
        this.contentResolver = contentResolver;
        this.locationRepository = locationRepository;
    }

    public List<FavouriteLocation> findAll() {
        return this.findAllWithSelection(null, null);
    }

    public List<FavouriteLocation> findAllByTitleContaining(String query) {
        return this.findAllWithSelection(Constants.TITLE_SELECTION, new String[]{query.toLowerCase() + "%"});
    }

    public void save(FavouriteLocation favouriteLocation) {
        if (favouriteLocation == null || favouriteLocation.getTitle() == null || favouriteLocation.getLocation() == null) {
            return;
        }

        ContentValues contentValues = new ContentValues();
        if (favouriteLocation.getId() != null) {
            contentValues.put(Constants.ID, favouriteLocation.getId());
        }
        contentValues.put(Constants.TITLE, favouriteLocation.getTitle());
        contentValues.put(Constants.LOCATION, favouriteLocation.getLocation().getId());
        this.contentResolver.insert(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, contentValues);
    }

    private List<FavouriteLocation> findAllWithSelection(String selection, String[] selectionArgs) {
        Cursor cursor = this.contentResolver.query(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, null, selection, selectionArgs, null);

        if (cursor == null) {
            return null;
        }

        List<FavouriteLocation> favouriteLocations = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndex(Constants.ID));
            String title = cursor.getString(cursor.getColumnIndex(Constants.TITLE));
            long locationId = cursor.getLong(cursor.getColumnIndex(Constants.LOCATION));

            Location location = this.locationRepository.findById(String.valueOf(locationId));

            FavouriteLocation favouriteLocation = new FavouriteLocation(id, title, location);
            favouriteLocations.add(favouriteLocation);
        }

        cursor.close();

        return favouriteLocations;
    }

    public void deleteById(String id) {
        this.contentResolver.delete(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, Constants.ID_SELECTION, new String[]{id});
    }

    public void deleteAll() {
        this.contentResolver.delete(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS, null, null);
    }
}