package com.nuttyknot.whenwhere;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public class LocationHelper {

    // private static final String TAG = "LocationListenerFacade";

    private static LocationManager mLocationManager = null;

    public static void start_fine(Context context, LocationListener locationListener, long minTime, float minDistance) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER    , minTime, minDistance, locationListener);
    }

    public static void start_coarse(Context context, LocationListener locationListener, long minTime, float minDistance) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);
    }

    public static void stop(Context context, LocationListener locationListener) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.removeUpdates(locationListener);
    }

}
