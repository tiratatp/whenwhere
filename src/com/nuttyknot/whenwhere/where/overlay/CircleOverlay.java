package com.nuttyknot.whenwhere.where.overlay;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;
import com.nuttyknot.whenwhere.where.WhereActivity;

public class CircleOverlay extends MyLocationOverlay {
	private Context context;
	private float circleRadius = 5;
	
	public CircleOverlay(Context context, MapView mapView) {
		super(context, mapView);
		this.context = context;				
	}
	
	public void setCircleRadius(float newRadius) {
		this.circleRadius = newRadius;		
	}
	
	@Override
	public void onLocationChanged(android.location.Location location) {
		super.onLocationChanged(location);
		((WhereActivity)context).onLocationChanged(location);
	}

	@Override	
	protected void drawMyLocation(android.graphics.Canvas canvas,
            MapView mapView,
            android.location.Location lastFix,
            GeoPoint myLocation,
            long when) {
		super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
		
		Projection projection = mapView.getProjection();

		Point pt = new Point();

		projection.toPixels(myLocation, pt);		

		Paint innerCirclePaint;

		innerCirclePaint = new Paint();
		innerCirclePaint.setARGB(50, 0, 0, 0);
		innerCirclePaint.setAntiAlias(true);
		innerCirclePaint.setStyle(Paint.Style.FILL);

		canvas.drawCircle((float) pt.x, (float) pt.y, projection.metersToEquatorPixels(circleRadius * 1000),
				innerCirclePaint);
	}
}