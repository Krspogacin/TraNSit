package org.mad.transit.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public static final Uri CONTENT_URI_STOP = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_STOP);
    public static final Uri CONTENT_URI_LINE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE);
    public static final Uri CONTENT_URI_ZONE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_ZONE);
    public static final Uri CONTENT_URI_PRICE_LIST = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_PRICE_LIST);
    public static final Uri CONTENT_URI_TIMETABLE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_TIMETABLE);
    public static final Uri CONTENT_URI_DEPARTURE_TIME = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_DEPARTURE_TIME);
    public static final Uri CONTENT_URI_LOCATION = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LOCATION);
    public static final Uri CONTENT_URI_LINE_STOPS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_STOPS);
    public static final Uri CONTENT_URI_LINE_LOCATIONS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_LOCATIONS);

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
    }

    @Override
    public boolean onCreate() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        database =  databaseHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case STOP_ID:
                queryBuilder.appendWhere(DatabaseHelper.ID + "=" + uri.getLastPathSegment());
                queryBuilder.setTables(DatabaseHelper.TABLE_STOP);
                break;
            case STOP:
                queryBuilder.setTables(DatabaseHelper.TABLE_STOP);
                break;
            case LINE_ID:
                queryBuilder.appendWhere(DatabaseHelper.ID + "=" + uri.getLastPathSegment());
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);

        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @Nullable ContentValues[] values){
        int uriType = sURIMatcher.match(uri);
        if (values != null) {
            switch (uriType) {
                case LINE:
                    database.beginTransaction();
                    for (ContentValues entry : values) {
                        String[] projection = {
                                DatabaseHelper.ID
                        };
                        String[] selectionArgs = {
                                entry.getAsString("number")
                        };
                        Cursor cursor = database.query(DatabaseHelper.TABLE_LINE, projection, "number = ?", selectionArgs, null, null, null);
                        if (cursor.getCount() > 0) {
                            continue;
                        } else {
                            database.insert(DatabaseHelper.TABLE_LINE, null, entry);
                        }
                        cursor.close();
                    }
                    database.setTransactionSuccessful();
                    database.endTransaction();
                    break;
                case LOCATION:
                    for (ContentValues entry : values) {
                        String lineNumber = entry.getAsString("lineNumber");
                        String[] projection = {
                                DatabaseHelper.ID
                        };
                        String[] selectionArgs = {
                                lineNumber
                        };
                        Cursor cursor = database.query(DatabaseHelper.TABLE_LINE, projection, "number = ?", selectionArgs, null, null, null);
                        long lineId;
                        if (cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            lineId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
                        } else {
                            Log.e("TraNSit", "No line with number:" + lineNumber);
                            break;
                        }
                        cursor.close();

                        entry.remove("lineNumber");
                        String lineDirection = entry.getAsString("lineDirection");
                        entry.remove("lineDirection");

                        long locationId = database.insert(DatabaseHelper.TABLE_LOCATION, null, entry);

                        entry = new ContentValues();
                        entry.put("location", locationId);
                        entry.put("line", lineId);
                        entry.put("direction", lineDirection);

                        database.insert(DatabaseHelper.TABLE_LINE_LOCATIONS, null, entry);
                    }
                    break;
                case STOP:
                    for (ContentValues entry : values) {
                        ContentValues locationEntry = new ContentValues();
                        String latitude = entry.getAsString("latitude");
                        String longitude = entry.getAsString("longitude");
                        locationEntry.put("latitude", Double.parseDouble(latitude));
                        locationEntry.put("longitude", Double.parseDouble(longitude));
                        entry.remove("latitude");
                        entry.remove("longitude");

                        String [] projectionLocation = {
                                DatabaseHelper.ID
                        };
                        String [] selectionArgsLocation = {
                            latitude, longitude
                        };
                        Cursor cursorLocation = database.query(DatabaseHelper.TABLE_LOCATION, projectionLocation, "latitude = ? and longitude = ?", selectionArgsLocation, null, null, null);
                        Long locationId;
                        if (cursorLocation.getCount() > 0) {
                            cursorLocation.moveToFirst();
                            locationId = cursorLocation.getLong(cursorLocation.getColumnIndex(DatabaseHelper.ID));
                        }else{
                            locationId = database.insert(DatabaseHelper.TABLE_LOCATION, null, locationEntry);
                        }
                        cursorLocation.close();
                        entry.put("location", locationId);

                        String zone = entry.getAsString("zone");
                        entry.remove("zone");
                        String [] projectionZone = {
                                DatabaseHelper.ID
                        };
                        String [] selectionArgsZone = {
                                zone
                        };
                        Cursor cursorZone = database.query(DatabaseHelper.TABLE_ZONE, projectionZone, "name = ?", selectionArgsZone, null, null, null);
                        long zoneId;
                        if (cursorZone.getCount() > 0) {
                            cursorZone.moveToFirst();
                            zoneId = cursorZone.getLong(cursorZone.getColumnIndex(DatabaseHelper.ID));
                        }else{
                            ContentValues entryZone = new ContentValues();
                            entryZone.put("name", zone);
                            zoneId = database.insert(DatabaseHelper.TABLE_ZONE, null, entryZone);
                        }
                        cursorZone.close();
                        entry.put("zone", zoneId);

                        String lineNumber = entry.getAsString("lineNumber");
                        entry.remove("lineNumber");
                        String [] projectionLine = {
                                DatabaseHelper.ID
                        };
                        String [] selectionArgsLine = {
                                lineNumber
                        };
                        Cursor cursorLine = database.query(DatabaseHelper.TABLE_LINE, projectionLine, "number = ?", selectionArgsLine, null, null, null);
                        long lineId;
                        if (cursorLine.getCount() > 0) {
                            cursorLine.moveToFirst();
                            lineId = cursorLine.getLong(cursorLine.getColumnIndex(DatabaseHelper.ID));
                        }else{
                            Log.e("TraNSit", "No line with number:" + lineNumber);
                            break;
                        }
                        cursorLine.close();

                        String lineDirection = entry.getAsString("direction");
                        entry.remove("direction");

                        String [] projectionStop = {
                                DatabaseHelper.ID
                        };
                        String [] selectionArgsStop = {
                                locationId.toString()
                        };
                        Cursor cursorStop = database.query(DatabaseHelper.TABLE_STOP, projectionStop, "location = ?", selectionArgsStop, null, null, null);
                        long stopId;
                        if (cursorStop.getCount() > 0) {
                            cursorStop.moveToFirst();
                            stopId = cursorStop.getLong(cursorStop.getColumnIndex(DatabaseHelper.ID));
                        }else{
                            stopId = database.insert(DatabaseHelper.TABLE_STOP, null, entry);
                        }
                        cursorStop.close();

                        entry = new ContentValues();
                        entry.put("line", lineId);
                        entry.put("stop", stopId);
                        entry.put("direction", lineDirection);

                        database.insert(DatabaseHelper.TABLE_LINE_STOPS, null, entry);
                    }
                    break;
                case DEPARTURE_TIME:
                    for (ContentValues entry : values) {
                        database.insert(DatabaseHelper.TABLE_DEPARTURE_TIME, null, entry);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        return 0;
    }

    /*@NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull String authority, @NonNull ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        ContentProviderResult[] results = new ContentProviderResult[operations
                .size()];
        // Begin a transaction
        database.beginTransaction();
        try {
            results = super.applyBatch(operations);
            database.setTransactionSuccessful();
        } catch (OperationApplicationException e) {
            Log.d("TAG", "batch failed: " + e.getLocalizedMessage());
        } finally {
            database.endTransaction();
        }

        return results;
    }*/

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
                        DatabaseHelper.ID + "=" + id,
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
                        DatabaseHelper.ID + "=" + id,
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
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
                        DatabaseHelper.ID + "=" + id,
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
                        DatabaseHelper.ID + "=" + id,
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
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
