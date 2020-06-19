package org.mad.transit.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartureTime implements Serializable {
    private static final long serialVersionUID = 6484961952185968191L;
    private Long id;
    private String formattedValue;
    private Long timetableId;
}