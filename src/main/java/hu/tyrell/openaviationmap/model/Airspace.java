package hu.tyrell.openaviationmap.model;

public class Airspace {

	/**
	 * The designator of the airspace.
	 */
	private String designator;

	/**
	 * The name of the airspace.
	 */
	private String name;

	/**
	 * The airspace class, A, B, C, ..., G
	 */
	private String airspaceClass;

	/**
	 * The airspace type, TMA, CTR, military, Restricted, etc.
	 */
	private String type;

	/**
	 * The lower limit of the airspace.
	 */
	private Elevation lowerLimit;

	/**
	 * The upper limit of the airspace.
	 */
	private Elevation upperLimit;

	/**
	 * The shape of the airspace.
	 */
	private Boundary boundary;

	/**
	 * Remarks about the airspace.
	 */
	private String remarks;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the airspaceClass
	 */
	public String getAirspaceClass() {
		return airspaceClass;
	}

	/**
	 * @param airspaceClass the airspaceClass to set
	 */
	public void setAirspaceClass(String airspaceClass) {
		this.airspaceClass = airspaceClass;
	}

	/**
	 * @return the designator
	 */
	public String getDesignator() {
		return designator;
	}

	/**
	 * @param designator the designator to set
	 */
	public void setDesignator(String designator) {
		this.designator = designator;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the lowerLimit
	 */
	public Elevation getLowerLimit() {
		return lowerLimit;
	}

	/**
	 * @param lowerLimit the lowerLimit to set
	 */
	public void setLowerLimit(Elevation lowerLimit) {
		this.lowerLimit = lowerLimit;
	}

	/**
	 * @return the upperLimit
	 */
	public Elevation getUpperLimit() {
		return upperLimit;
	}

	/**
	 * @param upperLimit the upperLimit to set
	 */
	public void setUpperLimit(Elevation upperLimit) {
		this.upperLimit = upperLimit;
	}

    /**
     * @return the boundary
     */
    public Boundary getBoundary() {
        return boundary;
    }

    /**
     * @param boundary the boundary to set
     */
    public void setBoundary(Boundary boundary) {
        this.boundary = boundary;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
