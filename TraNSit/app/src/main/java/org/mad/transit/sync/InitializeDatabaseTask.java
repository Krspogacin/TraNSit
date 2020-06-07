package org.mad.transit.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.database.DBContentProvider;
import org.mad.transit.dto.LineDto;
import org.mad.transit.dto.LineStopsDto;
import org.mad.transit.dto.LineTimetableDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InitializeDatabaseTask extends AsyncTask<Void, Void, Void> {
    private final ContentResolver contentResolver;
    private final TaskListener taskListener;
    private boolean lineCoordinatesFinished;
    private boolean stopsFinished;
    private boolean timeTablesFinished;
    public static Map<String, Long> lineIdsMap;

    public InitializeDatabaseTask(ContentResolver contentResolver, TaskListener taskListener) {
        this.contentResolver = contentResolver;
        this.taskListener = taskListener;
        lineIdsMap = new HashMap<>();
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

        if (!responseLines.isSuccessful()) {
            Log.e("code", "Code: " + responseLines.code());
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
        this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_LINE, contentValues);
        Log.i("TraNSit", "GET LINES FINISHED");

        //LOCATIONS, LINE-LOCATIONS
        Call<List<LineDto>> callLineLocations = ServiceUtils.transitRestApi.getLinesCoordinates();
        callLineLocations.enqueue(new Callback<List<LineDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineDto>> call, @NotNull Response<List<LineDto>> response) {
                if (!response.isSuccessful()) {
                    Log.e("code", "Code: " + response.code());
                    return;
                }
                List<LineDto> lines = response.body();
                ContentValues entry;
                Long lineId;
                DBContentProvider.database.beginTransaction();
                for (LineDto line : lines) {
                    lineId = InitializeDatabaseTask.lineIdsMap.get(line.getName());
                    ContentValues[] contentValues = new ContentValues[line.getCoordinates().size()];
                    for (int i = 0; i < line.getCoordinates().size(); i++) {
                        entry = new ContentValues();
                        entry.put("latitude", Double.parseDouble(line.getCoordinates().get(i).getLat()));
                        entry.put("longitude", Double.parseDouble(line.getCoordinates().get(i).getLon()));
                        entry.put("lineId", lineId);
                        entry.put("lineDirection", line.getDirection());
                        contentValues[i] = entry;
                    }
                    InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
//                    DBContentProvider.database.yieldIfContendedSafely();
                }
                Log.i("TraNSit", "GET LINE COORDINATES FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                InitializeDatabaseTask.this.lineCoordinatesFinished = true;
                InitializeDatabaseTask.this.tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null ? t.getMessage() : "error");
            }
        });

        //ZONE, PRICE LIST
        /*ContentValues entry = new ContentValues();
        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone1Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone2Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone3Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone1Id);
        entry.put("second_station_zone", zone4Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone2Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone3Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone2Id);
        entry.put("second_station_zone", zone4Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone3Id);
        entry.put("second_station_zone", zone3Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 110);
        entry.put("first_station_zone", zone3Id);
        entry.put("second_station_zone", zone4Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);

        entry.clear();
        entry.put("price", 65);
        entry.put("first_station_zone", zone4Id);
        entry.put("second_station_zone", zone4Id);
        contentResolver.insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);*/


        //STOPS, LINE-STOPS, LOCATIONS
        Call<List<LineStopsDto>> callStops = ServiceUtils.transitRestApi.getStops();
        callStops.enqueue(new Callback<List<LineStopsDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineStopsDto>> call, @NotNull Response<List<LineStopsDto>> response) {
                if (!response.isSuccessful()) {
                    Log.e("code", "Code: " + response.code());
                    return;
                }

                List<LineStopsDto> lineStops = response.body();
                Long lineId;
                DBContentProvider.database.beginTransaction();
                for (LineStopsDto lineStop : lineStops) {
                    lineId = InitializeDatabaseTask.lineIdsMap.get(lineStop.getName());
                    ContentValues[] contentValues = new ContentValues[lineStop.getStops().size()];
                    for (int i = 0; i < lineStop.getStops().size(); i++) {
                        ContentValues entry = new ContentValues();
                        entry.put("direction", lineStop.getDirection());
                        entry.put("lineId", lineId);
                        entry.put("zone", lineStop.getStops().get(i).getZone());
                        entry.put("latitude", Double.parseDouble(lineStop.getStops().get(i).getLat()));
                        entry.put("longitude", Double.parseDouble(lineStop.getStops().get(i).getLon()));
                        entry.put("title", lineStop.getStops().get(i).getName());
                        contentValues[i] = entry;
                    }
                    InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_STOP, contentValues);
                }
                Log.i("TraNSit", "GET STOPS FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                InitializeDatabaseTask.this.stopsFinished = true;
                InitializeDatabaseTask.this.tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineStopsDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null ? t.getMessage() : "error");
            }
        });

        //TIMETABLE, DEPARTURE TIME
        Call<List<LineTimetableDto>> callTimetables = ServiceUtils.transitRestApi.getTimeTables();
        callTimetables.enqueue(new Callback<List<LineTimetableDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineTimetableDto>> call, @NotNull Response<List<LineTimetableDto>> response) {
                if (!response.isSuccessful()) {
                    Log.e("code", "Code: " + response.code());
                    return;
                }

                List<LineTimetableDto> lineTimetables = response.body();
                Long lineId;
                DBContentProvider.database.beginTransaction();
                for (LineTimetableDto lineTimetable : lineTimetables) {
                    lineId = InitializeDatabaseTask.lineIdsMap.get(lineTimetable.getName());
                    ContentValues entry;
                    if (lineTimetable.getTimeTable().getWorkday() != null) {
                        if (!lineTimetable.getTimeTable().getWorkday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "WORKDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = InitializeDatabaseTask.this.contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getWorkday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getWorkday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getWorkday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                    if (lineTimetable.getTimeTable().getSaturday() != null) {
                        if (!lineTimetable.getTimeTable().getSaturday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "SATURDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = InitializeDatabaseTask.this.contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSaturday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getSaturday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getSaturday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                    if (lineTimetable.getTimeTable().getSunday() != null) {
                        if (!lineTimetable.getTimeTable().getSunday().isEmpty()) {
                            entry = new ContentValues();
                            entry.put("day", "SUNDAY");
                            entry.put("line", lineId);
                            entry.put("direction", lineTimetable.getDirection());
                            Uri uri = InitializeDatabaseTask.this.contentResolver.insert(DBContentProvider.CONTENT_URI_TIMETABLE, entry);
                            long timetableId = Long.parseLong(uri.getLastPathSegment());

                            ContentValues[] contentValues = new ContentValues[lineTimetable.getTimeTable().getSunday().size()];
                            for (int i = 0; i < lineTimetable.getTimeTable().getSunday().size(); i++) {
                                entry = new ContentValues();
                                entry.put("formatted_value", lineTimetable.getTimeTable().getSunday().get(i));
                                entry.put("timetable", timetableId);
                                contentValues[i] = entry;
                            }
                            InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_DEPARTURE_TIME, contentValues);
                        }
                    }
                }
                Log.i("TraNSit", "GET TIMETABLE FINISHED");
                DBContentProvider.database.setTransactionSuccessful();
                DBContentProvider.database.endTransaction();
                InitializeDatabaseTask.this.timeTablesFinished = true;
                InitializeDatabaseTask.this.tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineTimetableDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null ? t.getMessage() : "error");
            }
        });
    }

    private void tryToFinishTask() {
        if (this.lineCoordinatesFinished && this.stopsFinished && this.timeTablesFinished && this.taskListener != null) {
            this.taskListener.onFinished();
        }
    }

    public interface TaskListener {
        void onFinished();
    }
}
