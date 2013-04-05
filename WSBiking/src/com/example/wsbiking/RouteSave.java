package com.example.wsbiking;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author Leon Dmello This the activity which gets fired when route recording
 *         is stopped It allows to discard or save a route with the facility to
 *         provide a title and description for the route.
 * 
 */
public class RouteSave extends Activity {
	private ArrayList<RoutePoint> routePoints;
	private float totalDistance, elapsedTime, avgSpeed;
	private DatabaseHandler dbHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route_save);
		dbHandler = DatabaseHandler.getInstance(this);
		InitializeElements();
	}

	@Override
	public void onStop() {
		dbHandler.close();
		super.onStop();
	}

	private void InitializeElements() {
		Intent recordActivity = getIntent();

		this.routePoints = recordActivity.getExtras().getParcelableArrayList(
				"routePoints");
		this.totalDistance = recordActivity.getFloatExtra("totalDistance", 0);
		this.elapsedTime = recordActivity.getFloatExtra("elapsedTime", 0);
		this.avgSpeed = recordActivity.getFloatExtra("avgSpeed", 0);

		Button btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setEnabled(false);

		EditText edtTitle = (EditText) findViewById(R.id.edtTxTitle);

		edtTitle.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable titleText) {
				ToggleSave(titleText);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void ToggleSave(Editable titleText) {
		Button btnSave = (Button) findViewById(R.id.btnSave);

		if (!btnSave.isEnabled() && titleText.length() > 0)
			btnSave.setEnabled(true);
		else if (titleText.length() == 0)
			btnSave.setEnabled(false);
	}

	public void DiscardRoute(View btnDiscard) {
		Toast toast = Toast.makeText(getApplicationContext(),
				"Route Discarded", Toast.LENGTH_SHORT);

		toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();

		this.finish();
	}

	public void SaveRoute(View btnSave) {
		EditText edtTitle = (EditText) findViewById(R.id.edtTxTitle);
		EditText edtDesc = (EditText) findViewById(R.id.edtTxtDesc);

		String routeTitle = edtTitle.getText().toString();
		String routeDesc = edtDesc.getText().toString();

		if (dbHandler.addRoute(this.routePoints, routeTitle, routeDesc,
				this.totalDistance, this.elapsedTime, this.avgSpeed)) {

			Toast toast = Toast.makeText(getApplicationContext(),
					"Route Saved", Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Route Discarded", Toast.LENGTH_SHORT);

			toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
		
		this.finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.route_save, menu);
		return true;
	}
}
