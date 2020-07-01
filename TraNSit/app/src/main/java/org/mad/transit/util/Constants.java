package org.mad.transit.util;

import android.graphics.Color;

import org.mad.transit.model.TimetableDay;
import org.mad.transit.search.SearchOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Constants {

    private Constants() {
    }

    //Units
    public static final long MILLISECONDS_IN_MINUTE = 60_000L;
    public static final double MILLISECONDS_IN_HOUR = 3_600_000D;

    public static final float GEOFENCE_NOTIFICATION_RADIUS = 100;
    public static final float GEOFENCE_NAVIGATION_RADIUS = 50;

    //Search
    public static final String DEFAULT_TIME_OPTION = "Sada";
    private static final String SECOND_TIME_OPTION = "Izaberi vreme";

    public static final List<String> SEARCH_TIME_OPTIONS = Arrays.asList(DEFAULT_TIME_OPTION, SECOND_TIME_OPTION);

    private static final int FIRST_SOLUTION_COUNT_OPTION = 1;
    public static final int DEFAULT_SOLUTION_COUNT_OPTION = 3;
    private static final int THIRD_SOLUTION_COUNT_OPTION = 5;

    public static final List<Integer> SEARCH_SOLUTION_COUNT_OPTIONS = Arrays.asList(FIRST_SOLUTION_COUNT_OPTION, DEFAULT_SOLUTION_COUNT_OPTION, THIRD_SOLUTION_COUNT_OPTION);

    //Date
    public static final DateFormat TIME_FORMAT;
    public static final DateFormat DATE_FORMAT;

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

    public static SearchOptions getDefaultSearchOptions() {
        Calendar calendar = Calendar.getInstance();
        return SearchOptions.builder()
                .solutionCount(DEFAULT_SOLUTION_COUNT_OPTION)
                .transfersEnabled(true)
                .hours(calendar.get(Calendar.HOUR_OF_DAY))
                .minutes(calendar.get(Calendar.MINUTE))
                .build();
    }

    //Day
    public static TimetableDay getCurrentTimetableDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY:
                return TimetableDay.SUNDAY;
            case Calendar.SATURDAY:
                return TimetableDay.SATURDAY;
            default:
                return TimetableDay.WORKDAY;
        }
    }

    //Time
    public static long getTimeInMilliseconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (60 * hours + minutes) * MILLISECONDS_IN_MINUTE;
    }

    static {
        TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.forLanguageTag("sr-RS"));
        DATE_FORMAT = SimpleDateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag("sr-RS"));
//        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC")); // TODO check if this could be set for all formatting cases

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