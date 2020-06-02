package org.mad.transit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PastDirection {

    private Long id;
    private Location startLocation;
    private Location endLocation;
    private String date;
}