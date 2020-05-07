package org.mad.transit.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.mad.transit.R;
import org.mad.transit.model.Suggestion;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SuggestionsAdapter extends ArrayAdapter<Suggestion> {

    private List<Suggestion> allSuggestions;
    private List<Suggestion> defaultOptions;

    public SuggestionsAdapter(@NonNull Context context, @NonNull List<Suggestion> suggestions) {
        super(context, 0, suggestions);
        allSuggestions = new ArrayList<>(suggestions);
        clear(); //necessary for not showing values at beginning
        defaultOptions = new ArrayList<>();
        defaultOptions.add(new Suggestion(getContext().getString(R.string.choose_on_map), R.drawable.ic_map_gray_24dp));
        defaultOptions.add(new Suggestion(getContext().getString(R.string.choose_current_location), R.drawable.ic_my_location_gray_24dp));
        addAll(defaultOptions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.suggestions_list_item, parent, false);
        }

        TextView locationText = convertView.findViewById(R.id.suggestion_text);
        ImageView locationImage = convertView.findViewById(R.id.suggestion_icon);

        Suggestion suggestion = getItem(position);
        if (suggestion != null) {
            locationText.setText(suggestion.getText());
            locationImage.setImageResource(suggestion.getIcon());
        }

        return convertView;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return locationFilter;
    }

    private Filter locationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Suggestion> suggestions = new ArrayList<>();

            if (isSearchTextEmpty(constraint)) {
                suggestions.addAll(defaultOptions);
            } else {
                String text = constraint.toString().trim().toLowerCase();
                for (Suggestion suggestion : allSuggestions) {
                    if (suggestion.getText().toLowerCase().startsWith(text)) {
                        suggestions.add(suggestion);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List<Suggestion>) results.values);
            notifyDataSetChanged();
        }


        private boolean isSearchTextEmpty(CharSequence constraint) {
            return TextUtils.isEmpty(constraint) || constraint.toString().trim().isEmpty();
        }
    };

}
