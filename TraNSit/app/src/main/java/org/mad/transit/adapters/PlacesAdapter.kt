package org.mad.transit.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.algolia.instantsearch.core.hits.HitsView
import com.algolia.instantsearch.helper.android.inflate
import com.algolia.search.model.places.PlaceLanguage
import org.mad.transit.R
import org.mad.transit.view.holder.PlacesViewHolder

class PlacesAdapter(
        private val placesListHeaderContainer: LinearLayout
) : ListAdapter<PlaceLanguage, PlacesViewHolder>(PlacesAdapter), HitsView<PlaceLanguage> {

    var query: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {
        return PlacesViewHolder(parent.inflate(R.layout.place_item))
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item)
    }

    override fun setHits(hits: List<PlaceLanguage>) {
        if (query.isEmpty() && hits.isNotEmpty()) {
            if (placesListHeaderContainer.visibility == View.VISIBLE) {
                placesListHeaderContainer.visibility = View.GONE
            }
            return
        }

        if (hits.isEmpty()) {
            if (placesListHeaderContainer.visibility == View.VISIBLE) {
                placesListHeaderContainer.visibility = View.GONE
            }
        } else {
            placesListHeaderContainer.visibility = View.VISIBLE
        }

        submitList(hits)
    }

    companion object : DiffUtil.ItemCallback<PlaceLanguage>() {

        override fun areItemsTheSame(oldItem: PlaceLanguage, newItem: PlaceLanguage): Boolean {
            return oldItem::class == newItem::class
        }

        override fun areContentsTheSame(oldItem: PlaceLanguage, newItem: PlaceLanguage): Boolean {
            return oldItem == newItem
        }
    }
}