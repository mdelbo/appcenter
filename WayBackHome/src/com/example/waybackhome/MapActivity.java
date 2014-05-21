package com.example.waybackhome;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class MapActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(37.565102,127.007765) , 15.0f) );

        
//        MapView mapView = (MapView)findViewById(R.id.map);
//        MapController mapController = mapView.getController();
//        mapController.setZoom(1);
//        mapController.setCenter(new GeoPoint((int)(40.719482 * 1E6),(int)(-73.998568 * 1E6)));
    }

//	@Override
//	protected boolean isRouteDisplayed() {
//		// TODO Auto-generated method stub
//		return false;
//	}
}