package org.mad.transit.sync;

import org.mad.transit.dto.LineDto;
import org.mad.transit.dto.LineStopsDto;
import org.mad.transit.dto.LineTimetableDto;
import org.mad.transit.dto.ZoneDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface TransitRestApi {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET(ServiceUtils.ZONES)
    Call<List<ZoneDto>> getZones();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET(ServiceUtils.LINES)
    Call<List<LineDto>> getLines();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET(ServiceUtils.LINES_COORDINATES)
    Call<List<LineDto>> getLinesCoordinates();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET(ServiceUtils.STOPS)
    Call<List<LineStopsDto>> getStops();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })
    @GET(ServiceUtils.TIME_TABLES)
    Call<List<LineTimetableDto>> getTimeTables();
}
