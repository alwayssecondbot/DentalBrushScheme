package ru.yanes.config;

import java.util.Arrays;

/**
 * Stores all visual and geometric metrics for rendering the jaw and teeth.
 * <p>
 * The constants are shared by the rendering code ({@link ru.yanes.panels.DentalPanel},
 * {@link ru.yanes.panels.BrushPanel}) and the hit-testing code ({@link ru.yanes.controllers.DentalGeometry}),
 * so a click is always checked against the same positions where the shapes are drawn.
 * All sizes are in pixels, all angles are in radians.
 */
public final class DentalGeometryConstants {
	/**
	 * Horizontal radius of the elliptical arcs for the jaws, in pixels.
	 */
	public static final int RADIUS_X = 350;
	/**
	 * Vertical radius of the elliptical arcs for the jaws, in pixels.
	 */
	public static final int RADIUS_Y = 430;
	/**
	 * Start angle of the upper jaw arc (in radians). The first upper tooth is placed at this angle.
	 */
	public static final double START_ANGLE_UPPER = Math.PI * 1.05;
	/**
	 * End angle of the upper jaw arc (in radians). The last upper tooth is placed at this angle.
	 */
	public static final double END_ANGLE_UPPER = Math.PI * 1.95;
	/**
	 * Start angle of the lower jaw arc (in radians). The first lower tooth is placed at this angle.
	 */
	public static final double START_ANGLE_LOWER = Math.PI * 0.95;
	/**
	 * End angle of the lower jaw arc (in radians). The last lower tooth is placed at this angle.
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
	 * Tooth click radius: maximum distance from the tooth center, in pixels, at which a click hits the tooth.
	 */
	public static final int TOOTH_RADIUS = 25;
	/**
	 * Half-width of the space triangle base, in pixels.
	 */
	public static final int SPACE_WIDTH = 15;
	/**
	 * Distance from the space center to the base of the space triangle, in pixels.
	 */
	public static final int SPACE_HEIGHT = 45;
	/**
	 * Space click radius: maximum distance from the center of a space triangle, in pixels,
	 * at which a click hits this triangle.
	 */
	public static final int SPACE_RADIUS = 13;
	/**
	 * Array of x-coordinates of the space triangle vertices (relative to the space center, before rotation).
	 * Shared by the outer and inner triangles.
	 */
	public static final int[] BASIC_SPACE_X = new int[]{0, SPACE_WIDTH, -SPACE_WIDTH};
	/**
	 * Array of y-coordinates of the outer space triangle vertices (relative to the space center, before rotation).
	 * Negative values point away from the center of the jaw.
	 */
	public static final int[] OUTER_SPACE_Y = new int[]{-TOOTH_HEIGHT / 3, -SPACE_HEIGHT, -SPACE_HEIGHT};
	/**
	 * Array of y-coordinates of the inner space triangle vertices (relative to the space center, before rotation).
	 * Positive values point towards the center of the jaw.
	 */
	public static final int[] INNER_SPACE_Y = new int[]{TOOTH_HEIGHT / 3, SPACE_HEIGHT, SPACE_HEIGHT};
	/**
	 * Local y-coordinate of the outer triangle center (the average of {@link #OUTER_SPACE_Y}), used for hit-testing.
	 */
	public static final double LOCAL_OUTER_Y = Arrays.stream(OUTER_SPACE_Y).average().getAsDouble();
	/**
	 * Local y-coordinate of the inner triangle center (the average of {@link #INNER_SPACE_Y}), used for hit-testing.
	 */
	public static final double LOCAL_INNER_Y = Arrays.stream(INNER_SPACE_Y).average().getAsDouble();
	/**
	 * The diameter of the circle representing the brush, in pixels.
	 */
	public static final int BRUSH_DIAMETER = 70;

	public static final float SELECTED_BRUSH_STROKE_THICKNESS = 10F;

	public static final int SELECTED_BRUSH_STROKE_OFFSET = 5;

	public static final float BRUSH_STROKE_THICKNESS = 2.5F;


}
