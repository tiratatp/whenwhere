package com.nuttyknot.whenwhere;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.nuttyknot.whenwhere.R;;

public class WhereActivity extends MapActivity {
	
	MapController 	mapController;
	MapView			mapView;
	SeekBar			seekBar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.where);        
        LocationHelper.start_coarse(this, new MyLocationListener(this), 0, 0);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapController = mapView.getController();
		mapController.setZoom(16);
		
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setMax(25);
		seekBar.setProgress(10);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				TextView txtView = (TextView) findViewById(R.id.textView);			
				txtView.setText("Distance: " + progress + " Km");
			}
		});
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
			int lat = (int) (location.getLatitude() * 1E6);
	        int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point);
			CircleOverlay circleOverlay = new CircleOverlay(point); 
        }

        @Override
        public void onProviderDisabled(String provider) { }

        @Override
        public void onProviderEnabled(String provider) { }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }

    }
}
