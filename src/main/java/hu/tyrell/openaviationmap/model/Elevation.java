package hu.tyrell.openaviationmap.model;

/**
 * A class to represent a certain elevation.
 */
public class Elevation {
	/**
	 * The elevation itself;
	 */
	private double elevation;

	/**
	 * The unit of measurement used in the elevation.
	 */
	private UOM uom;

	/**
	 * The reference used in the elevations.
	 */
	private ElevationReference reference;

	/**
	 * @return the elevation
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * @param elevation the elevation to set
	 */
	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	/**
	 * @return the uom
	 */
	public UOM getUom() {
		return uom;
	}

	/**
	 * @param uom the uom to set
	 */
	public void setUom(UOM uom) {
		this.uom = uom;
	}

	/**
	 * @return the reference
	 */
	public ElevationReference getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(ElevationReference reference) {
		this.reference = reference;
	}
}
