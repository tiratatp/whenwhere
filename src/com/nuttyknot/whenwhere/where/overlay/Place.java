package com.nuttyknot.whenwhere.where.overlay;

import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Place extends OverlayItem {

	public JSONObject json;

	public Place(GeoPoint point, String title, String snippet, JSONObject json) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
		this.json = json;
	}

}
