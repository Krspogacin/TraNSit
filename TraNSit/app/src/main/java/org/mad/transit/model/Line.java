package org.mad.transit.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Line implements Serializable {
    private static final long serialVersionUID = 4969950388681257364L;
    private Long id;
    private String number;
    private String title;
    private LineType type;
    private String nextDeparture;
    private LineOneDirection lineDirectionA;
    private LineOneDirection lineDirectionB;
}