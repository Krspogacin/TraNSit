package org.mad.transit.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DBContentProvider extends ContentProvider {
    private DatabaseHelper database;

    //Authority is symbolic name of your provider
    private static final String AUTHORITY = "org.mad.transit";

    private static final int STOP = 1;
    private static final int STOP_ID = 2;
    private static final int LINE = 3;
    private static final int LINE_ID = 4;
    private static final int ZONE = 5;
    private static final int PRICE_LIST = 6;
    private static final int TIMETABLE = 7;
    private static final int DEPARTURE_TIME = 8;
    private static final int COORDINATE = 9;
    private static final int LINE_STOPS = 10;
    private static final int LINE_COORDINATES = 11;

    public static final Uri CONTENT_URI_STOP = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_STOP);
    public static final Uri CONTENT_URI_LINE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE);
    public static final Uri CONTENT_URI_ZONE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_ZONE);
    public static final Uri CONTENT_URI_PRICE_LIST = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_PRICE_LIST);
    public static final Uri CONTENT_URI_TIMETABLE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_TIMETABLE);
    public static final Uri CONTENT_URI_DEPARTURE_TIME = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_DEPARTURE_TIME);
    public static final Uri CONTENT_URI_COORDINATE = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_COORDINATE);
    public static final Uri CONTENT_URI_LINE_STOPS = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_STOPS);
    public static final Uri CONTENT_URI_LINE_COORDINATES = Uri.parse("content://" + AUTHORITY + "/" + DatabaseHelper.TABLE_LINE_COORDINATES);

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
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_COORDINATE, COORDINATE);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE_STOPS, LINE_STOPS);
        sURIMatcher.addURI(AUTHORITY, DatabaseHelper.TABLE_LINE_COORDINATES, LINE_COORDINATES);
    }

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext());
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
            case COORDINATE:
                queryBuilder.setTables(DatabaseHelper.TABLE_COORDINATE);
                break;
            case LINE_STOPS:
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE_STOPS);
                break;
            case LINE_COORDINATES:
                queryBuilder.setTables(DatabaseHelper.TABLE_LINE_COORDINATES);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

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
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id;
        switch (uriType) {
            case STOP:
                id = sqlDB.insert(DatabaseHelper.TABLE_STOP, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_STOP + "/" + id);
                break;
            case LINE:
                id = sqlDB.insert(DatabaseHelper.TABLE_LINE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE + "/" + id);
                break;
            case ZONE:
                id = sqlDB.insert(DatabaseHelper.TABLE_ZONE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_ZONE + "/" + id);
                break;
            case PRICE_LIST:
                id = sqlDB.insert(DatabaseHelper.TABLE_PRICE_LIST, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_PRICE_LIST + "/" + id);
                break;
            case TIMETABLE:
                id = sqlDB.insert(DatabaseHelper.TABLE_TIMETABLE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_TIMETABLE + "/" + id);
                break;
            case DEPARTURE_TIME:
                id = sqlDB.insert(DatabaseHelper.TABLE_DEPARTURE_TIME, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_DEPARTURE_TIME + "/" + id);
                break;
            case COORDINATE:
                id = sqlDB.insert(DatabaseHelper.TABLE_COORDINATE, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_COORDINATE + "/" + id);
                break;
            case LINE_STOPS:
                id = sqlDB.insert(DatabaseHelper.TABLE_LINE_STOPS, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE_STOPS + "/" + id);
                break;
            case LINE_COORDINATES:
                id = sqlDB.insert(DatabaseHelper.TABLE_LINE_COORDINATES, null, values);
                retVal = Uri.parse(DatabaseHelper.TABLE_LINE_COORDINATES + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        String id;
        switch (uriType) {
            case STOP:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_STOP,
                        selection,
                        selectionArgs);
                break;
            case STOP_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_STOP,
                        DatabaseHelper.ID + "=" + id,
                        null);
                break;
            case LINE:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_LINE,
                        selection,
                        selectionArgs);
                break;
            case LINE_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_LINE,
                        DatabaseHelper.ID + "=" + id,
                        null);
                break;
            case ZONE:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_ZONE,
                        selection,
                        selectionArgs);
                break;
            case PRICE_LIST:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_PRICE_LIST,
                        selection,
                        selectionArgs);
                break;
            case TIMETABLE:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_TIMETABLE,
                        selection,
                        selectionArgs);
                break;
            case DEPARTURE_TIME:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_DEPARTURE_TIME,
                        selection,
                        selectionArgs);
                break;
            case COORDINATE:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_COORDINATE,
                        selection,
                        selectionArgs);
                break;
            case LINE_STOPS:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_LINE_STOPS,
                        selection,
                        selectionArgs);
                break;
            case LINE_COORDINATES:
                rowsDeleted = sqlDB.delete(DatabaseHelper.TABLE_LINE_COORDINATES,
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
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        String id;
        switch (uriType) {
            case STOP:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_STOP,
                        values,
                        selection,
                        selectionArgs);
                break;
            case STOP_ID:
                id = uri.getLastPathSegment();
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_STOP,
                        values,
                        DatabaseHelper.ID + "=" + id,
                        null);
                break;
            case LINE:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_LINE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_ID:
                id = uri.getLastPathSegment();
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_LINE,
                        values,
                        DatabaseHelper.ID + "=" + id,
                        null);
                break;
            case ZONE:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_ZONE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case PRICE_LIST:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_PRICE_LIST,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TIMETABLE:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_TIMETABLE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case DEPARTURE_TIME:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_DEPARTURE_TIME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case COORDINATE:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_COORDINATE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_STOPS:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_LINE_STOPS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case LINE_COORDINATES:
                rowsUpdated = sqlDB.update(DatabaseHelper.TABLE_LINE_COORDINATES,
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
