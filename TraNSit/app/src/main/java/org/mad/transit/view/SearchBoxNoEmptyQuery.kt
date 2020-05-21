package org.mad.transit.view

import androidx.appcompat.widget.SearchView
import com.algolia.instantsearch.core.Callback
import com.algolia.instantsearch.core.searchbox.SearchBoxView
import com.algolia.search.model.places.PlaceLanguage
import org.mad.transit.R
import org.mad.transit.adapters.PlacesAdapter

public class SearchBoxNoEmptyQuery(
        public val searchView: SearchView,
        public val adapter: PlacesAdapter
) : SearchBoxView {

    override var onQueryChanged: Callback<String?>? = null
    override var onQuerySubmitted: Callback<String?>? = null

    init {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return defaultOptions(query)
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return defaultOptions(query)
            }
        })
    }

    fun defaultOptions(query: String?): Boolean {
        if (query == null || query.isEmpty()) {
            val chooseOnMap = PlaceLanguage(null, null, null, arrayListOf(searchView.resources.getString(R.string.choose_on_map)))
            val chooseCurrentLocation = PlaceLanguage(null, null, null, arrayListOf(searchView.resources.getString(R.string.choose_current_location)))
            adapter.setHits(arrayListOf(chooseOnMap, chooseCurrentLocation))
        } else {
            query.let {
                onQuerySubmitted?.invoke(query)
            }
        }
        return false
    }

    override fun setText(text: String?, submitQuery: Boolean) {
        searchView.setQuery(text, submitQuery)
    }
}
