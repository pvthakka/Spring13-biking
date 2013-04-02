package com.example.wsbiking;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wsbiking.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import android.support.v4.app.FragmentActivity;

/**
 * 
 * @author Leon Dmello
 * The main route recording activity.
 * Tracks the route points when the user has started recording a route.
 * 
 */
public class RecordActivity extends FragmentActivity {
	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	private LocationManager locManager;
	private LocationListener locationListener;

	private boolean gps_enabled = false;
	// private boolean network_enabled = false;

	private ArrayList<RoutePoint> routePoints;

	// Variables to keep track of current route recording
	private float totalDistance;
	private long startTime;
	private Location lastLocation;

	// Database handler object
	private DatabaseHandler dbHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		setUpMapIfNeeded();
		dbHandler = DatabaseHandler.getInstance(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}

	@Override
	public void onStop() {
		dbHandler.close();
		super.onStop();
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play
	 * services APK is correctly installed) and the map has not already been
	 * instantiated.. This will ensure that we only ever call
	 * {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt
	 * for the user to install/update the Google Play services APK on their
	 * device.
	 * <p>
	 * A user can return to this FragmentActivity after following the prompt and
	 * correctly installing/updating/enabling the Google Play services. Since
	 * the FragmentActivity may not have been completely destroyed during this
	 * process (it is likely that it would only be stopped or paused),
	 * {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the
	 * camera.
	 * <p>
	 * This should only be called once and when we are sure that {@link #mMap}
	 * is not null.
	 */
	private void setUpMap() {

		mMap.setMyLocationEnabled(true);
		routePoints = new ArrayList<RoutePoint>();
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				locationChanged(location);
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		// try {
		// network_enabled = locManager
		// .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		// } catch (Exception ex) {

		// }

		if (!gps_enabled) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("Attention!");
			builder.setMessage("Sorry, location cannot be determined. Please enable GPS");
			builder.create().show();
		}
	}

	public void locationChanged(Location currentLocation) {
		// AlertDialog.Builder builder = new Builder(this);
		// builder.setTitle("Location updated");
		// builder.setMessage("Lat : " + location.getLatitude() + ", Long : "
		// + location.getLongitude() + ", Source : "
		// + location.getProvider());
		// builder.create().show();

		if (currentLocation != null) {

			if (lastLocation != null)
				totalDistance += currentLocation.distanceTo(lastLocation);

			lastLocation = new Location(currentLocation);

			routePoints.add(new RoutePoint(currentLocation.getLatitude(),
					currentLocation.getLongitude()));
		}
	}

	public void startRecording(View btnStart) {

		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}

		if (gps_enabled) {

			Resources res = getResources();

			// Register location listener to get periodic updates
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					res.getInteger(R.integer.time_interval),
					res.getInteger(R.integer.min_distance), locationListener);

			// Initialize route recording parameters
			startTime = System.currentTimeMillis();
			totalDistance = 0;
			lastLocation = null;

			// Hide play button
			btnStart.setVisibility(View.INVISIBLE);
			ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
			btnStop.setVisibility(View.VISIBLE);
		}
	}

	public void stopRecording(View btnStop) {

		if (routePoints.size() <= 1) {
			Toast.makeText(getApplicationContext(),
					"Need more than one point to save route",
					Toast.LENGTH_SHORT).show();
		} else {
			// Elapsed time in minutes
			long elapsedTime = (System.currentTimeMillis() - startTime)
					/ (1000 * 60);

			locManager.removeUpdates(locationListener);

			Intent intent = new Intent(this, RouteSave.class);
			intent.putParcelableArrayListExtra("routePoints", routePoints);
			intent.putExtra("totalDistance", totalDistance);
			intent.putExtra("elapsedTime", elapsedTime);
			intent.putExtra("avgSpeed", calculateSpeed(totalDistance, elapsedTime));
			startActivity(intent);
		}

		routePoints.clear();

		// Hide Stop Button
		btnStop.setVisibility(View.INVISIBLE);
		ImageView btnStart = (ImageView) findViewById(R.id.start_button);
		btnStart.setVisibility(View.VISIBLE);
	}
	

	private float calculateSpeed(float distance, float duration) {
		// TODO Test code

		float hours = duration / 60;
		duration = duration % 60;
		hours += duration / 60 * 100;

		return distance / hours;
	}
}
