package org.mad.transit.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavouriteLocation implements Serializable {
    private static final long serialVersionUID = -4718728730795073749L;
    private Long id;
    private String title;
    private Location location;

    public FavouriteLocation(Long id, String title, Location location) {
        this.id = id;
        this.title = title;
        this.location = location;
    }

    public FavouriteLocation(String title, Location location) {
        this.title = title;
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }
}