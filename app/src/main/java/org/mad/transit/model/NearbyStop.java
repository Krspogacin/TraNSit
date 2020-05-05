package org.mad.transit.model;

public class NearbyStop {
    private String name;
    private int walkTime;
    private String[] lines;

    public NearbyStop() {
    }

    public NearbyStop(String name, int walkTime, String[] lines) {
        this.name = name;
        this.walkTime = walkTime;
        this.lines = lines;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWalkTime() {
        return this.walkTime;
    }

    public void setWalkTime(int walkTime) {
        this.walkTime = walkTime;
    }

    public String[] getLines() {
        return this.lines;
    }

    public void setLines(String[] lines) {
        this.lines = lines;
    }
}