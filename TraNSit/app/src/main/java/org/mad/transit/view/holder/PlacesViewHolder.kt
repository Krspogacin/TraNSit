package org.mad.transit.view.holder

import android.text.SpannedString
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.core.highlighting.HighlightTokenizer
import com.algolia.instantsearch.helper.android.highlighting.toSpannedString
import com.algolia.search.model.places.PlaceLanguage
import com.algolia.search.model.search.HighlightResult
import com.algolia.search.serialize.toHighlights
import org.mad.transit.R

class PlacesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    fun bind(place: PlaceLanguage) {
        val firstOrNull = place.highlightResultOrNull
                ?.toHighlights("locale_names")
                ?.firstOrNull()
        
        val name = firstOrNull
                ?.tokenize() ?: place.localNames.first()
        val county = (place.highlightResultOrNull
                ?.toHighlights("county")
                ?.firstOrNull()
                ?.tokenize() ?: place.county.first()) as SpannedString
        val postCode = place.postCodeOrNull?.firstOrNull()?.let { ", $it" } ?: ""

        view.findViewById<TextView>(R.id.place_item_text).text = TextUtils.concat(name, ", ", county, postCode)
    }

    public companion object {
        fun HighlightResult.tokenize(): SpannedString {
            return HighlightTokenizer()(value).toSpannedString()
        }
    }
}