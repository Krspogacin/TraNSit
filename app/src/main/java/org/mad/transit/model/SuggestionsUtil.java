package org.mad.transit.model;

import org.mad.transit.R;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SuggestionsUtil {

    public List<Suggestion> getSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(new Suggestion("Bulevar Kralja Petra I", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Vojvode Bojovića", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Kisačka", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Romanijska", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Gundulićeva", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Bulevar oslobođenja", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Đurđa Brankovića", R.drawable.ic_location_on_gray_24dp));
        return suggestions;
    }
}
