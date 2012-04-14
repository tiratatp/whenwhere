package com.nuttyknot.whenwhere.where;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.nuttyknot.whenwhere.R;
import com.nuttyknot.whenwhere.RestClient;
import com.nuttyknot.whenwhere.webview.WebviewActivity;
import com.nuttyknot.whenwhere.where.event.Event;
import com.nuttyknot.whenwhere.where.event.PanEvent;
import com.nuttyknot.whenwhere.where.event.ZoomEvent;
import com.nuttyknot.whenwhere.where.overlay.CircleOverlay;
import com.nuttyknot.whenwhere.where.overlay.Person;
import com.nuttyknot.whenwhere.where.overlay.PersonOverlay;
import com.nuttyknot.whenwhere.where.overlay.Place;
import com.nuttyknot.whenwhere.where.overlay.PlaceOverlay;

public class WhereActivity extends MapActivity implements ActionListener {

	MapController mapController;
	ExtendedMapView mapView;
	SeekBar seekBar;
	Button doneButton;
	CircleOverlay circleOverlay;
	PlaceOverlay placeOverlay;
	PersonOverlay personOverlay;

	Location currentPosition;
	private boolean positionReady = false;

	private String role = "";

	int oldZoomLevel = -1;

	// private static final String TAG = "RefreshLocationListener";

	public void onLocationChanged(Location location) {
		currentPosition = location;
		doneButton.setClickable(true);
		positionReady = true;
		mapController.animateTo(new GeoPoint(
				(int) (location.getLatitude() * 1E6), (int) (location
						.getLongitude() * 1E6)));
	}

	private void callWhenActivity() {
		Log.d("com.nuttyknot.whenwhere", "Position: " + currentPosition);
		Intent intent = new Intent(this, WebviewActivity.class);
		intent.putExtra("latitude",
				String.valueOf(currentPosition.getLatitude()));
		intent.putExtra("longitude",
				String.valueOf(currentPosition.getLongitude()));
		intent.putExtra("radius", seekBar.getProgress());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (circleOverlay != null) {
			circleOverlay.disableMyLocation();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (circleOverlay != null) {
			circleOverlay.enableMyLocation();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent.hasExtra("role")) {
			role = intent.getStringExtra("role");
		}

		if (role.equals("decide_where")) {
			setContentView(R.layout.where_no_seekbar);
		} else {
			setContentView(R.layout.where);
		}

		mapView = (ExtendedMapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		List<Overlay> overlayList = mapView.getOverlays();

		if (role.equals("decide_where")) {
			mapView.addEventListener("pan", this);
			mapView.addEventListener("zoom", this);

			placeOverlay = new PlaceOverlay(this, this.getResources()
					.getDrawable(R.drawable.white_marker));
			personOverlay = new PersonOverlay(this, this.getResources()
					.getDrawable(R.drawable.marker));
			overlayList.add(placeOverlay);
			overlayList.add(personOverlay);

			String event_id = intent.getStringExtra("event_id");
			// String url =
			// "https://nuttyknot.cloudant.com/whenwhere/_design/rsvp/_view/by_event_id?key=\""
			// + event_id + "\"";
			String url = "https://nuttyknot.cloudant.com/whenwhere/_design/rsvp/_view/by_event_id";
			JSONObject jsonInput = new JSONObject();
			try {
				jsonInput.put("key", event_id);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Handler handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					// get the bundle and extract data by key
					Bundle b = msg.getData();
					String str = b.getString("data");
					try {
						JSONArray people = (new JSONObject(str))
								.getJSONArray("rows");
						double mean_lat = 0.0;
						double mean_lng = 0.0;
						// inner loop var
						JSONObject person;
						double current_lat;
						double current_lng;

						int people_length = people.length();
						for (int i = 0; i < people_length; i++) {
							try {
								person = people.getJSONObject(i).getJSONObject(
										"value");
								current_lat = Double.parseDouble(person
										.getString("latitude"));
								current_lng = Double.parseDouble(person
										.getString("longitude"));
								mean_lat += current_lat;
								mean_lng += current_lng;
								personOverlay.addOverlay(new Person(
										new GeoPoint((int) (current_lat * 1E6),
												(int) (current_lng * 1E6)),
										person.getInt("radius"), person
												.getString("name"), ""));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						GeoPoint mean_point = new GeoPoint(
								(int) ((mean_lat / people_length) * 1E6),
								(int) ((mean_lng / people_length) * 1E6));
						placeOverlay.setCenter(mean_point);
						mapController.animateTo(mean_point);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			RestClient.connect_async(url, jsonInput, handler, "GET");
		} else {
			circleOverlay = new CircleOverlay(this, mapView);
			overlayList.add(circleOverlay);
		}

		mapController = mapView.getController();
		mapController.setZoom(14);

		if (!role.equals("decide_where")) {
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
					txtView.setText("Radius: " + progress + " km.");
					if (circleOverlay != null) {
						circleOverlay.setCircleRadius(progress);
					}
					mapView.invalidate();
				}
			});

		}

		doneButton = (Button) findViewById(R.id.donebutton);
		doneButton.setClickable(false);
		doneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (positionReady) {
					callWhenActivity();
				} else if (role.equals("decide_where")) {
					Place pickedPlace = placeOverlay.getPickedPlace();
					if (pickedPlace != null) {
						Log.d("WhereActivity",
								"pickedPlace:" + pickedPlace.getTitle());

					}
				}
			}

		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		if (placeOverlay != null) {
			if (event instanceof PanEvent) {
				PanEvent panEvent = (PanEvent) event;
				placeOverlay.setCenter(panEvent.newCenter);
			} else if (event instanceof ZoomEvent) {
				int[][] bounds = mapView.getBounds();
				float[] results = new float[3];
				Location.distanceBetween((double) bounds[0][0] / 1E6,
						(double) bounds[0][1] / 1E6,
						(double) bounds[1][0] / 1E6,
						(double) bounds[1][1] / 1E6, results);
				placeOverlay.setRadius((int) Math.ceil(results[0]));
			}
		}
	}
}
