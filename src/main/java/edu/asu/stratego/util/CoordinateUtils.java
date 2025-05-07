package edu.asu.stratego.util;

import java.awt.Point;

public class CoordinateUtils {

    /**
     * Rotates a point 180 degrees around the center of the 10x10 board.
     *
     * @param p the original point
     * @return a new point rotated 180 degrees
     */
    public static Point rotate180(Point p) {
        return new Point(9 - p.x, 9 - p.y);
    }
}
