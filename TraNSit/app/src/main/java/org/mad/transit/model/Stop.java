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
    protected Long id;
    protected String title;
    protected Location location;
    protected Zone zone;
    protected List<Line> lines;
}
