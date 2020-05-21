package org.mad.transit.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import lombok.SneakyThrows;

public class LocationsUtil {

    public static final int LOCATION_PERMISSIONS_REQUEST = 1234;
    public static final int LOCATION_REQUEST_CHECK_SETTINGS = 4321;
    private static final long UPDATE_INTERVAL = 1000;
    private static final long FASTEST_INTERVAL = 500;
    private static final float SMALLEST_DISPLACEMENT = 1f;
    private static final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    public static void retrieveLocationSettings(LocationRequest locationRequest, final Activity activity, final Fragment fragment) {
        if (locationRequest == null) {
            locationRequest = createLocationRequest();
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build()).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {

            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    task.getResult(ApiException.class);
                } catch (ApiException e) {
                    // Location settings are not satisfied. But could be fixed by showing a dialog to the user if status code is RESOLUTION_REQUIRED.
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        if (fragment == null) {
                            activity.startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                    LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                        } else {
                            fragment.startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                    LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                        }
                    } else {
                        Toast.makeText(activity, "Location settings are not available at the moment", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public static void retrieveLocationSettings(LocationRequest locationRequest, final Activity activity) {
        retrieveLocationSettings(locationRequest, activity, null);
    }

    public static boolean locationSettingsAvailability(LocationManager locationManager) {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        return locationRequest;
    }

    public static boolean locationPermissionsGranted(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermissions(Fragment fragment) {
        fragment.requestPermissions(permissions, LOCATION_PERMISSIONS_REQUEST);
    }

    public static void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSIONS_REQUEST);
    }

    public static String retrieveAddressFromLatAndLng(Context context, double latitude, double longitude) throws IOException {
        Geocoder geocoder = new Geocoder(context);
        List<Address> fromLocation = geocoder.getFromLocation(latitude, longitude, 1);
        if (!fromLocation.isEmpty()) {
            return fromLocation.get(0).getAddressLine(0);
        }
        return null;
    }
}
