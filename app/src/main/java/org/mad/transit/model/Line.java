package org.mad.transit.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Line implements Serializable {
    private static final long serialVersionUID = 4969950388681257364L;
    private String number;
    private String name;
    private LineType type;
    private String nextDeparture;
}