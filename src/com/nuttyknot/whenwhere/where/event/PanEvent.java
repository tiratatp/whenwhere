package com.nuttyknot.whenwhere.where.event;

import com.google.android.maps.GeoPoint;

public class PanEvent extends Event {
	public GeoPoint oldCenter;
	public GeoPoint newCenter;

	public PanEvent(final GeoPoint oldCenter, final GeoPoint newCenter) {
		this.oldCenter = oldCenter;
		this.newCenter = newCenter;
	}
}
