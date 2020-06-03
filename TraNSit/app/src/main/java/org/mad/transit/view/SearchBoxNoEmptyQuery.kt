package org.mad.transit.view

import android.content.ContentResolver
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import org.mad.transit.adapters.FavouritePlacesAdapter
import org.mad.transit.adapters.PlacesAdapter
import org.mad.transit.model.FavouriteLocation
import org.mad.transit.repository.FavouriteLocationRepository

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

            showFavouriteLocations(FavouriteLocationRepository.findAll(contentResolver))
        } else {
            chooseOnMapItemContainer.visibility = View.GONE
            chooseCurrentLocationItemContainer.visibility = View.GONE
            placesAdapter.query = query

            query.let {
                onQuerySubmitted?.invoke(query)
            }

            showFavouriteLocations(FavouriteLocationRepository.findAllByTitleContaining(contentResolver, query))
        }
        return false
    }

    override fun setText(text: String?, submitQuery: Boolean) {
        searchView.setQuery(text, submitQuery)
    }

    private fun showFavouriteLocations(favouriteLocations: List<FavouriteLocation>) {
        if (favouriteLocations.isNotEmpty()) {
            favouriteLocationsListHeaderContainer.visibility = View.VISIBLE
        } else {
            favouriteLocationsListHeaderContainer.visibility = View.GONE
        }
        favouritePlacesAdapter.favouriteLocations = favouriteLocations
    }
}
