package com.nuttyknot.whenwhere.where.event;

public class ZoomEvent extends Event{
	public int oldZoomLevel;
	public int newZoomLevel;

	public ZoomEvent(final int oldZoomLevel, final int newZoomLevel) {
		this.oldZoomLevel = oldZoomLevel;
		this.newZoomLevel = newZoomLevel;
	}
}
