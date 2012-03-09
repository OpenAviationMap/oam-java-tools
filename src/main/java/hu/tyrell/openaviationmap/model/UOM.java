package hu.tyrell.openaviationmap.model;

/**
 * Enumeration of units of measurements, like feet, meters, flight level,
 * mostly for elevation / height.
 */
public enum UOM {
	FT(0.3048), M(1.0), FL(304.8), NM(1852);

	/**
	 * The length of this unit of measurement in meters.
	 */
	private double inMeters;

	/**
	 * Constructor.
	 *
	 * @param inMeters the length of this unit of measurement in meters.
	 */
	private UOM(double inMeters) {
	    this.inMeters = inMeters;
	}

    /**
     * @return the inMeters
     */
    public double getInMeters() {
        return inMeters;
    }
}
