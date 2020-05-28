package org.mad.transit.sync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.database.DBContentProvider;
import org.mad.transit.database.DatabaseHelper;
import org.mad.transit.dto.LineDto;
import org.mad.transit.dto.LineStopsDto;
import org.mad.transit.dto.LineTimetableDto;

import java.io.IOException;
import java.util.List;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitializeDatabaseTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private final TaskListener taskListener;
    private boolean lineCoordinatesFinished;
    private boolean stopsFinished;
    private boolean timeTablesFinished;


    public InitializeDatabaseTask(Context context, TaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
    }

    @SneakyThrows
    @Override
    protected Void doInBackground(Void... voids) {
        this.initializeDB();
        return null;
    }

    private void initializeDB() throws IOException {

        //LINES
        Call<List<LineDto>> callLines = ServiceUtils.transitRestApi.getLines();
        Response<List<LineDto>> responseLines = callLines.execute();

        if (!responseLines.isSuccessful()){
            Log.e("code","Code: " + responseLines.code());
            return;
        }
        List<LineDto> lines = responseLines.body();

        ContentValues[] contentValues = new ContentValues[lines.size()];
        ContentValues entry;
        for (int i = 0; i < lines.size(); i++) {
            entry = new ContentValues();
            entry.put("name", lines.get(i).getTitle());
            entry.put("number", lines.get(i).getName());
            entry.put("type", lines.get(i).getType());
            contentValues[i] = entry;
        }
        context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_LINE, contentValues);
        Log.i("TraNSit", "GET LINES FINISHED");

        //LOCATIONS, LINE-LOCATIONS
        Call<List<LineDto>> callLineLocations = ServiceUtils.transitRestApi.getLinesCoordinates();
        callLineLocations.enqueue(new Callback<List<LineDto>>() {
            @Override
            public void onResponse(Call<List<LineDto>> call, Response<List<LineDto>> response) {
                if (!response.isSuccessful()){
                    Log.e("code","Code: " + response.code());
                    return;
                }

                DBContentProvider.database.beginTransaction();

                List<LineDto> lines = response.body();
                ContentValues entry;
                for (LineDto line: lines) {
                    ContentValues[] contentValues = new ContentValues[line.getCoordinates().size()];
                    for (int i = 0; i < line.getCoordinates().size(); i++) {
                        entry = new ContentValues();
                        entry.put("latitude", Double.parseDouble(line.getCoordinates().get(i).getLat()));
                        entry.put("longitude", Double.parseDouble(line.getCoordinates().get(i).getLon()));
                        entry.put("lineNumber", line.getName());
                        entry.put("lineDirection", line.getDirection());
                        contentValues[i] = entry;
                    }
                    context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
                }
                Log.i("TraNSit", "LINE COORDINATES FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                lineCoordinatesFinished = true;
                tryToFinishTask();
            }

            @Override
            public void onFailure(Call<List<LineDto>> call, Throwable t) {
                Log.e("message", t.getMessage() != null?t.getMessage():"error");
            }
        });

        //ZONE, PRICE LIST
        /*ContentValues entry = new ContentValues();
        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone1Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone2Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone3Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone4Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone2Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone3Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone4Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone3Id);
        entry.put("second_station_zone", zone3Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone3Id);
        entry.put("second_station_zone", zone4Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone4Id);
        entry.put("second_station_zone", zone4Id);
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);*/


        //STOPS, LINE-STOPS, LOCATIONS
        Call<List<LineStopsDto>> callStops = ServiceUtils.transitRestApi.getStops();
        callStops.enqueue(new Callback<List<LineStopsDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineStopsDto>> call, @NotNull Response<List<LineStopsDto>> response) {
                if (!response.isSuccessful()){
                    Log.e("code","Code: " + response.code());
                    return;
                }

                DBContentProvider.database.beginTransaction();

                List<LineStopsDto> lineStops = response.body();
                for (LineStopsDto lineStop: lineStops){
                    ContentValues[] contentValues = new ContentValues[lineStop.getStops().size()];
                    for (int i = 0; i < lineStop.getStops().size(); i++) {
                        ContentValues entry = new ContentValues();
                        entry.put("direction", lineStop.getDirection());
                        entry.put("lineNumber", lineStop.getName());
                        entry.put("zone", lineStop.getStops().get(i).getZone());
                        entry.put("latitude", lineStop.getStops().get(i).getLat());
                        entry.put("longitude", lineStop.getStops().get(i).getLon());
                        entry.put("title", lineStop.getStops().get(i).getName());
                        contentValues[i] = entry;
                    }
                    context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_STOP, contentValues);
                }
                Log.i("TraNSit", "GET STOPS FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                stopsFinished = true;
                tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineStopsDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null?t.getMessage():"error");
            }
        });

        //TIMETABLE, DEPARTURE TIME
        Call<List<LineTimetableDto>> callTimetables = ServiceUtils.transitRestApi.getTimeTables();
        callTimetables.enqueue(new Callback<List<LineTimetableDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineTimetableDto>> call, @NotNull Response<List<LineTimetableDto>> response) {
                if (!response.isSuccessful()){
                    Log.e("code","Code: " + response.code());
                    return;
                }

                DBContentProvider.database.beginTransaction();

                List<LineTimetableDto> lineTimetables = response.body();
                for (LineTimetableDto lineTimetable: lineTimetables){
                    String [] projection = {
                            DatabaseHelper.ID
                    };
                    String [] selectionArgs = {
                            lineTimetable.getName()
                    };
                    Cursor cursor = context.getContentResolver().query(DBContentProvider.CONTENT_URI_LINE, projection, "number = ?", selectionArgs, null);
                    long lineId;
                    if (cursor != null) {
                        cursor.moveToFirst();
                        lineId = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.ID));
                        cursor.close();
                    }else{
                        Log.e("traNSit","Cursor is null");
                        return;
                    }

                    ContentValues entry;
                    if (lineTimetable.getTimeTable().getWorkday() != null) {
                        if(!lineTimetable.getTimeTable().getWorkday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "WORKDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getWorkday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getWorkday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getWorkday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                    if (lineTimetable.getTimeTable().getSaturday() != null) {
                        if (!lineTimetable.getTimeTable().getSaturday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "SATURDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSaturday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getSaturday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getSaturday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                    if (lineTimetable.getTimeTable().getSunday() != null) {
                        if (!lineTimetable.getTimeTable().getSunday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "SUNDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSunday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getSunday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getSunday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                }
                Log.i("TraNSit", "GET TIMETABLE FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                timeTablesFinished = true;
                tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineTimetableDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null?t.getMessage():"error");
            }
        });
    }

    private void tryToFinishTask() {
        if (lineCoordinatesFinished && stopsFinished && timeTablesFinished && taskListener != null) {
            taskListener.onFinished();
        }
    }

    public interface TaskListener {
        void onFinished();
    }
}
