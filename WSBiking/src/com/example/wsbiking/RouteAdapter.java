package com.example.wsbiking;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RouteAdapter extends ArrayAdapter<Route> {
	private Context context;
	private int layoutResourceId;
	private ArrayList<Route> routes = null;

	public RouteAdapter(Context context, int layoutResourceId,
			ArrayList<Route> routes) {
		super(context, layoutResourceId, routes);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.routes = routes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RouteHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new RouteHolder();
			holder.txtVwRouteTitle = (TextView) row
					.findViewById(R.id.txtVwRouteTitle);
			holder.TxtVwRouteDesc = (TextView) row
					.findViewById(R.id.TxtVwRouteDesc);
			holder.txtVwRouteID = (TextView) row
					.findViewById(R.id.txtVwRouteID);
			holder.imgBtnShowMap = (ImageView) row
					.findViewById(R.id.imgBtnShowMap);
			holder.txtVwSpeed = (TextView) row.findViewById(R.id.txtVwSpeed);
			holder.txtVwDistance = (TextView) row
					.findViewById(R.id.txtVwDistance);
			holder.txtVwTime = (TextView) row.findViewById(R.id.txtVwTime);

			row.setTag(holder);
		} else {
			holder = (RouteHolder) row.getTag();
		}

		Route route = routes.get(position);
		
		holder.txtVwRouteTitle.setText(route.getTitle());
		holder.TxtVwRouteDesc.setText(route.getDescription());
		holder.txtVwRouteID.setText(String.valueOf(route.getID()));
		holder.txtVwSpeed.setText(String.valueOf(route.getSpeed()));
		holder.txtVwDistance.setText(String.valueOf(route.getDistance()));
		holder.txtVwTime.setText(String.valueOf(route.getDuration()));

		return row;
	}

	static class RouteHolder {
		TextView txtVwRouteTitle;
		TextView TxtVwRouteDesc;
		TextView txtVwRouteID;
		ImageView imgBtnShowMap;
		TextView txtVwSpeed;
		TextView txtVwDistance;
		TextView txtVwTime;
	}
}
