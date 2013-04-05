package com.example.wsbiking;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * 
 * @author Leon Dmello All the database interactions will go in this Singleton
 *         handler for DB
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables

	/**
	 * instance.
	 */
	private static DatabaseHandler instance = null;
	private static SQLiteDatabase appWritableDb;

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Log tag for logging errors
	private static final String LOG_TAG = "DB Handler";

	// Database Name
	private static final String DATABASE_NAME = "routesManager";

	// Routes table name
	private static final String TABLE_ROUTES = "routes";
	// Route points table name
	private static final String TABLE_ROUTE_POINTS = "routePoints";

	// Routes Table Columns names
	private static final String ROUTEID = "routeID";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String AVGSPEED = "speed";
	private static final String DISTANCE = "distance";
	private static final String DURATION = "duration";

	// Routes Table Column indexes
	private static final Integer ROUTEIDINDEX = 0;
	private static final Integer NAMEINDEX = 1;
	private static final Integer DESCRIPTIONINDEX = 2;
	private static final Integer AVGSPEEDINDEX = 3;
	private static final Integer DISTANCEINDEX = 4;
	private static final Integer DURATIONINDEX = 5;

	// Route points Table Columns names
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";

	/**
	 * @return instance.
	 */
	public static DatabaseHandler getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHandler(context.getApplicationContext());
		}

		return instance;
	}

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * Returns a writable database instance in order not to open and close many
	 * SQLiteDatabase objects simultaneously
	 * 
	 * @return a writable instance to SQLiteDatabase
	 */
	public SQLiteDatabase getMyWritableDatabase() {
		if ((appWritableDb == null) || (!appWritableDb.isOpen())) {
			appWritableDb = this.getWritableDatabase();
		}

		return appWritableDb;
	}

	@Override
	public void close() {
		super.close();
		if (appWritableDb != null) {
			appWritableDb.close();
			appWritableDb = null;
		}
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_ROUTE_TABLE = "CREATE TABLE " + TABLE_ROUTES + "("
				+ ROUTEID + " INTEGER PRIMARY KEY AUTOINCREMENT," + NAME
				+ " TEXT," + DESCRIPTION + " TEXT," + AVGSPEED + " REAL,"
				+ DISTANCE + " REAL," + DURATION + " REAL" + "); ";

		String CREATE_ROUTE_POINTS_TABLE = "CREATE TABLE " + TABLE_ROUTE_POINTS
				+ "(" + ROUTEID + " INTEGER," + LATITUDE + " REAL," + LONGITUDE
				+ " REAL," + "FOREIGN KEY(" + ROUTEID + ") REFERENCES "
				+ TABLE_ROUTES + "(" + ROUTEID + ")" + "); ";

		try {
			db.execSQL(CREATE_ROUTE_TABLE);
			db.execSQL(CREATE_ROUTE_POINTS_TABLE);
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to create tables: " + ex.getMessage());
		}
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String DROP_TABLES = "DROP TABLE IF EXISTS " + TABLE_ROUTE_POINTS
				+ "; " + "DROP TABLE IF EXISTS " + TABLE_ROUTES + "; ";
		// Drop older table if existed

		try {
			db.execSQL(DROP_TABLES);
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to drop tables: " + ex.getMessage());
		}

		// Create tables again
		onCreate(db);
	}

	// Create a new route entry
	// TODO Null handling to be done while calling
	public boolean addRoute(ArrayList<RoutePoint> routePoints, String name,
			String description, float distance, float duration, float avgSpeed) {

		long insertedRowID;
		boolean result = false;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put(NAME, name);
			values.put(DESCRIPTION, description);
			values.put(DISTANCE, distance);
			values.put(DURATION, duration);
			values.put(AVGSPEED, avgSpeed);

			// Inserting Row
			insertedRowID = db.insert(TABLE_ROUTES, null, values);

			if (insertedRowID == -1)
				throw new Exception("Failed to insert Route entry");

			if (insertRoutePoints(routePoints, insertedRowID))
				result = true;

		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to insert route values: " + ex.getMessage());
		}

		return result;
	}

	private boolean insertRoutePoints(ArrayList<RoutePoint> routePoints,
			long insertedRowID) {

		SQLiteDatabase db = this.getWritableDatabase();
		db.beginTransaction();

		boolean result = false;

		try {
			String sqlString = "Insert into " + TABLE_ROUTE_POINTS + " ("
					+ ROUTEID + ", " + LATITUDE + "," + LONGITUDE
					+ ") values(?,?,?)";

			SQLiteStatement sqlSTMT = db.compileStatement(sqlString);

			int routeSize = routePoints.size();

			for (int rowCounter = 0; rowCounter < routeSize; rowCounter++) {

				RoutePoint point = routePoints.get(rowCounter);

				sqlSTMT.bindDouble(1, insertedRowID);
				sqlSTMT.bindDouble(2, point.getLatitude());
				sqlSTMT.bindDouble(3, point.getLongitude());

				sqlSTMT.execute();
			}

			db.setTransactionSuccessful();
			result = true;
		} catch (Exception ex) {
			Log.e(LOG_TAG, "Failed to insert route points: " + ex.getMessage());
		} finally {
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Returns an array list containing all routes saved
	 * 
	 * @return
	 */
	public ArrayList<Route> getRoutes() {

		Cursor cursor = null;
		ArrayList<Route> allRoutes = null;

		try {

			SQLiteDatabase db = this.getWritableDatabase();

			cursor = db.query(TABLE_ROUTES, new String[] { ROUTEID, NAME,
					DESCRIPTION, AVGSPEED, DURATION, DISTANCE }, null, null,
					null, null, null);

			if (cursor.moveToFirst()) {
				allRoutes = new ArrayList<Route>();
				Integer routeID;
				String routeName, routeDesc;
				float routeSpeed, routeDuration, routeDistance;
				
				while (!cursor.isAfterLast()) {
					
					routeID = cursor.getInt(ROUTEIDINDEX);
					routeName = cursor.getString(NAMEINDEX);
					routeDesc = cursor.getString(DESCRIPTIONINDEX);
					routeSpeed = cursor.getInt(AVGSPEEDINDEX);
					routeDuration = cursor.getInt(DURATIONINDEX);
					routeDistance = cursor.getInt(DISTANCEINDEX);
					
					allRoutes.add(new Route(routeID, routeName, routeDesc, routeSpeed, routeDuration, routeDistance));
					
					cursor.moveToNext();
				}
			}

			cursor.close();
			
		} catch (Exception ex) {
			Log.e(LOG_TAG,
					"Failed to get all routes from route table: "
							+ ex.getMessage());
		}

		return allRoutes;
	}
}
