package com.nuttyknot.whenwhere;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.nuttyknot.whenwhere.R;;

public class WhereActivity extends MapActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.where);
        
        LocationHelper.start_coarse(this, new MyLocationListener(this), 0, 0);
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
    private class MyLocationListener implements LocationListener {

        // private static final String TAG = "RefreshLocationListener";

        private Context context;

        public MyLocationListener(Context context) {
            this.context = context;
        }

        @Override
        public void onLocationChanged(Location location) {
            LocationHelper.stop(context, this);
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

    }
}
