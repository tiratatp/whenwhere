package com.nuttyknot.whenwhere.where.overlay;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class PersonOverlay extends ItemizedOverlay<Person> {

	private ArrayList<Person> peopleList;
	private Context context;

	public PersonOverlay(Context context, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		peopleList = new ArrayList<Person>();
		this.context = context;
		populate();
	}

	public void addOverlay(Person overlay) {
		peopleList.add(overlay);
		populate();
	}

	@Override
	protected Person createItem(int i) {
		return peopleList.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return peopleList.size();
	}

	@Override
	public void draw(final Canvas canvas, final MapView mapView,
			final boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if (!shadow) {
			Projection projection = mapView.getProjection();
			Point pt = new Point();

			for (int i = 0; i < size(); i++) {
				Person person = peopleList.get(i);

				projection.toPixels(person.getPoint(), pt);

				Paint innerCirclePaint;

				innerCirclePaint = new Paint();
				innerCirclePaint.setARGB(50, 0, 0, 0);
				innerCirclePaint.setAntiAlias(true);
				innerCirclePaint.setStyle(Paint.Style.FILL);

				canvas.drawCircle((float) pt.x, (float) pt.y, projection
						.metersToEquatorPixels(person.getRadius() * 1000),
						innerCirclePaint);
			}
		}
	}

	@Override
	protected boolean onTap(int index) {
		Toast.makeText(context, peopleList.get(index).getTitle(),
				Toast.LENGTH_SHORT).show();
		return super.onTap(index);
	}
}