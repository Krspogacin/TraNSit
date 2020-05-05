package org.mad.transit.model;

public class Line {
    private String number;
    private String name;
    private LineType type;

    public Line() {}

    public Line(String number, String name, LineType type) {
        this.number = number;
        this.name = name;
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LineType getType() {
        return type;
    }

    public void setType(LineType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Line{" +
                "number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
