package org.mad.transit.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.mad.transit.R;
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
    private SharedPreferences sharedPreferences;
    private static final String INITIALIZE_DB_FLAG = "initialize_db_flag";
    private final TaskListener taskListener;
    private boolean stopsFinished;
    private boolean timeTablesFinished;

    public InitializeDatabaseTask(Context context, TaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
    }

    @SneakyThrows
    @Override
    protected Void doInBackground(Void... voids) {
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.favourites_preference_file_key), Context.MODE_PRIVATE);
        boolean initializeDBFlag = this.sharedPreferences.getBoolean(InitializeDatabaseTask.INITIALIZE_DB_FLAG, false);
        if (!initializeDBFlag) {
            this.initializeDB();
        }
        return null;
    }

    private void initializeDB() throws IOException {

        //LINES, LOCATIONS, LINE-LOCATIONS
        Call<List<LineDto>> call = ServiceUtils.transitRestApi.getLinesCoordinates();
        Response<List<LineDto>> response = call.execute();

        if (!response.isSuccessful()){
            Log.e("code","Code: " + response.code());
            return;
        }

        List<LineDto> lines = response.body();
        for (LineDto line: lines){
            String [] projection = {
                    DatabaseHelper.ID
            };
            String [] selectionArgs = {
                    line.getName()
            };
            Cursor cursor = context.getContentResolver().query(DBContentProvider.CONTENT_URI_LINE, projection, "number = ?", selectionArgs, null);
            ContentValues entry = new ContentValues();
            int lineId;
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    lineId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.ID));
                } else {
                    entry.put("name", line.getTitle());
                    entry.put("number", line.getName());
                    entry.put("type", line.getType());
                    Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_LINE, entry);
                    lineId = Integer.parseInt(uri.getLastPathSegment());
                }
                cursor.close();
                ContentValues[] contentValues = new ContentValues[line.getCoordinates().size()];
                for (int i = 0; i < line.getCoordinates().size(); i++) {
                    entry = new ContentValues();
                    entry.put("latitude",Double.parseDouble(line.getCoordinates().get(i).getLat()));
                    entry.put("longitude", Double.parseDouble(line.getCoordinates().get(i).getLon()));
                    entry.put("lineId", lineId);
                    entry.put("lineDirection", line.getDirection());
                    contentValues[i] = entry;
                }
                context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_LOCATION, contentValues);
            }else{
                Log.e("traNSit","Cursor is null");
                return;
            }
        }
        Log.i("GET LINES", "FINISHED");

        //ZONE, PRICE LIST
        ContentValues entry = new ContentValues();
        entry.put("name", "I");
        Uri uri = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_ZONE, entry);
        long zone1Id = Long.parseLong(uri.getLastPathSegment());

        entry.clear();
        entry.put("name", "II");
        Uri uri2 = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_ZONE, entry);
        long zone2Id = Long.parseLong(uri2.getLastPathSegment());

        entry.clear();
        entry.put("name", "III");
        Uri uri3 = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_ZONE, entry);
        long zone3Id = Long.parseLong(uri3.getLastPathSegment());

        entry.clear();
        entry.put("name", "IV");
        Uri uri4 = context.getContentResolver().insert(DBContentProvider.CONTENT_URI_ZONE, entry);
        long zone4Id = Long.parseLong(uri4.getLastPathSegment());

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
        context.getContentResolver().insert(DBContentProvider.CONTENT_URI_PRICE_LIST, entry);


        //STOPS, LINE-STOPS, LOCATIONS
        Call<List<LineStopsDto>> call2 = ServiceUtils.transitRestApi.getStops();
        call2.enqueue(new Callback<List<LineStopsDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineStopsDto>> call, @NotNull Response<List<LineStopsDto>> response) {
                if (!response.isSuccessful()){
                    Log.e("code","Code: " + response.code());
                    return;
                }

                List<LineStopsDto> lineStops = response.body();
                for (LineStopsDto lineStop: lineStops){
                    String [] projection = {
                            DatabaseHelper.ID
                    };
                    String [] selectionArgs = {
                            lineStop.getName()
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

                    ContentValues[] contentValues = new ContentValues[lineStop.getStops().size()];
                    for (int i = 0; i < lineStop.getStops().size(); i++) {
                        ContentValues entry = new ContentValues();
                        entry.put("direction", lineStop.getDirection());
                        entry.put("line", lineId);
                        entry.put("zone", lineStop.getStops().get(i).getZone());
                        entry.put("latitude", lineStop.getStops().get(i).getLat());
                        entry.put("longitude", lineStop.getStops().get(i).getLon());
                        entry.put("title", lineStop.getStops().get(i).getName());
                        contentValues[i] = entry;
                    }
                    context.getContentResolver().bulkInsert(DBContentProvider.CONTENT_URI_STOP, contentValues);
                }
                Log.i("GET STOPS", "FINISHED");
                stopsFinished = true;
                tryToFinishTask();
            }

            @Override
            public void onFailure(@NotNull Call<List<LineStopsDto>> call, @NotNull Throwable t) {
                Log.e("message", t.getMessage() != null?t.getMessage():"error");
            }
        });

        //TIMETABLE, DEPARTURE TIME
        Call<List<LineTimetableDto>> call3 = ServiceUtils.transitRestApi.getTimeTables();
        call3.enqueue(new Callback<List<LineTimetableDto>>() {
            @Override
            public void onResponse(@NotNull Call<List<LineTimetableDto>> call, @NotNull Response<List<LineTimetableDto>> response) {
                if (!response.isSuccessful()){
                    Log.e("code","Code: " + response.code());
                    return;
                }

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
                Log.i("GET TIMETABLE", "FINISHED");
                sharedPreferences.edit().putBoolean(InitializeDatabaseTask.INITIALIZE_DB_FLAG, true).apply();
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
        if (stopsFinished && timeTablesFinished && taskListener != null) {
            taskListener.onFinished();
        }
    }

    public interface TaskListener {
        void onFinished();
    }
}
