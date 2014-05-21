package com.example.waybackhome;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapActivity extends com.google.android.maps.MapActivity {
	private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MapView mapView = (MapView)findViewById(R.id.mapview);
        MapController mapController = mapView.getController();
        mapController.setZoom(15);  
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}