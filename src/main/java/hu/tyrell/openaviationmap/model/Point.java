package hu.tyrell.openaviationmap.model;

/**
 * A 2 dimensional geographical point in space.
 */
public class Point {
	/**
	 * The latitude.
	 */
	private double latitude;

	/**
	 * The longitude.
	 */
	private double longitude;

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
