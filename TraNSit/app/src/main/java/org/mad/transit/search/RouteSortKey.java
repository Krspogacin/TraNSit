package org.mad.transit.search;

import org.mad.transit.R;

import lombok.Getter;

public enum RouteSortKey {
    TOTAL_DURATION(R.string.total_duration_sort_label),
    TOTAL_PRICE(R.string.total_price_sort_label),
    NEXT_DEPARTURE(R.string.next_departure_sort_label);

    @Getter
    private int labelId;

    RouteSortKey(int labelId) {
        this.labelId = labelId;
    }
}
