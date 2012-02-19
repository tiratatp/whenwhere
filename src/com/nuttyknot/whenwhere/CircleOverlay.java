package com.nuttyknot.whenwhere;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class CircleOverlay extends Overlay {
	Context context;
	double mLat;
	double mLon;
	boolean gotLocation = false;
	float circleRadius = 15;

	public CircleOverlay(Context _context) {
		context = _context;				
	}
	
	public void setCircleRadius(float newRadius) {
		this.circleRadius = newRadius;
	}
	
	public void setLocation(double _lat, double _lon) {
		mLat = _lat;
		mLon = _lon;
		gotLocation = true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {

		super.draw(canvas, mapView, shadow);
		if(!gotLocation) {
			return;
		}
		Projection projection = mapView.getProjection();

		Point pt = new Point();

		GeoPoint geo = new GeoPoint((int) (mLat * 1e6), (int) (mLon * 1e6));

		projection.toPixels(geo, pt);		

		Paint innerCirclePaint;

		innerCirclePaint = new Paint();
		innerCirclePaint.setARGB(100, 255, 255, 255);
		innerCirclePaint.setAntiAlias(true);
		innerCirclePaint.setStyle(Paint.Style.FILL);

		canvas.drawCircle((float) pt.x, (float) pt.y, projection.metersToEquatorPixels(circleRadius * 1000),
				innerCirclePaint);
	}
}