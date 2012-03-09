package hu.tyrell.openaviationmap.model;

import java.util.List;

/**
 * A series of points, where the last point is the same as the first one.
 */
public class Ring implements Boundary {
	private List<Point> pointList;

	/**
	 * Return the type of this boundary.
	 *
	 * @return Ring
	 */
	@Override
    public Type getType() {
	    return Type.RING;
	}

	/**
	 * @return the pointList
	 */
	public List<Point> getPointList() {
		return pointList;
	}

	/**
	 * @param pointList the pointList to set
	 */
	public void setPointList(List<Point> pointList) {
		this.pointList = pointList;
	}
}
