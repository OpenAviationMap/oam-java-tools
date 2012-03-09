package hu.tyrell.openaviationmap.model;

/**
 * A boundary type described as a circle of some radius around a center
 * point.
 */
public class Circle implements Boundary {
    /**
     * The center point of the circle.
     */
    private Point center;

    /**
     * The radius of the circle.
     */
    private Distance radius;

    /**
     * Return the type of this boundary.
     *
     * @return Circle
     */
    @Override
    public Type getType() {
        return Type.CIRCLE;
    }

    /**
     * @return the center
     */
    public Point getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Point center) {
        this.center = center;
    }

    /**
     * @return the radius
     */
    public Distance getRadius() {
        return radius;
    }

    /**
     * @param radius the radius to set
     */
    public void setRadius(Distance radius) {
        this.radius = radius;
    }

}
