package hu.tyrell.openaviationmap.model.oam;

import hu.tyrell.openaviationmap.model.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An Open Street Map 'way' element.
 */
public class Way {
    /**
     * The points that make up the closed polygon.
     */
    private List<Point> pointList;

    /**
     * Tags of this way element.
     */
    private Map<String, String> tags;

    /**
     * Default constructor.
     */
    public Way() {
        pointList = new Vector<Point>();
        tags      = new HashMap<String, String>();
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

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
