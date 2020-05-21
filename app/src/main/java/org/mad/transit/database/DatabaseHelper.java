package org.mad.transit.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    static final String TABLE_COORDINATE = "coordinate";
    static final String TABLE_LINE_STOPS = "line_stops";
    static final String TABLE_LINE_COORDINATES = "line_coordinates";
    static final String ID = "id";
    private static final String DATABASE_NAME = "transit.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Map<String, String> columns = new HashMap<>();

        //CREATE ZONE TABLE
        columns.put("name", "text not null");
        String tableZone = createTable(TABLE_ZONE, columns);
        tableZone = finishQuery(tableZone);
        db.execSQL(tableZone);

        columns.clear();

        //CREATE PRICE LIST TABLE
        columns.put("first_station_zone", "integer not null");
        columns.put("second_station_zone","integer not null");
        columns.put("price", "real not null");
        String tablePriceList = createTable(TABLE_PRICE_LIST, columns);
        tablePriceList = addForeignKey(tablePriceList, "first_station_zone", TABLE_ZONE, ID);
        tablePriceList = addForeignKey(tablePriceList, "second_station_zone", TABLE_ZONE, ID);
        tablePriceList = finishQuery(tablePriceList);
        db.execSQL(tablePriceList);

        columns.clear();

        //CREATE COORDINATE TABLE
        columns.put("latitude","real not null" );
        columns.put("longitude", "real not null");
        String tableCoordinate = createTable(TABLE_COORDINATE, columns);
        tableCoordinate = finishQuery(tableCoordinate);
        db.execSQL(tableCoordinate);

        columns.clear();

        //CREATE STOP TABLE
        columns.put("title", "text not null");
        columns.put("coordinate","integer not null");
        columns.put("zone", "integer not null");
        String tableStop = createTable(TABLE_STOP, columns);
        tableStop = addForeignKey(tableStop, "coordinate", TABLE_COORDINATE, ID);
        tableStop = addForeignKey(tableStop, "zone", TABLE_ZONE, ID);
        tableStop = finishQuery(tableStop);
        db.execSQL(tableStop);

        columns.clear();

        //CREATE LINE TABLE
        columns.put("number", "text not null");
        columns.put("name","text not null");
        columns.put("type", "text");
        String tableLine = createTable(TABLE_LINE, columns);
        tableLine = finishQuery(tableLine);
        db.execSQL(tableLine);

        columns.clear();

        //CREATE TIMETABLE TABLE
        columns.put("line", "integer not null");
        columns.put("direction","text");
        columns.put("day", "text");
        String tableTimetable = createTable(TABLE_TIMETABLE, columns);
        tableTimetable = addForeignKey(tableTimetable, "line", TABLE_LINE, ID);
        tableTimetable = finishQuery(tableTimetable);
        db.execSQL(tableTimetable);

        columns.clear();

        //CREATE DEPARTURE TIME TABLE
        columns.put("hours", "integer");
        columns.put("minutes","integer");
        columns.put("formatted_value", "text not null");
        columns.put("timetable", "integer not null");
        String tableDepartureTime = createTable(TABLE_DEPARTURE_TIME, columns);
        tableDepartureTime = addForeignKey(tableDepartureTime, "timetable", TABLE_TIMETABLE, ID);
        tableDepartureTime = finishQuery(tableDepartureTime);
        db.execSQL(tableDepartureTime);

        columns.clear();

        //CREATE LINE STOPS TABLE
        columns.put("line", "integer not null");
        columns.put("stop","integer not null");
        columns.put("direction", "text not null");
        String tableLineStops = createTableNoAutoincrementID(TABLE_LINE_STOPS, columns);
        tableLineStops = addPrimaryKeyConstraint(tableLineStops, "line", "stop");
        tableLineStops = addForeignKey(tableLineStops, "line",TABLE_LINE, ID);
        tableLineStops = addForeignKey(tableLineStops, "stop", TABLE_STOP, ID);
        tableLineStops = finishQuery(tableLineStops);
        db.execSQL(tableLineStops);

        columns.clear();

        //CREATE LINE COORDINATES TABLE
        columns.put("line", "integer not null");
        columns.put("coordinate","integer not null");
        String tableLineCoordinate = createTableNoAutoincrementID(TABLE_LINE_COORDINATES, columns);
        tableLineCoordinate = addPrimaryKeyConstraint(tableLineCoordinate, "line", "coordinate");
        tableLineCoordinate = addForeignKey(tableLineCoordinate, "line",TABLE_LINE, ID);
        tableLineCoordinate = addForeignKey(tableLineCoordinate, "coordinate", TABLE_COORDINATE, ID);
        tableLineCoordinate = finishQuery(tableLineCoordinate);
        db.execSQL(tableLineCoordinate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ZONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRICE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMETABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTURE_TIME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COORDINATE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE_STOPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINE_COORDINATES);
        onCreate(db);
    }

    private String createTable(String tableName, Map<String, String> columns){
        StringBuilder createTableQuery = new StringBuilder("create table "
                + tableName + "("
                + ID + " integer primary key autoincrement, ");
        for (Map.Entry<String, String> entry: columns.entrySet()){
            createTableQuery.append(entry.getKey()).append(" ").append(entry.getValue()).append(", ");
        }
        return  createTableQuery.toString();
    }

    private String createTableNoAutoincrementID(String tableName, Map<String, String> columns){
        StringBuilder createTableQuery = new StringBuilder("create table "
                + tableName + "(");
        for (Map.Entry<String, String> entry: columns.entrySet()){
            createTableQuery.append(entry.getKey()).append(" ").append(entry.getValue()).append(", ");
        }
        return  createTableQuery.toString();
    }

    private String addForeignKey(String query, String referenceColumn, String referencedTable, String referencedColumn){
        query += " FOREIGN KEY ("+referenceColumn+") REFERENCES "+ referencedTable +" ("+ referencedColumn+"), ";
        return query;
    }

    private String addPrimaryKeyConstraint(String query, String firstColumn, String secondColumn){
        query += " PRIMARY KEY (" + firstColumn + ", " + secondColumn + "), ";
        return query;
    }

    private String finishQuery(String query){
        return query.substring(0, query.length() - 2) + ")";
    }

    /*public void addOne(Stop stop){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("title", stop.getTitle());
        contentValues.put("latitude", stop.getLatitude());
        contentValues.put("longitude", stop.getLongitude());

        database.insert(TABLE_STOP, null, contentValues);
        database.close();
    }*/
}
