package com.infoeducatie.android.notemanager.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class AddressOverlay extends Overlay {
	
	public static final int CONTAINER_RADIUS = 4;
	public static final int CONTAINER_SHADOW_OFFSET = 1;
	
	Address address;
	GeoPoint geoPoint;

	public AddressOverlay(Address address) {
		super();
		this.address = address;
		
		Double latitudeE6 = address.getLatitude() * 1E6;
		Double longitudeE6 = address.getLongitude() * 1E6;
		
		setGeoPoint(new GeoPoint(latitudeE6.intValue(), longitudeE6.intValue()));
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		Point locationPoint = new Point();
		
		Projection projection = mapView.getProjection();
		projection.toPixels(getGeoPoint(), locationPoint);
		
		Paint containerPaint = new Paint();
		containerPaint.setAntiAlias(true);
		
		int containerX = locationPoint.x;
		int containerY = locationPoint.y;
		
		if(shadow) {
			containerX += CONTAINER_SHADOW_OFFSET;
			containerY += CONTAINER_SHADOW_OFFSET;
			containerPaint.setARGB(90, 0, 0, 0);
			canvas.drawCircle(containerX, containerY, CONTAINER_RADIUS, containerPaint);
		}
		else {
			containerPaint.setColor(Color.RED);
			canvas.drawCircle(containerX, containerY, CONTAINER_RADIUS, containerPaint);
		}
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public GeoPoint getGeoPoint() {
		return geoPoint;
	}

	public void setGeoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
	}
	
}
