package com.example.wsbiking;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 
 * @author Leon Dmello
 * The route point class to record latitude and longitude of all route points
 * 
 */
public class RoutePoint implements Parcelable {
	private Double latitude, longitude;

	RoutePoint(Double passedLatitude, Double passedLongitude) {
		this.latitude = passedLatitude;
		this.longitude = passedLongitude;
	}
	
	/**
     * This will be used only by the MyCreator
     * @param source
     */
    public RoutePoint(Parcel source){
          /*
           * Reconstruct from the Parcel
           */
    		latitude = source.readDouble();
    		longitude = source.readDouble();
    }

	public Double getLatitude() {
		return this.latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(latitude);
		out.writeDouble(longitude);
	}

	public static final Parcelable.Creator<RoutePoint> CREATOR = new Parcelable.Creator<RoutePoint>() {
		public RoutePoint createFromParcel(Parcel in) {
			return new RoutePoint(in);
		}

		public RoutePoint[] newArray(int size) {
			return new RoutePoint[size];
		}
	};

}
