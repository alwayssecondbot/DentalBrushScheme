package ru.yanes.config;

import java.util.Arrays;

/**
 * Stores all visual and geometric metrics for rendering the jaw and teeth.
 */
public final class DentalGeometryConstants {
	/**
	 * Horizontal and vertical radius of the elliptical arcs for the jaws.
	 */
	public static final int RADIUS_X = 350;
	public static final int RADIUS_Y = 430;
	/**
	 * Start and end angles of the upper jaw arc (in radians).
	 */
	public static final double START_ANGLE_UPPER = Math.PI * 1.05;
	public static final double END_ANGLE_UPPER = Math.PI * 1.95;
	/**
	 * Start angle of the lower jaw arc (in radians).
	 */
	public static final double START_ANGLE_LOWER = Math.PI * 0.95;
	/**
	 * End angle of the lower jaw arc (in radians).
	 */
	public static final double END_ANGLE_LOWER = Math.PI * 0.05;
	/**
	 * Tooth width in pixels.
	 */
	public static final int TOOTH_WIDTH = 45;
	/**
	 * Tooth height in pixels.
	 */
	public static final int TOOTH_HEIGHT = 55;
	/**
	 * Tooth click radius.
	 */
	public static final int TOOTH_RADIUS = 25;
	/**
	 * Space (triangle) width and height in pixels.
	 */
	public static final int SPACE_WIDTH = 15;
	public static final int SPACE_HEIGHT = 45;
	/**
	 * Space click radius.
	 */
	public static final int SPACE_RADIUS = 13;
	/**
	 * Array of x-coordinates of the space's base triangle (relative to the center).
	 */
	public static final int[] BASIC_SPACE_X = new int[]{0, SPACE_WIDTH, -SPACE_WIDTH};
	/**
	 * Arrays of y-coordinates of the space's outer and inner triangles.
	 */
	public static final int[] OUTER_SPACE_Y = new int[]{-TOOTH_HEIGHT / 3, -SPACE_HEIGHT, -SPACE_HEIGHT};
	public static final int[] INNER_SPACE_Y = new int[]{TOOTH_HEIGHT / 3, SPACE_HEIGHT, SPACE_HEIGHT};
	/** Y of outer triangle center */
	public static final double LOCAL_OUTER_Y = Arrays.stream(OUTER_SPACE_Y).average().getAsDouble();
	/** Y of inner triangle center */
	public static final double LOCAL_INNER_Y = Arrays.stream(INNER_SPACE_Y).average().getAsDouble();
	/**
	 * The diameter of the circle representing the brush, in pixels.
	 */
	public static final int BRUSH_DIAMETER = 70;
}
