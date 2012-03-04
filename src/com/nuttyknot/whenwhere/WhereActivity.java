package com.nuttyknot.whenwhere;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.nuttyknot.whenwhere.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class WhereActivity extends MapActivity implements LocationListener {

	MapController mapController;
	MapView mapView;
	SeekBar seekBar;
	Button doneButton;
	CircleOverlay circleOverlay;

	Location currentPosition;
	boolean positionReady = false;

	// private static final String TAG = "RefreshLocationListener";

	@Override
	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude() * 1E6);
		int lng = (int) (location.getLongitude() * 1E6);
		currentPosition = location;
		GeoPoint point = new GeoPoint(lat, lng);
		mapController.animateTo(point);
		doneButton.setClickable(true);
		circleOverlay.setLocation(location.getLatitude(),
				location.getLongitude());
		mapView.invalidate();
		positionReady = true;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	private void callWhenActivity() {
		Log.d("com.nuttyknot.whenwhere", "Position: " + currentPosition);
		Intent intent = new Intent(this, WebviewActivity.class);
		intent.putExtra("current_position", currentPosition.getLatitude()
				+ ", " + currentPosition.getLongitude());
		intent.putExtra("radius", seekBar.getProgress());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.where);
		LocationHelper.start_coarse(this, this, 0, 0);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		circleOverlay = new CircleOverlay(this);
		mapView.getOverlays().add(circleOverlay);
		mapController = mapView.getController();
		mapController.setZoom(14);

		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setMax(20);
		seekBar.setProgress(5);
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
				txtView.setText("Radius: " + progress + " KM.");
				circleOverlay.setCircleRadius(progress);
				mapView.invalidate();
			}
		});

		doneButton = (Button) findViewById(R.id.donebutton);
		doneButton.setClickable(false);
		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (positionReady) {
					callWhenActivity();
				}
			}

		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}
