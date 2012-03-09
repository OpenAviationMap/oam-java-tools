package hu.tyrell.openaviationmap.model;

/**
 * An object describing distance.
 */
public class Distance {
    /**
     * The distance measurement.
     */
    private double distance;

    /**
     * The unit of measurement used.
     */
    private UOM uom;

    /**
     * Get the distance in a specific unit of measurement.
     *
     * @param requestedUom the desired unit of measurement
     * @return the distance object converted to the desired
     *         unit of measurement. returns this very object if the
     *         desired unit of measurement is the same as this one.
     */
    public Distance inUom(UOM requestedUom) {
        if (this.uom == requestedUom) {
            return this;
        }

        double inMeters = distance * uom.getInMeters();
        Distance convertedDistance = new Distance();
        convertedDistance.setUom(requestedUom);
        convertedDistance.setDistance(inMeters / requestedUom.getInMeters());

        return convertedDistance;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
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
}
