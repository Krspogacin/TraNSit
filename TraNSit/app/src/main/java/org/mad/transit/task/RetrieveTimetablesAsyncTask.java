package org.mad.transit.task;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.util.Log;

import org.mad.transit.dto.LineTimetableDto;
import org.mad.transit.model.Line;
import org.mad.transit.repository.DepartureTimeRepository;
import org.mad.transit.repository.LineRepository;
import org.mad.transit.repository.TimetableRepository;
import org.mad.transit.sync.InitializeDatabaseTask;
import org.mad.transit.sync.ServiceUtils;
import org.mad.transit.util.TimetableAndDepartureTimeUtil;

import java.util.HashMap;
import java.util.List;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Response;

public class RetrieveTimetablesAsyncTask extends AsyncTask<Void, Void, Void> {

    private final ContentResolver contentResolver;
    private final TimetableRepository timetableRepository;
    private final LineRepository lineRepository;
    private final DepartureTimeRepository departureTimeRepository;
    private final TaskListener taskListener;

    public RetrieveTimetablesAsyncTask(ContentResolver contentResolver, TimetableRepository timetableRepository, LineRepository lineRepository,
                                       DepartureTimeRepository departureTimeRepository, TaskListener taskListener) {
        this.contentResolver = contentResolver;
        this.timetableRepository = timetableRepository;
        this.lineRepository = lineRepository;
        this.departureTimeRepository = departureTimeRepository;
        this.taskListener = taskListener;
    }

    @SneakyThrows
    @Override
    protected Void doInBackground(Void... voids) {
        Log.i("timetables async task", "I'm here");
        this.departureTimeRepository.deleteAll();
        this.timetableRepository.deleteAll();
        Call<List<LineTimetableDto>> callTimetables = ServiceUtils.transitRestApi.getTimeTables();
        Response<List<LineTimetableDto>> response = callTimetables.execute();
        List<LineTimetableDto> lineTimetables = response.body();
        List<Line> lines = RetrieveTimetablesAsyncTask.this.lineRepository.findAll();
        InitializeDatabaseTask.lineIdsMap = new HashMap<>();
        for (Line line : lines) {
            InitializeDatabaseTask.lineIdsMap.put(line.getNumber(), line.getId());
        }
        TimetableAndDepartureTimeUtil.retrieveTimetablesAndDepartureTimes(RetrieveTimetablesAsyncTask.this.contentResolver, lineTimetables);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        RetrieveTimetablesAsyncTask.this.taskListener.onFinished();
    }

    public interface TaskListener {
        void onFinished();
    }
}
