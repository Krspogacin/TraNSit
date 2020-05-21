package org.mad.transit.view.holder

import android.text.SpannedString
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.core.highlighting.HighlightTokenizer
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import com.algolia.search.model.places.PlaceLanguage
import com.algolia.search.model.search.HighlightResult
import com.algolia.search.serialize.toHighlights
import org.mad.transit.R

class PlacesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val chooseOnMapDefaultOption = view.resources.getString(R.string.choose_on_map)
    private val chooseCurrentLocationDefaultOption = view.resources.getString(R.string.choose_current_location)

    fun bind(place: PlaceLanguage) {
        val name = place.highlightResultOrNull
                ?.toHighlights("locale_names")
                ?.firstOrNull()
                ?.tokenize() ?: place.localNames.first()

        when (name) {
            chooseOnMapDefaultOption -> {
                view.findViewById<TextView>(R.id.place_item_text).text = TextUtils.concat(name)
                view.findViewById<ImageView>(R.id.place_item_icon).setImageResource(R.drawable.ic_map_gray_24dp)
            }
            chooseCurrentLocationDefaultOption -> {
                view.findViewById<TextView>(R.id.place_item_text).text = TextUtils.concat(name)
                view.findViewById<ImageView>(R.id.place_item_icon).setImageResource(R.drawable.ic_my_location_gray_24dp)
            }
            else -> {
                val county = (place.highlightResultOrNull
                        ?.toHighlights("county")
                        ?.firstOrNull()
                        ?.tokenize() ?: place.county.first()) as SpannedString
                val postCode = place.postCodeOrNull?.firstOrNull()?.let { ", $it" } ?: ""

                view.findViewById<TextView>(R.id.place_item_text).text = TextUtils.concat(name, ", ", county, postCode)
                view.findViewById<ImageView>(R.id.place_item_icon).setImageResource(R.drawable.ic_location_on_gray_24dp)
            }
        }
    }

    private fun HighlightResult.tokenize(): SpannedString {
        return HighlightTokenizer()(value).toSpannedString()
    }
}