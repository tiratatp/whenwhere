package com.nuttyknot.whenwhere.where.overlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Person extends OverlayItem {

	private int radius;

	public Person(GeoPoint point, int radius, String title, String snippet) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
		this.radius = radius;
	}

	protected int getRadius() {
		return radius;
	}

}
