package org.mad.transit.view

import android.content.ContentResolver
import android.database.Cursor
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import org.mad.transit.adapters.FavouritePlacesAdapter
import org.mad.transit.adapters.PlacesAdapter
import org.mad.transit.database.DBContentProvider
import org.mad.transit.util.retrieveFavouriteLocationsFromCursor

class SearchBoxNoEmptyQuery(
        private val searchView: SearchView,
        private val placesAdapter: PlacesAdapter,
        private val favouritePlacesAdapter: FavouritePlacesAdapter,
        private val chooseOnMapItemContainer: LinearLayout,
        private val chooseCurrentLocationItemContainer: LinearLayout,
        private val favouriteLocationsListHeaderContainer: LinearLayout,
        private val contentResolver: ContentResolver
) : SearchBoxView {

    override var onQueryChanged: Callback<String?>? = null
    override var onQuerySubmitted: Callback<String?>? = null

    init {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return filter(query)
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return filter(query)
            }
        })
    }

    fun filter(query: String?): Boolean {
        if (query == null || query.isEmpty()) {
            chooseOnMapItemContainer.visibility = View.VISIBLE
            chooseCurrentLocationItemContainer.visibility = View.VISIBLE
            placesAdapter.query = ""
            placesAdapter.setHits(emptyList())

            val cursor = contentResolver.query(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS,
                    null,
                    null,
                    null,
                    null)
            showFavouriteLocations(cursor)
            cursor?.close()
        } else {
            chooseOnMapItemContainer.visibility = View.GONE
            chooseCurrentLocationItemContainer.visibility = View.GONE
            placesAdapter.query = query

            query.let {
                onQuerySubmitted?.invoke(query)
            }
            //TODO if there are any favourite location which satisfies given query, then show favourite locations header, list and places header
            val cursor = contentResolver.query(DBContentProvider.CONTENT_URI_FAVOURITE_LOCATIONS,
                    null,
                    "title LIKE ?",
                    arrayOf("%${query.toLowerCase().trim()}%"),
                    null)
            showFavouriteLocations(cursor)
        }
        return false
    }

    override fun setText(text: String?, submitQuery: Boolean) {
        searchView.setQuery(text, submitQuery)
    }

    private fun showFavouriteLocations(cursor: Cursor?) {
        if (cursor?.count!! > 0) {
            favouritePlacesAdapter.favouriteLocations = retrieveFavouriteLocationsFromCursor(contentResolver, cursor)
            favouriteLocationsListHeaderContainer.visibility = View.VISIBLE
        } else {
            if (favouriteLocationsListHeaderContainer.visibility == View.VISIBLE) {
                favouriteLocationsListHeaderContainer.visibility = View.GONE
                favouritePlacesAdapter.favouriteLocations = emptyList()
            }
        }
    }
}
