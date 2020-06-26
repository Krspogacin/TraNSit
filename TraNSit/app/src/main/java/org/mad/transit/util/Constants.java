package org.mad.transit.util;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Constants {

    public static final long MILLISECONDS_IN_MINUTE = 60_000L;
    public static final double MILLISECONDS_IN_HOUR = 3_600_000D;

    public static final float GEOFENCE_NOTIFICATION_RADIUS = 100;

    //Column names
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String ZONE = "zone";
    public static final String START_ZONE = "start_zone";
    public static final String END_ZONE = "end_zone";
    public static final String PRICE = "price";
    public static final String LOCATION = "location";
    public static final String LINE = "line";
    public static final String DIRECTION = "direction";
    public static final String STOP = "stop";
    public static final String DAY = "day";
    public static final String NAME = "name";
    public static final String START_LOCATION = "start_location";
    public static final String END_LOCATION = "end_location";
    public static final String DATE = "date";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String TIMETABLE = "timetable";
    public static final String FORMATTED_VALUE = "formatted_value";
    public static final String NUMBER = "number";
    public static final String TYPE = "type";
    public static final String LINE_ID = "lineId";
    public static final String LINE_DIRECTION = "lineDirection";

    //Selections
    public static final String ID_SELECTION = "id = ?";
    public static final String START_AND_END_LOCATION_SELECTION = "start_location = ? and end_location = ?";
    public static final String TITLE_SELECTION = "title LIKE ?";

    //Months
    private static final Map<Integer, String> monthsMap;

    //Line colors
    private static final Map<Long, Integer> lineColorMap;

    private static final Random RANDOM;

    public static String getMonth(Integer month) {
        return monthsMap.get(month);
    }

    public static Integer getLineColor(Long lineId) {
        if (!lineColorMap.containsKey(lineId)) {
            int color = Color.rgb(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
            lineColorMap.put(lineId, color);
        }
        return lineColorMap.get(lineId);
    }


    static {
        monthsMap = new HashMap<>();
        monthsMap.put(0, "Januar");
        monthsMap.put(1, "Februar");
        monthsMap.put(2, "Mart");
        monthsMap.put(3, "April");
        monthsMap.put(4, "Maj");
        monthsMap.put(5, "Jun");
        monthsMap.put(6, "Jul");
        monthsMap.put(7, "Avgust");
        monthsMap.put(8, "Septembar");
        monthsMap.put(9, "Oktobar");
        monthsMap.put(10, "Novembar");
        monthsMap.put(11, "Decembar");

        lineColorMap = new HashMap<>();
        RANDOM = new Random();
    }
}