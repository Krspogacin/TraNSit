package org.mad.transit.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.database.DBContentProvider;
import org.mad.transit.dto.LineDto;
import org.mad.transit.dto.LineStopsDto;
import org.mad.transit.dto.LineTimetableDto;
import org.mad.transit.dto.ZoneDto;
import org.mad.transit.util.TimetableAndDepartureTimeUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.mad.transit.util.Constants.DIRECTION;
import static org.mad.transit.util.Constants.END_ZONE;
import static org.mad.transit.util.Constants.LATITUDE;
import static org.mad.transit.util.Constants.LINE_DIRECTION;
import static org.mad.transit.util.Constants.LINE_ID;
import static org.mad.transit.util.Constants.LONGITUDE;
import static org.mad.transit.util.Constants.NAME;
import static org.mad.transit.util.Constants.NUMBER;
import static org.mad.transit.util.Constants.PRICE;
import static org.mad.transit.util.Constants.START_ZONE;
import static org.mad.transit.util.Constants.TITLE;
import static org.mad.transit.util.Constants.TYPE;
import static org.mad.transit.util.Constants.ZONE;

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
        //ZONES, PRICE LISTS
        Call<List<ZoneDto>> callZones = ServiceUtils.transitRestApi.getZones();
        Response<List<ZoneDto>> responseZones = callZones.execute();

        if (!responseZones.isSuccessful()) {
            Log.e("code", "Code: " + responseZones.code());
            return;
        }
        List<ZoneDto> zones = responseZones.body();

        DBContentProvider.database.beginTransaction();
        for (ZoneDto zone : zones) {
            ContentValues[] contentValues = new ContentValues[zone.getPriceList().size()];
            for (int i = 0; i < zone.getPriceList().size(); i++) {
                ContentValues entry = new ContentValues();
                entry.put(START_ZONE, zone.getName());
                entry.put(END_ZONE, zone.getPriceList().get(i).getToZone());
                entry.put(PRICE, zone.getPriceList().get(i).getPrice());
                contentValues[i] = entry;
            }
            this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_ZONE, contentValues);
        }
        Log.i("TraNSit", "GET ZONES FINISHED");
        DBContentProvider.database.setTransactionSuccessful();
        DBContentProvider.database.endTransaction();

        //LINES
        Call<List<LineDto>> callLines = ServiceUtils.transitRestApi.getLines();
        Response<List<LineDto>> responseLines = callLines.execute();

        if (!responseLines.isSuccessful()) {
            Log.e("code", "Code: " + responseLines.code());
            return;
        }
        List<LineDto> lines = responseLines.body();

        DBContentProvider.database.beginTransaction();
        ContentValues[] contentValues = new ContentValues[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            ContentValues entry = new ContentValues();
            entry.put(NAME, lines.get(i).getTitle());
            entry.put(NUMBER, lines.get(i).getName());
            entry.put(TYPE, lines.get(i).getType());
            contentValues[i] = entry;
        }
        this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_LINE, contentValues);
        Log.i("TraNSit", "GET LINES FINISHED");
        DBContentProvider.database.setTransactionSuccessful();
        DBContentProvider.database.endTransaction();

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
                        entry.put(LATITUDE, Double.parseDouble(line.getCoordinates().get(i).getLat()));
                        entry.put(LONGITUDE, Double.parseDouble(line.getCoordinates().get(i).getLon()));
                        entry.put(LINE_ID, lineId);
                        entry.put(LINE_DIRECTION, line.getDirection());
                        contentValues[i] = entry;
                    }
                    InitializeDatabaseTask.this.contentResolver.bulkInsert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
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
                        entry.put(DIRECTION, lineStop.getDirection());
                        entry.put(LINE_ID, lineId);
                        entry.put(ZONE, lineStop.getStops().get(i).getZone());
                        entry.put(LATITUDE, Double.parseDouble(lineStop.getStops().get(i).getLat()));
                        entry.put(LONGITUDE, Double.parseDouble(lineStop.getStops().get(i).getLon()));
                        entry.put(TITLE, lineStop.getStops().get(i).getName());
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
                TimetableAndDepartureTimeUtil.retrieveTimetablesAndDepartureTimes(InitializeDatabaseTask.this.contentResolver, lineTimetables);
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
