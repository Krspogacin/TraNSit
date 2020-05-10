package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Stop implements Serializable {
    private static final long serialVersionUID = 9663211053934640L;
    protected String title;
    protected double latitude;
    protected double longitude;
    protected List<Line> lines;
}
