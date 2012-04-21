package com.nuttyknot.whenwhere.where.overlay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.nuttyknot.whenwhere.R;
import com.nuttyknot.whenwhere.webview.JavaScriptInterface;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class PlaceOverlay extends BalloonItemizedOverlay<Place> {

	private ArrayList<Place> placesList;
	private Hashtable<String, Integer> placesHash;
	private int radius = 5000;
	private Context context;
	private Place pickedPlace = null;
	private Drawable defaultPin;
	private Drawable pickedPin;

	public PlaceOverlay(Context context, MapView mapView, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker), mapView);
		placesList = new ArrayList<Place>();
		placesHash = new Hashtable<String, Integer>();
		this.context = context;
		this.defaultPin = defaultMarker;
		pickedPin = context.getResources().getDrawable(R.drawable.marker2);
		populate();
	}

	@Override
	public boolean onTap(int index) {
		// TODO Auto-generated method stub
		Place item = placesList.get(index);
		Toast.makeText(context, "Select " + item.getTitle(), Toast.LENGTH_SHORT)
				.show();
		item.setMarker(boundCenterBottom(pickedPin));
		if (pickedPlace != null && pickedPlace != item) {
			pickedPlace.setMarker(defaultPin);
		}
		pickedPlace = item;
		return super.onTap(index);
	}

	@Override
	protected Place createItem(int i) {
		return placesList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return placesList.size();
	}

	public Place getPickedPlace() {
		return pickedPlace;
	}

	@Override
	protected boolean onBalloonTap(int index, Place item) {
		// open facebook page
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.facebook.com/"
							+ item.json.getString("id")));
			context.startActivity(intent);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.onBalloonTap(index, item);
	}

	public void setCenter(final GeoPoint center) {
		Thread background = new Thread(new Runnable() {
			@Override
			public void run() {
				Facebook facebook = JavaScriptInterface.facebook;
				Bundle parameters = new Bundle();
				parameters.putString("center", (double) center.getLatitudeE6()
						/ 1E6 + "," + (double) center.getLongitudeE6() / 1E6);
				parameters.putString("type", "place");
				parameters.putString("distance", String.valueOf(radius));

				try {
					String response = facebook.request("search", parameters);
					JSONObject json = new JSONObject(response);
					JSONArray places = json.getJSONArray("data");
					JSONObject place;
					Place place_marker;
					JSONObject location;
					int place_pos;
					String place_id;
					int places_length = places.length();
					for (int i = 0; i < places_length; i++) {
						place = places.getJSONObject(i);
						place_id = place.getString("id");
						if (!placesHash.containsKey(place_id)) {
							location = place.getJSONObject("location");
							place_marker = new Place(
									new GeoPoint(
											(int) (location
													.getDouble("latitude") * 1E6),
											(int) (location
													.getDouble("longitude") * 1E6)),
									place.getString("name"), "", place);
							place_pos = placesList.size();
							placesList.add(place_marker);
							placesHash.put(place_id, place_pos);
						}
					}
					populate();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		background.start();
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}