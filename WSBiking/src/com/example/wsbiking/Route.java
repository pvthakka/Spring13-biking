package com.example.wsbiking;

/**
 * The route class to record teh various paraameters associated with a route
 * 
 * @author Leon Dmello
 * 
 */
public class Route {
	private Integer ID;
	private String title, description;
	private float speed, duration, distance;

	public Route(Integer routeID, String routeName, String routeDesc,
			float avgSpeed, float routeDuration, float routeDistance) {
		this.ID = routeID;
		this.title = routeName;
		this.description = routeDesc;
		this.speed = avgSpeed;
		this.duration = routeDuration;
		this.distance = routeDistance;
	}

	public Integer getID() {
		return this.ID;
	}

	public String getTitle() {
		return this.title;
	}

	public String getDescription() {
		return this.description;
	}

	public float getSpeed() {
		return this.speed;
	}

	public float getDuration() {
		return this.duration;
	}

	public float getDistance() {
		return this.distance;
	}
}
