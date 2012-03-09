package hu.tyrell.openaviationmap.model;

/**
 * An airspace boundary.
 */
public interface Boundary {
    /** The type of the boundary. */
    public enum Type {
        RING, CIRCLE;
    }

    /**
     * Return the type of boundary.
     *
     * @return the type of boundary.
     */
    public Type getType();
}
