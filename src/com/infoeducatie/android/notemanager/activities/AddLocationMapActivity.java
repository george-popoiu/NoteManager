package com.infoeducatie.android.notemanager.activities;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.infoeducatie.android.notemanager.R;
import com.infoeducatie.android.notemanager.views.AddressOverlay;

public class AddLocationMapActivity extends MapActivity {
	
	public static final String ADDRESS_RESULT = "address";
	
	MyLocationOverlay myLocationOverlay;
	private MapView mapView;
	private EditText addressText;
	private Button useLocationButton;
	Address address;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.add_location);
		setUpViews();
	}

	private void setUpViews() {
		mapView = (MapView)this.findViewById(R.id.map);
		addressText = (EditText)this.findViewById(R.id.add_location_edit);
		useLocationButton = (Button)this.findViewById(R.id.use_location_button);
		
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(myLocationOverlay);
		mapView.invalidate();
		mapView.setBuiltInZoomControls(true);
		
		useLocationButton.setEnabled(false);
		useLocationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(address!=null) {
					Intent intent = new Intent();
					intent.putExtra(ADDRESS_RESULT, address);
					setResult(RESULT_OK, intent);
				}
				finish();
			}
		});
	}
	
	private Address getAddressFromLocation(Location lastFix) {
		Geocoder g = new Geocoder(this);
		List<Address> addresses;
		try {
			addresses = g.getFromLocation(lastFix.getLatitude(), lastFix.getLongitude(), 1);
			if( null!=addresses && addresses.size()>0 ) {
				return addresses.get(0);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return address;
	}
	
	public void mapLocationButtonClicked(View v) {
		mapAddress();
	}
	
	private void mapAddress() {
		String addressString = addressText.getText().toString();
		Geocoder g = new Geocoder(this);
		List<Address> addresses;
		try {
			addresses = g.getFromLocationName(addressString, 1);
			if( null!=addresses && addresses.size()>0 ) {
				address = addresses.get(0);
				
				AddressOverlay addressOverlay = new AddressOverlay(address);
				mapView.getOverlays().add(addressOverlay);
				mapView.invalidate();
				
				final MapController mapController = mapView.getController();
				mapController.animateTo(addressOverlay.getGeoPoint(), new Runnable() {
					@Override
					public void run() {
						mapController.setZoom(12);
					}
				});
				
				useLocationButton.setEnabled(true);
			}
			else {
				addressNotFoundAlert();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addressNotFoundAlert() {
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("Address Not Found")
		.setMessage("Try to be as explicit as possible when inserting an address")
		.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.create();
		alertDialog.show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

}
