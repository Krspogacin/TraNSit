package org.mad.transit.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class Location implements Serializable {
    private static final long serialVersionUID = -7304512443431752058L;
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;

    public Location(Long id, String name, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Location(String name, Double latitude, Double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Location that = (Location) o;
        return Objects.equals(this.latitude, that.latitude) &&
                Objects.equals(this.longitude, that.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.latitude, this.longitude);
    }
}
