package com.example.wsbiking;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class ViewRoutes extends Activity {

	private static final String ROUTEID = "routeID";
	private static final String AVGSPEED = "speed";
	private static final String DISTANCE = "distance";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String DURATION = "duration";

	private DatabaseHandler dbHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_routes);
		dbHandler = DatabaseHandler.getInstance(this);
		populateList();
	}

	private void populateList() {
		// TODO Auto-generated method stub
		ArrayList<Route> routes = dbHandler.getRoutes();

		try {
			if (routes != null) {
				RouteAdapter adapter = new RouteAdapter(this,
						R.layout.singleroute, routes);

				ListView routesListView = (ListView) findViewById(R.id.routesList);

				routesListView.setAdapter(adapter);
			}
		} catch (Exception ex) {
//			Log.e("Route List Error", ex.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_routes, menu);
		return true;
	}

}
