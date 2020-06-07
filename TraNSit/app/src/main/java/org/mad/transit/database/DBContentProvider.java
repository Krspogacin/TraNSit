package org.mad.transit.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mad.transit.model.Location;
import org.mad.transit.model.Stop;
import org.mad.transit.model.Zone;
import org.mad.transit.sync.InitializeDatabaseTask;
import org.mad.transit.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class DBContentProvider extends ContentProvider {
    public static SQLiteDatabase database;

    //Authority is symbolic name of your provider
    public static final String AUTHORITY = "org.mad.transit";

    private static final int STOP = 1;
    private static final int STOP_ID = 2;
    private static final int LINE = 3;
    private static final int LINE_ID = 4;
    private static final int ZONE = 5;
    private static final int PRICE_LIST = 6;
    private static final int TIMETABLE = 7;
    private static final int DEPARTURE_TIME = 8;
    private static final int LOCATION = 9;
    private static final int LINE_STOPS = 10;
    private static final int LINE_LOCATIONS = 11;
    private static final int FAVOURITE_LOCATIONS = 12;
    private static final int PAST_DIRECTIONS = 13;

    public static final Uri CONTENT_URI_STOP = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_STOP);
    public static final Uri CONTENT_URI_LINE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE);
    public static final Uri CONTENT_URI_ZONE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_ZONE);
    public static final Uri CONTENT_URI_PRICE_LIST = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_PRICE_LIST);
    public static final Uri CONTENT_URI_TIMETABLE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_TIMETABLE);
    public static final Uri CONTENT_URI_DEPARTURE_TIME = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_DEPARTURE_TIME);
    public static final Uri CONTENT_URI_LOCATION = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LOCATION);
    public static final Uri CONTENT_URI_LINE_STOPS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_STOPS);
    public static final Uri CONTENT_URI_LINE_LOCATIONS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_LOCATIONS);
    public static final Uri CONTENT_URI_FAVOURITE_LOCATIONS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_FAVOURITE_LOCATIONS);
    public static final Uri CONTENT_URI_PAST_DIRECTIONS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_PAST_DIRECTIONS);

    //a content URI pattern matches content URIs using wildcard characters
    //*: Matches a String of any valid characters of any length
    //#: Matches a string of numeric characters of any length
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_STOP, STOP);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_STOP + "/#", STOP_ID);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE, LINE);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE + "/#", LINE_ID);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_ZONE, ZONE);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_PRICE_LIST, PRICE_LIST);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_TIMETABLE, TIMETABLE);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_DEPARTURE_TIME, DEPARTURE_TIME);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LOCATION, LOCATION);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE_STOPS, LINE_STOPS);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE_LOCATIONS, LINE_LOCATIONS);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_FAVOURITE_LOCATIONS, FAVOURITE_LOCATIONS);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_PAST_DIRECTIONS, PAST_DIRECTIONS);
    }

    private final List<Location> alreadySavedLocations = new ArrayList<>();
    private final List<Stop> alreadySavedStops = new ArrayList<>();
    private final List<Zone> alreadySavedZones = new ArrayList<>();

    @Override
    public boolean onCreate() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this.getContext());
        database = databaseHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case STOP_ID:
                queryBuilder.appendWhere(Constants.ID + "=" + uri.getLastPathSegment());
                queryBuilder.setTables(DatabaseHelper.TABLE_STOP);
                break;
            case STOP:
                queryBuilder.setTables(DatabaseHelper.TABLE_STOP);
                break;
            case LINE_ID:
                queryBuilder.appendWhere(Constants.ID + "=" + uri.getLastPathSegment());
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE);
                break;
            case LINE:
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE);
                break;
            case ZONE:
                queryBuilder.setTables(DatabaseHelper.TABLE_ZONE);
                break;
            case PRICE_LIST:
                queryBuilder.setTables(DatabaseHelper.TABLE_PRICE_LIST);
                break;
            case TIMETABLE:
                queryBuilder.setTables(DatabaseHelper.TABLE_TIMETABLE);
                break;
            case DEPARTURE_TIME:
                queryBuilder.setTables(DatabaseHelper.TABLE_DEPARTURE_TIME);
                break;
            case LOCATION:
                queryBuilder.setTables(DatabaseHelper.TABLE_LOCATION);
                break;
            case LINE_STOPS:
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE_STOPS);
                break;
            case LINE_LOCATIONS:
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE_LOCATIONS);
                break;
            case FAVOURITE_LOCATIONS:
                queryBuilder.setTables(DatabaseHelper.TABLE_FAVOURITE_LOCATIONS);
                break;
            case PAST_DIRECTIONS:
                queryBuilder.setTables(DatabaseHelper.TABLE_PAST_DIRECTIONS);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(this.getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri retVal;
        int uriType = sURIMatcher.match(uri);
        long id;
        switch (uriType) {
            case STOP:
                id = database.insert(DatabaseHelper.TABLE_STOP, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_STOP + "/" + id);
                break;
            case LINE:
                id = database.insert(DatabaseHelper.TABLE_LINE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE + "/" + id);
                break;
            case ZONE:
                id = database.insert(DatabaseHelper.TABLE_ZONE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_ZONE + "/" + id);
                break;
            case PRICE_LIST:
                id = database.insert(DatabaseHelper.TABLE_PRICE_LIST, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_PRICE_LIST + "/" + id);
                break;
            case TIMETABLE:
                id = database.insert(DatabaseHelper.TABLE_TIMETABLE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_TIMETABLE + "/" + id);
                break;
            case DEPARTURE_TIME:
                id = database.insert(DatabaseHelper.TABLE_DEPARTURE_TIME, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_DEPARTURE_TIME + "/" + id);
                break;
            case LOCATION:
                id = database.insert(DatabaseHelper.TABLE_LOCATION, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LOCATION + "/" + id);
                break;
            case LINE_STOPS:
                id = database.insert(DatabaseHelper.TABLE_LINE_STOPS, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE_STOPS + "/" + id);
                break;
            case LINE_LOCATIONS:
                id = database.insert(DatabaseHelper.TABLE_LINE_LOCATIONS, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE_LOCATIONS + "/" + id);
                break;
            case FAVOURITE_LOCATIONS:
                id = database.insert(DatabaseHelper.TABLE_FAVOURITE_LOCATIONS, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_FAVOURITE_LOCATIONS + "/" + id);
                break;
            case PAST_DIRECTIONS:
                id = database.insert(DatabaseHelper.TABLE_PAST_DIRECTIONS, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_PAST_DIRECTIONS + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        this.getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @Nullable ContentValues[] values) {
        int uriType = sURIMatcher.match(uri);
        if (values != null) {
            switch (uriType) {
                case LINE:
                    database.beginTransaction();
                    for (ContentValues entry : values) {
                        String lineNumber = entry.getAsString(Constants.NUMBER);
                        if (!InitializeDatabaseTask.lineIdsMap.containsKey(lineNumber)) {
                            long id = database.insert(DatabaseHelper.TABLE_LINE, null, entry);
                            InitializeDatabaseTask.lineIdsMap.put(lineNumber, id);
                        }
                    }
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    break;
                case LOCATION:
                    String sqlLocationInsert = "insert into location (latitude, longitude) values (?, ?);";
                    SQLiteStatement stmtLocationInsert = database.compileStatement(sqlLocationInsert);

                    String sqlLineLocationsInsert = "insert into line_locations (line, location, direction) values (?, ?, ?);";
                    SQLiteStatement stmtLineLocationsInsert = database.compileStatement(sqlLineLocationsInsert);

                    for (ContentValues entry : values) {
                        stmtLocationInsert.bindDouble(1, entry.getAsDouble(Constants.LATITUDE));
                        stmtLocationInsert.bindDouble(2, entry.getAsDouble(Constants.LONGITUDE));

                        long locationId = stmtLocationInsert.executeInsert();
                        stmtLocationInsert.clearBindings();

                        stmtLineLocationsInsert.bindLong(1, entry.getAsLong(Constants.LINE_ID));
                        stmtLineLocationsInsert.bindLong(2, locationId);
                        stmtLineLocationsInsert.bindString(3, entry.getAsString(Constants.LINE_DIRECTION));
                        stmtLineLocationsInsert.executeInsert();
                        stmtLineLocationsInsert.clearBindings();
                    }
                    break;
                case STOP:
                    String sqlLocationInsert2 = "insert into location (latitude, longitude) values (?, ?);";
                    SQLiteStatement stmtLocationInsert2 = database.compileStatement(sqlLocationInsert2);

                    String sqlZoneInsert = "insert into zone (name) values (?);";
                    SQLiteStatement stmtZoneInsert = database.compileStatement(sqlZoneInsert);

                    String sqlStopInsert = "insert into stop (title, location, zone) values (?, ?, ?);";
                    SQLiteStatement stmtStopInsert = database.compileStatement(sqlStopInsert);

                    String sqlLineStopsInsert = "insert into line_stops (line, stop, direction) values (?, ?, ?);";
                    SQLiteStatement stmtLineStopsInsert = database.compileStatement(sqlLineStopsInsert);

                    for (ContentValues entry : values) {
                        Location location = Location.builder()
                                .latitude(Double.parseDouble(entry.getAsString(Constants.LATITUDE)))
                                .longitude(Double.parseDouble(entry.getAsString(Constants.LONGITUDE)))
                                .build();

                        long locationId;
                        if (this.alreadySavedLocations.contains(location)) {
                            locationId = this.alreadySavedLocations.get(this.alreadySavedLocations.indexOf(location)).getId();
                        } else {
                            stmtLocationInsert2.bindDouble(1, entry.getAsDouble(Constants.LATITUDE));
                            stmtLocationInsert2.bindDouble(2, entry.getAsDouble(Constants.LONGITUDE));
                            locationId = stmtLocationInsert2.executeInsert();
                            stmtLocationInsert2.clearBindings();

                            location.setId(locationId);
                            this.alreadySavedLocations.add(location);
                        }

                        Zone zone = new Zone();
                        zone.setName(entry.getAsString(Constants.ZONE));

                        long zoneId;
                        if (this.alreadySavedZones.contains(zone)) {
                            zoneId = this.alreadySavedZones.get(this.alreadySavedZones.indexOf(zone)).getId();
                        } else {
                            stmtZoneInsert.bindString(1, entry.getAsString(Constants.ZONE));
                            zoneId = stmtZoneInsert.executeInsert();
                            stmtZoneInsert.clearBindings();

                            zone.setId(zoneId);
                            this.alreadySavedZones.add(zone);
                        }

                        Stop stop = new Stop();
                        stop.setLocation(location);

                        long stopId;
                        if (this.alreadySavedStops.contains(stop)) {
                            stopId = this.alreadySavedStops.get(this.alreadySavedStops.indexOf(stop)).getId();
                        } else {
                            stmtStopInsert.bindString(1, entry.getAsString(Constants.TITLE));
                            stmtStopInsert.bindLong(2, locationId);
                            stmtStopInsert.bindLong(3, zoneId);
                            stopId = stmtStopInsert.executeInsert();
                            stmtStopInsert.clearBindings();

                            stop.setId(stopId);
                            this.alreadySavedStops.add(stop);
                        }

                        stmtLineStopsInsert.bindLong(1, entry.getAsLong(Constants.LINE_ID));
                        stmtLineStopsInsert.bindLong(2, stopId);
                        stmtLineStopsInsert.bindString(3, entry.getAsString(Constants.DIRECTION));
                        stmtLineStopsInsert.executeInsert();
                        stmtLineStopsInsert.clearBindings();
                    }
                    break;
                case DEPARTURE_TIME:
                    String sqlDepartureTimeInsert = "insert into departure_time (formatted_value, timetable) values (?, ?);";
                    SQLiteStatement stmtDepartureTimeInsert = database.compileStatement(sqlDepartureTimeInsert);

                    for (ContentValues entry : values) {
                        stmtDepartureTimeInsert.bindString(1, entry.getAsString(Constants.FORMATTED_VALUE));
                        stmtDepartureTimeInsert.bindLong(2, entry.getAsLong(Constants.TIMETABLE));
                        stmtDepartureTimeInsert.executeInsert();
                        stmtDepartureTimeInsert.clearBindings();
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsDeleted;
        String id;
        switch (uriType) {
            case STOP:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_STOP,
                        selection,
                        selectionArgs);
                break;
            case STOP_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = database.delete(DatabaseHelper.TABLE_STOP,
                        Constants.ID + "=" + id,
                        null);
                break;
            case LINE:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_LINE,
                        selection,
                        selectionArgs);
                break;
            case LINE_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = database.delete(DatabaseHelper.TABLE_LINE,
                        Constants.ID + "=" + id,
                        null);
                break;
            case ZONE:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_ZONE,
                        selection,
                        selectionArgs);
                break;
            case PRICE_LIST:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_PRICE_LIST,
                        selection,
                        selectionArgs);
                break;
            case TIMETABLE:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_TIMETABLE,
                        selection,
                        selectionArgs);
                break;
            case DEPARTURE_TIME:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_DEPARTURE_TIME,
                        selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_LOCATION,
                        selection,
                        selectionArgs);
                break;
            case LINE_STOPS:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_LINE_STOPS,
                        selection,
                        selectionArgs);
                break;
            case LINE_LOCATIONS:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_LINE_LOCATIONS,
                        selection,
                        selectionArgs);
                break;
            case FAVOURITE_LOCATIONS:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_FAVOURITE_LOCATIONS,
                        selection,
                        selectionArgs);
                break;
            case PAST_DIRECTIONS:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_PAST_DIRECTIONS,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        this.getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsUpdated;
        String id;
        switch (uriType) {
            case STOP:
                rowsUpdated = database.update(DatabaseHelper.TABLE_STOP,
                        values,
                        selection,
                        selectionArgs);
                break;
            case STOP_ID:
                id = uri.getLastPathSegment();
                rowsUpdated = database.update(DatabaseHelper.TABLE_STOP,
                        values,
                        Constants.ID + "=" + id,
                        null);
                break;
            case LINE:
                rowsUpdated = database.update(DatabaseHelper.TABLE_LINE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_ID:
                id = uri.getLastPathSegment();
                rowsUpdated = database.update(DatabaseHelper.TABLE_LINE,
                        values,
                        Constants.ID + "=" + id,
                        null);
                break;
            case ZONE:
                rowsUpdated = database.update(DatabaseHelper.TABLE_ZONE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PRICE_LIST:
                rowsUpdated = database.update(DatabaseHelper.TABLE_PRICE_LIST,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TIMETABLE:
                rowsUpdated = database.update(DatabaseHelper.TABLE_TIMETABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case DEPARTURE_TIME:
                rowsUpdated = database.update(DatabaseHelper.TABLE_DEPARTURE_TIME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = database.update(DatabaseHelper.TABLE_LOCATION,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_STOPS:
                rowsUpdated = database.update(DatabaseHelper.TABLE_LINE_STOPS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_LOCATIONS:
                rowsUpdated = database.update(DatabaseHelper.TABLE_LINE_LOCATIONS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case FAVOURITE_LOCATIONS:
                rowsUpdated = database.update(DatabaseHelper.TABLE_FAVOURITE_LOCATIONS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PAST_DIRECTIONS:
                rowsUpdated = database.update(DatabaseHelper.TABLE_PAST_DIRECTIONS,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        this.getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
