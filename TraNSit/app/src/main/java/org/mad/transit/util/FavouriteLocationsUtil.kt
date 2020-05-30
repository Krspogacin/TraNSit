package org.mad.transit.util

import android.content.ContentResolver
import android.database.Cursor
import org.mad.transit.database.DBContentProvider
import org.mad.transit.model.FavouriteLocation
import org.mad.transit.model.Location

fun retrieveFavouriteLocationsFromCursor(contentResolver: ContentResolver, cursor: Cursor): List<FavouriteLocation> {
    val favouriteLocations = ArrayList<FavouriteLocation>()
    while (cursor.moveToNext()) {
        val id = cursor.getLong(cursor.getColumnIndex("id"))
        val title = cursor.getString(cursor.getColumnIndex("title"))
        val locationId = cursor.getLong(cursor.getColumnIndex("location"))
        val locationCursor = contentResolver.query(DBContentProvider.CONTENT_URI_LOCATION, null, "id = ?", arrayOf(locationId.toString()), null)

        locationCursor?.moveToFirst()

        val name = locationCursor!!.getString(locationCursor.getColumnIndex("name"))
        val latitude = locationCursor.getDouble(locationCursor.getColumnIndex("latitude"))
        val longitude = locationCursor.getDouble(locationCursor.getColumnIndex("longitude"))

        val location = Location(locationId, name, latitude, longitude)
        val favouriteLocation = FavouriteLocation(id, title, location)

        favouriteLocations.add(favouriteLocation)

        locationCursor.close()
    }
    cursor.close()
    return favouriteLocations
}