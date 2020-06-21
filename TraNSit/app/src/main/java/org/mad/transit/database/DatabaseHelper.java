package org.mad.transit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mad.transit.util.Constants;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String TABLE_STOP = "stop";
    static final String TABLE_LINE = "line";
    static final String TABLE_ZONE = "zone";
    static final String TABLE_PRICE_LIST = "price_list";
    static final String TABLE_TIMETABLE = "timetable";
    static final String TABLE_DEPARTURE_TIME = "departure_time";
    static final String TABLE_LOCATION = "location";
    static final String TABLE_LINE_STOPS = "line_stops";
    static final String TABLE_LINE_LOCATIONS = "line_locations";
    static final String TABLE_FAVOURITE_LOCATIONS = "favourite_locations";
    static final String TABLE_PAST_DIRECTIONS = "past_directions";
    private static final String DATABASE_NAME = "transit.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Map<String, String> columns = new HashMap<>();

        //CREATE ZONE TABLE
        columns.put("name", "text not null unique");
        String tableZone = this.createTable(TABLE_ZONE, columns);
        tableZone = this.finishQuery(tableZone);
        db.execSQL(tableZone);

        columns.clear();

        //CREATE PRICE LIST TABLE
        columns.put("start_zone", "integer not null");
        columns.put("end_zone", "integer not null");
        columns.put("price", "integer not null");
        String tablePriceList = this.createTable(TABLE_PRICE_LIST, columns);
        tablePriceList = this.addForeignKey(tablePriceList, "start_zone", TABLE_ZONE, Constants.ID);
        tablePriceList = this.addForeignKey(tablePriceList, "end_zone", TABLE_ZONE, Constants.ID);
        tablePriceList = this.finishQuery(tablePriceList);
        db.execSQL(tablePriceList);

        columns.clear();

        //CREATE LOCATION TABLE
        columns.put("name", "text");
        columns.put("latitude", "real not null");
        columns.put("longitude", "real not null");
        String tableLocation = this.createTable(TABLE_LOCATION, columns);
        tableLocation = this.finishQuery(tableLocation);
        db.execSQL(tableLocation);

        columns.clear();

        //CREATE STOP TABLE
        columns.put("title", "text not null");
        columns.put("location", "integer not null");
        columns.put("zone", "integer not null");
        String tableStop = this.createTable(TABLE_STOP, columns);
        tableStop = this.addForeignKey(tableStop, "location", TABLE_LOCATION, Constants.ID);
        tableStop = this.addForeignKey(tableStop, "zone", TABLE_ZONE, Constants.ID);
        tableStop = this.finishQuery(tableStop);
        db.execSQL(tableStop);

        columns.clear();

        //CREATE LINE TABLE
        columns.put("number", "text not null unique");
        columns.put("name", "text not null");
        columns.put("type", "text");
        String tableLine = this.createTable(TABLE_LINE, columns);
        tableLine = this.finishQuery(tableLine);
        db.execSQL(tableLine);

        columns.clear();

        //CREATE TIMETABLE TABLE
        columns.put("line", "integer not null");
        columns.put("direction", "text");
        columns.put("day", "text");
        String tableTimetable = this.createTable(TABLE_TIMETABLE, columns);
        tableTimetable = this.addForeignKey(tableTimetable, "line", TABLE_LINE, Constants.ID);
        tableTimetable = this.finishQuery(tableTimetable);
        db.execSQL(tableTimetable);

        columns.clear();

        //CREATE DEPARTURE TIME TABLE
        columns.put("formatted_value", "text not null");
        columns.put("timetable", "integer not null");
        String tableDepartureTime = this.createTable(TABLE_DEPARTURE_TIME, columns);
        tableDepartureTime = this.addForeignKey(tableDepartureTime, "timetable", TABLE_TIMETABLE, Constants.ID);
        tableDepartureTime = this.finishQuery(tableDepartureTime);
        db.execSQL(tableDepartureTime);

        columns.clear();

        //CREATE LINE STOPS TABLE
        columns.put("line", "integer not null");
        columns.put("stop", "integer not null");
        columns.put("direction", "text not null");
        String tableLineStops = this.createTable(TABLE_LINE_STOPS, columns);
        tableLineStops = this.addForeignKey(tableLineStops, "line", TABLE_LINE, Constants.ID);
        tableLineStops = this.addForeignKey(tableLineStops, "stop", TABLE_STOP, Constants.ID);
        tableLineStops = this.finishQuery(tableLineStops);
        db.execSQL(tableLineStops);

        columns.clear();

        //CREATE LINE LOCATIONS TABLE
        columns.put("line", "integer not null");
        columns.put("location", "integer not null");
        columns.put("direction", "text not null");
        String tableLineLocations = this.createTableNoAutoincrementID(TABLE_LINE_LOCATIONS, columns);
        tableLineLocations = this.addPrimaryKeyConstraint(tableLineLocations, "line", "location");
        tableLineLocations = this.addForeignKey(tableLineLocations, "line", TABLE_LINE, Constants.ID);
        tableLineLocations = this.addForeignKey(tableLineLocations, "location", TABLE_LOCATION, Constants.ID);
        tableLineLocations = this.finishQuery(tableLineLocations);
        db.execSQL(tableLineLocations);

        columns.clear();

        //CREATE FAVOURITE LOCATIONS TABLE
        columns.put("title", "text not null");
        columns.put("location", "integer not null");
        String tableFavouriteLocations = this.createTable(TABLE_FAVOURITE_LOCATIONS, columns);
        tableFavouriteLocations = this.addForeignKey(tableFavouriteLocations, "location", TABLE_LOCATION, Constants.ID);
        tableFavouriteLocations = this.finishQuery(tableFavouriteLocations);
        db.execSQL(tableFavouriteLocations);

        columns.clear();

        //CREATE PAST DIRECTIONS TABLE
        columns.put("start_location", "integer not null");
        columns.put("end_location", "integer not null");
        columns.put("date", "text not null");
        String tablePastDirections = this.createTable(TABLE_PAST_DIRECTIONS, columns);
        tablePastDirections = this.addForeignKey(tablePastDirections, "start_location", TABLE_LOCATION, Constants.ID);
        tablePastDirections = this.addForeignKey(tablePastDirections, "end_location", TABLE_LOCATION, Constants.ID);
        tablePastDirections = this.finishQuery(tablePastDirections);
        db.execSQL(tablePastDirections);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ZONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTURE_TIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE_STOPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITE_LOCATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAST_DIRECTIONS);
        this.onCreate(db);
    }

    private String createTable(String tableName, Map<String, String> columns) {
        StringBuilder createTableQuery = new StringBuilder("create table "
                + tableName + "("
                + Constants.ID + " integer primary key autoincrement, ");
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            createTableQuery.append(entry.getKey()).append(" ").append(entry.getValue()).append(", ");
        }
        return createTableQuery.toString();
    }

    private String createTableNoAutoincrementID(String tableName, Map<String, String> columns) {
        StringBuilder createTableQuery = new StringBuilder("create table "
                + tableName + "(");
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            createTableQuery.append(entry.getKey()).append(" ").append(entry.getValue()).append(", ");
        }
        return createTableQuery.toString();
    }

    private String addForeignKey(String query, String referenceColumn, String referencedTable, String referencedColumn) {
        query += " FOREIGN KEY (" + referenceColumn + ") REFERENCES " + referencedTable + " (" + referencedColumn + "), ";
        return query;
    }

    private String addPrimaryKeyConstraint(String query, String firstColumn, String secondColumn) {
        query += " PRIMARY KEY (" + firstColumn + ", " + secondColumn + "), ";
        return query;
    }

    private String finishQuery(String query) {
        return query.substring(0, query.length() - 2) + ")";
    }
}
