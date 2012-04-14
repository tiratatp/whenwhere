package com.nuttyknot.whenwhere.where.overlay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.nuttyknot.whenwhere.R;
import com.nuttyknot.whenwhere.webview.JavaScriptInterface;

public class PlaceOverlay extends ItemizedOverlay<Place> {

	private ArrayList<Place> placesList;
	private Hashtable<String, Integer> placesHash;
	private int radius = 5000;
	private Context context;
	private Place pickedPlace = null;
	private Drawable defaultPin;
	private Drawable pickedPin;

	public PlaceOverlay(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		placesList = new ArrayList<Place>();
		placesHash = new Hashtable<String, Integer>();
		this.context = context;
		this.defaultPin = defaultMarker;
		pickedPin = context.getResources().getDrawable(R.drawable.blue_marker);
		populate();
	}

	public void addOverlay(Place overlay) {
		placesList.add(overlay);
		populate();
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
	protected boolean onTap(int index) {
		Place newPlace = placesList.get(index);
		Toast.makeText(context, newPlace.getTitle(), Toast.LENGTH_SHORT).show();
		newPlace.setMarker(boundCenterBottom(pickedPin));
		if (pickedPlace != null) {
			pickedPlace.setMarker(defaultPin);
		}
		pickedPlace = newPlace;
		return super.onTap(index);
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
									place.getString("name"), "");
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