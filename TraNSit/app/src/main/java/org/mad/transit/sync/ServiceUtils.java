package org.mad.transit.sync;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceUtils {
    private static final String SERVICE_API_PATH = "http://192.168.1.6:3000/api/";
    static final String LINES = "lines";
    static final String LINES_COORDINATES = "lines-coordinates";
    static final String STOPS = "stops";
    static final String TIME_TABLES = "time-tables";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(SERVICE_API_PATH)
            .addConverterFactory(GsonConverterFactory.create())
            .client(initializeOkHttpClient())
            .build();

    public static TransitRestApi transitRestApi = retrofit.create(TransitRestApi.class);

    private static OkHttpClient initializeOkHttpClient(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();

        return client;
    }



}