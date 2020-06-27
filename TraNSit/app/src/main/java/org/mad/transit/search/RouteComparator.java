package org.mad.transit.search;

import org.mad.transit.dto.RouteDto;

import java.util.Comparator;
import java.util.Date;

import lombok.SneakyThrows;

import static org.mad.transit.search.RouteSortKey.NEXT_DEPARTURE;
import static org.mad.transit.search.RouteSortKey.TOTAL_DURATION;
import static org.mad.transit.search.RouteSortKey.TOTAL_PRICE;
import static org.mad.transit.util.Constants.DATE_FORMAT;

public enum RouteComparator implements Comparator<RouteDto> {
    TOTAL_PRICE_COMPARATOR(TOTAL_PRICE) {
        @Override
        public int compare(RouteDto route1, RouteDto route2) {
            return route1.getTotalPrice() - route2.getTotalPrice();
        }
    },
    TOTAL_DURATION_COMPARATOR(TOTAL_DURATION) {
        @Override
        public int compare(RouteDto route1, RouteDto route2) {
            return route1.getTotalDuration() - route2.getTotalDuration();
        }
    },
    NEXT_DEPARTURE_COMPARATOR(NEXT_DEPARTURE) {
        @SneakyThrows
        @Override
        public int compare(RouteDto route1, RouteDto route2) {
            if (route1.getNextDeparture() == null) {
                return 1;
            } else if (route2.getNextDeparture() == null) {
                return -1;
            } else {
                Date nextDeparture1 = DATE_FORMAT.parse(route1.getNextDeparture());
                Date nextDeparture2 = DATE_FORMAT.parse(route2.getNextDeparture());
                return nextDeparture1.compareTo(nextDeparture2);
            }
        }
    };

    private RouteSortKey sortKey;

    RouteComparator(RouteSortKey sortKey) {
        this.sortKey = sortKey;
    }

    public static RouteSortKey getDefaultSortKey() {
        return TOTAL_DURATION;
    }

    public static RouteComparator getDefaultComparator() {
        return TOTAL_DURATION_COMPARATOR;
    }

    public static RouteComparator getComparatorBySortKey(RouteSortKey routeSortKey) {
        for (RouteComparator comparator : RouteComparator.values()) {
            if (routeSortKey == comparator.sortKey) {
                return comparator;
            }
        }
        return getDefaultComparator();
    }
}
