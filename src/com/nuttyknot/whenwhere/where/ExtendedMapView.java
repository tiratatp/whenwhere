package com.nuttyknot.whenwhere.where;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.nuttyknot.whenwhere.where.event.Event;
import com.nuttyknot.whenwhere.where.event.PanEvent;
import com.nuttyknot.whenwhere.where.event.ZoomEvent;

public class ExtendedMapView extends MapView {

	private int oldZoomLevel = -1;
	private GeoPoint currentCenter;
	Hashtable<String, ArrayList<ActionListener>> eventListenerList;

	public ExtendedMapView(android.content.Context context,
			android.util.AttributeSet attrs) {
		super(context, attrs);
		eventListenerList = new Hashtable<String, ArrayList<ActionListener>>();
	}

	public ExtendedMapView(android.content.Context context,
			android.util.AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		eventListenerList = new Hashtable<String, ArrayList<ActionListener>>();
	}

	public ExtendedMapView(android.content.Context context,
			java.lang.String apiKey) {
		super(context, apiKey);
		eventListenerList = new Hashtable<String, ArrayList<ActionListener>>();
	}

	protected boolean addEventListener(String event, ActionListener listener) {
		ArrayList<ActionListener> list;
		if (!eventListenerList.containsKey(event)) {
			list = new ArrayList<ActionListener>();
			eventListenerList.put(event, list);
		} else {
			list = eventListenerList.get(event);
		}
		return list.add(listener);
	}

	private void notify(final String event_name, final Event event) {
		if (eventListenerList.containsKey(event_name)) {
			ArrayList<ActionListener> list = eventListenerList.get(event_name);
			Iterator<ActionListener> iterator = list.iterator();
			ActionListener current;
			while (iterator.hasNext()) {
				current = iterator.next();
				current.handleEvent(event);
			}
		}
	}

	public int[][] getBounds() {
		GeoPoint center = getMapCenter();
		int latitudeSpan = getLatitudeSpan();
		int longtitudeSpan = getLongitudeSpan();
		int[][] bounds = new int[2][2];

		bounds[0][0] = center.getLatitudeE6() - (latitudeSpan / 2);
		bounds[0][1] = center.getLongitudeE6() - (longtitudeSpan / 2);

		bounds[1][0] = center.getLatitudeE6() + (latitudeSpan / 2);
		bounds[1][1] = center.getLongitudeE6() + (longtitudeSpan / 2);
		return bounds;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			GeoPoint centerGeoPoint = this.getMapCenter();
			if (currentCenter == null
					|| (currentCenter.getLatitudeE6() != centerGeoPoint
							.getLatitudeE6())
					|| (currentCenter.getLongitudeE6() != centerGeoPoint
							.getLongitudeE6())) {
				notify("pan", new PanEvent(currentCenter, centerGeoPoint));
				currentCenter = centerGeoPoint;
			}
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (getZoomLevel() != oldZoomLevel) {
			int newZoomLevel = getZoomLevel();
			notify("zoom", new ZoomEvent(oldZoomLevel, newZoomLevel));
			oldZoomLevel = newZoomLevel;
		}
	}
}
