package com.example.wsbiking;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wsbiking.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.support.v4.app.FragmentActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * TODO: Improve code structure(Remove redundancy, error checking, try catch,
 * optimization etc.)
 * 
 * @author Leon Dmello The main route recording activity. Tracks the route
 *         points when the user has started recording a route.
 * 
 */
public class RecordActivity extends FragmentActivity {

	/**
	 * Constants used in this file
	 */
	private static final float DEFAULTZOOM = 14.3f;
	private static final float METER_THRESHOLD = 160.934f;
	private static final float METERS_TO_MILES = 1609.34f;
	private static final CharSequence INITIAL_DISTANCE = "0 meters";

	/**
	 * Note that this may be null if the Google Play services APK is not
	 * available.
	 */
	private GoogleMap mMap;

	private UiSettings mapUI;
	private Marker myLocationMarker;
	private Marker startMarker;
	private Marker endMarker;

	private LocationManager locManager;
	private LocationListener locationListener;

	private boolean gps_enabled = false;
	// private boolean network_enabled = false;

	private ArrayList<RoutePoint> routePoints;

	// Variables to keep track of current route recording
	private float totalDistance;
	private Location lastLocation;

	private DatabaseHandler dbHandler;
	private Resources resourceHandler;
	
	private Session session;
	private EditText fbUserId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		setUpMapIfNeeded();
		dbHandler = DatabaseHandler.getInstance(this);
		resourceHandler = getResources();
		fbUserId = (EditText) findViewById(R.id.fbUserId);
		session = Session.getActiveSession();
		setName(session);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.record, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_routes:
			// TODO: Call show route activity
			Intent intent = new Intent(this, ViewRoutes.class);
			startActivity(intent);

			break;
		default:
			break;
		}

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

		mapUI = mMap.getUiSettings();
		mapUI.setZoomControlsEnabled(false);

		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Location lastKnown = locManager.getLastKnownLocation(locManager
				.getBestProvider(new Criteria(), false));

		if (lastKnown != null)
			moveCamera(lastKnown, DEFAULTZOOM);

		routePoints = new ArrayList<RoutePoint>();

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

		if (!gps_enabled) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(resourceHandler
					.getString(R.string.title_GPSdisabled));
			builder.setMessage(resourceHandler
					.getString(R.string.message_GPSdisabled));
			builder.create().show();
		}
	}

	/**
	 * Location changed listener to store route points
	 * 
	 * @param currentLocation
	 */
	public void locationChanged(Location currentLocation) {
		if (currentLocation != null) {

			if (lastLocation != null) {
				totalDistance += currentLocation.distanceTo(lastLocation);

				PolylineOptions rectOptions = new PolylineOptions().add(
						new LatLng(lastLocation.getLatitude(), lastLocation
								.getLongitude())).add(
						new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude()));

				rectOptions.color(Color.BLUE);
				rectOptions.width((float) 1.0);

				mMap.addPolyline(rectOptions);
			} else {
				startMarker = mMap.addMarker(new MarkerOptions().position(
						new LatLng(currentLocation.getLatitude(),
								currentLocation.getLongitude())).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.cycling)));
			}

			TextView distance = (TextView) findViewById(R.id.tripDistance);
			distance.setText(formatTotalDistance());

			myLocationMarker.setPosition(new LatLng(currentLocation
					.getLatitude(), currentLocation.getLongitude()));

			lastLocation = new Location(currentLocation);

			moveCamera(currentLocation, mMap.getCameraPosition().zoom);

			routePoints.add(new RoutePoint(currentLocation.getLatitude(),
					currentLocation.getLongitude()));
		}
	}

	/**
	 * Format distance in decimal meters to rounded meters or miles
	 * 
	 * @return
	 */
	private String formatTotalDistance() {
		if (totalDistance <= METER_THRESHOLD) {
			return String.valueOf(Math.round(totalDistance) + " meters");
		} else {
			float tempDistance = totalDistance, calculatedDistance;
			calculatedDistance = (float) Math.floor(tempDistance
					/ METERS_TO_MILES);
			tempDistance %= METERS_TO_MILES;
			calculatedDistance += tempDistance / METERS_TO_MILES;
			calculatedDistance = Float.parseFloat(new DecimalFormat("#.##")
					.format(calculatedDistance));
			return String.valueOf(calculatedDistance + " miles");
		}
	}

	/**
	 * Start recording user route
	 * 
	 * @param btnStart
	 */
	public void startRecording(View btnStart) {

		try {
			gps_enabled = locManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}

		if (gps_enabled) {

			mapUI.setCompassEnabled(false);

			mMap.clear();
			startMarker = endMarker = null;

			// Get last known location and move camera
			Location lastKnown = locManager.getLastKnownLocation(locManager
					.getBestProvider(new Criteria(), false));

			if (lastKnown != null) {

				LatLng lastKnownLatLng = new LatLng(lastKnown.getLatitude(),
						lastKnown.getLongitude());

				myLocationMarker = mMap.addMarker(new MarkerOptions().position(
						lastKnownLatLng).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.mylocation)));

				startMarker = mMap.addMarker(new MarkerOptions().position(
						lastKnownLatLng).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.cycling)));

				lastLocation = new Location(lastKnown);

				moveCamera(lastKnown, DEFAULTZOOM);
			} else {
				lastLocation = null;
			}

			// Register location listener to get periodic updates
			locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					resourceHandler.getInteger(R.integer.time_interval),
					resourceHandler.getInteger(R.integer.min_distance),
					locationListener);

			// TODO: Initialize route recording parameters
			totalDistance = 0;

			// Start stop watch
			Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
			timer.setBase(SystemClock.elapsedRealtime());
			timer.setVisibility(View.VISIBLE);
			timer.start();

			TextView distance = (TextView) findViewById(R.id.tripDistance);
			distance.setText(INITIAL_DISTANCE);
			distance.setVisibility(View.VISIBLE);

			// Hide play button
			btnStart.setVisibility(View.INVISIBLE);
			ImageView btnStop = (ImageView) findViewById(R.id.stop_button);
			btnStop.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Stop recording the route
	 * 
	 * @param btnStop
	 */
	public void stopRecording(View btnStop) {

		mapUI.setCompassEnabled(true);

		// Stop stop watch
		Chronometer timer = (Chronometer) findViewById(R.id.tripTimer);
		timer.stop();
		timer.setVisibility(View.INVISIBLE);

		TextView distance = (TextView) findViewById(R.id.tripDistance);
		distance.setVisibility(View.INVISIBLE);
		myLocationMarker.remove();

		if (routePoints.size() <= 1) {

			mMap.clear();

			Toast toast = Toast.makeText(getApplicationContext(),
					resourceHandler
							.getString(R.string.message_single_route_point),
					Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();

		} else {

			endMarker = mMap.addMarker(new MarkerOptions().position(
					new LatLng(lastLocation.getLatitude(), lastLocation
							.getLongitude())).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.finish)));

			// Convert in terms of minutes
			float elapsedTime = (float) (SystemClock.elapsedRealtime() - timer
					.getBase()) / (float) (1000 * 60);

			Intent intent = new Intent(this, RouteSave.class);
			intent.putParcelableArrayListExtra("routePoints", routePoints);
			intent.putExtra("totalDistance", totalDistance);
			intent.putExtra("elapsedTime", elapsedTime);
			intent.putExtra("avgSpeed",
					calculateSpeed(totalDistance, elapsedTime));
			startActivity(intent);
		}

		routePoints.clear();
		locManager.removeUpdates(locationListener);

		// Hide Stop Button
		btnStop.setVisibility(View.INVISIBLE);
		ImageView btnStart = (ImageView) findViewById(R.id.start_button);
		btnStart.setVisibility(View.VISIBLE);
	}

	/**
	 * Calculate speed from distance and time
	 * 
	 * @param distance
	 * @param duration
	 * @return
	 */
	private float calculateSpeed(float distance, float duration) {
		// TODO Implement a full proof method to calculate average speed from
		// distance and time

		float hours = duration / 60;
		duration = duration % 60;
		hours += duration / 60 * 100;

		return distance / hours;
	}

	/**
	 * Move the camera to the location specified
	 * 
	 * @param newLocation
	 */
	private void moveCamera(Location newLocation, float zoomLevel) {
		CameraPosition camPos = new CameraPosition.Builder()
				.target(new LatLng(newLocation.getLatitude(), newLocation
						.getLongitude())).zoom(zoomLevel).build();

		CameraUpdate cameraUpdate = CameraUpdateFactory
				.newCameraPosition(camPos);

		mMap.animateCamera(cameraUpdate);
	}
	
	private void setName(final Session session) {
	    // Make an API call to get user data and define a 
	    // new callback to handle the response.
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {
	    		@Override
	    		public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {

	                    Log.i("pratik", "username "+user.getId()+user.getFirstName() + " " + user.getLastName());
	                    fbUserId.setText(user.getId());
	                }
	            }
	            if (response.getError() != null) {
	                // Handle errors, will do so later.
	            }
	        }

	    });
	    request.executeAsync();
	} 
}
