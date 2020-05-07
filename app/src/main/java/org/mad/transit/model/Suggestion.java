package org.mad.transit.model;

import java.io.Serializable;

import lombok.Value;

@Value
public class Suggestion implements Serializable {
    private static final long serialVersionUID = 2761307877048338662L;
    String text;
    int icon;
}
