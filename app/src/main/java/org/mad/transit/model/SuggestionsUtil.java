package org.mad.transit.model;

import org.mad.transit.R;

import java.util.ArrayList;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SuggestionsUtil {

    public List<Suggestion> getSuggestions() {
        List<Suggestion> suggestions = new ArrayList<>();
        suggestions.add(new Suggestion("Monday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Tuesday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Wednesday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Thursday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Friday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Saturday", R.drawable.ic_location_on_gray_24dp));
        suggestions.add(new Suggestion("Sunday", R.drawable.ic_location_on_gray_24dp));
        return suggestions;
    }
}
