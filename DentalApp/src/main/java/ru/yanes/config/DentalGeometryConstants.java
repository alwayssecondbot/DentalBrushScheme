package ru.yanes.config;

import java.util.Arrays;

/**
 * Stores all visual and geometric metrics for rendering the jaw and teeth.
 * <p>
 * The constants are shared by the rendering code ({@link ru.yanes.panels.DentalPanel},
 * {@link ru.yanes.panels.BrushPanel}) and the hit-testing code ({@link ru.yanes.controllers.DentalGeometry}),
 * so a click is always checked against the same positions where the shapes are drawn.
 * Line thicknesses of the shapes are stored here as well, next to the sizes of the shapes they outline.
 * All sizes are in pixels, all angles are in radians.
 * <p>
 * Colors and fonts are not stored here: see {@link UITheme}. Sizes of the window and panels
 * and other layout values are stored in {@link UIConstants}.
 */
public final class DentalGeometryConstants {
	/**
	 * Horizontal radius of the elliptical arcs for the jaws, in pixels.
	 */
	public static final int JAW_RADIUS_X = 350;
	/**
	 * Vertical radius of the elliptical arcs for the jaws, in pixels.
	 */
	public static final int JAW_RADIUS_Y = 430;
	/**
	 * Start angle of the upper jaw arc (in radians). The first upper tooth is placed at this angle.
	 */
	public static final double UPPER_JAW_START_ANGLE = Math.PI * 1.05;
	/**
	 * End angle of the upper jaw arc (in radians). The last upper tooth is placed at this angle.
	 */
	public static final double UPPER_JAW_END_ANGLE = Math.PI * 1.95;
	/**
	 * Start angle of the lower jaw arc (in radians). The first lower tooth is placed at this angle.
	 */
	public static final double LOWER_JAW_START_ANGLE = Math.PI * 0.95;
	/**
	 * End angle of the lower jaw arc (in radians). The last lower tooth is placed at this angle.
	 */
	public static final double LOWER_JAW_END_ANGLE = Math.PI * 0.05;
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
	public static final int TOOTH_CLICK_RADIUS = 25;
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
	public static final int SPACE_CLICK_RADIUS = 13;
	/**
	 * Array of x-coordinates of the space triangle vertices (relative to the space center, before rotation).
	 * Shared by the outer and inner triangles.
	 * <p>
	 * Private because an array can't be made immutable: use {@link #spaceTriangleX()} to get a copy.
	 */
	private static final int[] SPACE_TRIANGLE_X = new int[]{0, SPACE_WIDTH, -SPACE_WIDTH};
	/**
	 * Array of y-coordinates of the outer space triangle vertices (relative to the space center, before rotation).
	 * Negative values point away from the center of the jaw.
	 * <p>
	 * Private because an array can't be made immutable: use {@link #outerSpaceTriangleY()} to get a copy.
	 */
	private static final int[] OUTER_SPACE_TRIANGLE_Y = new int[]{-TOOTH_HEIGHT / 3, -SPACE_HEIGHT, -SPACE_HEIGHT};
	/**
	 * Array of y-coordinates of the inner space triangle vertices (relative to the space center, before rotation).
	 * Positive values point towards the center of the jaw.
	 * <p>
	 * Private because an array can't be made immutable: use {@link #innerSpaceTriangleY()} to get a copy.
	 */
	private static final int[] INNER_SPACE_TRIANGLE_Y = new int[]{TOOTH_HEIGHT / 3, SPACE_HEIGHT, SPACE_HEIGHT};
	/**
	 * Local y-coordinate of the outer triangle center (the average of {@link #OUTER_SPACE_TRIANGLE_Y}), used for hit-testing.
	 */
	public static final double OUTER_SPACE_CENTER_Y = Arrays.stream(OUTER_SPACE_TRIANGLE_Y).average().getAsDouble();
	/**
	 * Local y-coordinate of the inner triangle center (the average of {@link #INNER_SPACE_TRIANGLE_Y}), used for hit-testing.
	 */
	public static final double INNER_SPACE_CENTER_Y = Arrays.stream(INNER_SPACE_TRIANGLE_Y).average().getAsDouble();

	/**
	 * Returns x-coordinates of the space triangle vertices (relative to the space center, before rotation).
	 * Shared by the outer and inner triangles.
	 *
	 * @return a new copy of the array on every call, so changing it does not affect other callers.
	 */
	public static int[] spaceTriangleX() {
		return SPACE_TRIANGLE_X.clone();
	}

	/**
	 * Returns y-coordinates of the outer space triangle vertices (relative to the space center, before rotation).
	 * Negative values point away from the center of the jaw.
	 *
	 * @return a new copy of the array on every call, so changing it does not affect other callers.
	 */
	public static int[] outerSpaceTriangleY() {
		return OUTER_SPACE_TRIANGLE_Y.clone();
	}

	/**
	 * Returns y-coordinates of the inner space triangle vertices (relative to the space center, before rotation).
	 * Positive values point towards the center of the jaw.
	 *
	 * @return a new copy of the array on every call, so changing it does not affect other callers.
	 */
	public static int[] innerSpaceTriangleY() {
		return INNER_SPACE_TRIANGLE_Y.clone();
	}
	/**
	 * The diameter of the circle representing the brush, in pixels.
	 */
	public static final int BRUSH_DIAMETER = 70;
	/**
	 * Line thickness of the shadow ring around the selected brush, in pixels.
	 */
	public static final float SELECTION_SHADOW_THICKNESS = 10F;
	/**
	 * Extra size of the shadow ring around the selected brush, in pixels.
	 * <p>
	 * The ring starts at the same upper-left corner as the brush circle but is {@code SELECTION_SHADOW_OFFSET}
	 * pixels larger, so its center is shifted by half of this value to the right and down. This is intended:
	 * the ring looks like a shadow falling from the upper-left corner of the circle.
	 */
	public static final int SELECTION_SHADOW_OFFSET = 5;
	/**
	 * Line thickness of the outline of the brush circles, in pixels.
	 */
	public static final float BRUSH_OUTLINE_THICKNESS = 2.5F;
	/**
	 * Vertical offset of each jaw arc center from the center of the dental panel, in pixels:
	 * the upper jaw is moved up by this value, the lower jaw is moved down.
	 * Used both for drawing and for hit-testing the jaws.
	 */
	public static final int JAW_VERTICAL_OFFSET = 40;
	/**
	 * Distance between the edge of a jaw arc and the "upper jaw" / "lower jaw" label, in pixels.
	 */
	public static final int JAW_LABEL_MARGIN = 100;
	/**
	 * Horizontal diameter of the arc of the rounded tooth corners, in pixels.
	 */
	public static final int TOOTH_CORNER_ARC_WIDTH = 20;
	/**
	 * Vertical diameter of the arc of the rounded tooth corners, in pixels.
	 */
	public static final int TOOTH_CORNER_ARC_HEIGHT = 20;
	/**
	 * Line thickness of the outline of the teeth, in pixels.
	 */
	public static final float TOOTH_OUTLINE_THICKNESS = 2.5F;
	/**
	 * Line thickness of the outline of the space triangles, in pixels.
	 * Used both for the dashed outline of an empty space and the solid outline of a filled one.
	 */
	public static final float SPACE_OUTLINE_THICKNESS = 2.0F;
	/**
	 * Line thickness of the rounded border of the comment text area, in pixels.
	 */
	public static final float COMMENT_AREA_BORDER_THICKNESS = 2F;

	/**
	 * Private constructor: the class only holds constants and must not be instantiated.
	 */
	private DentalGeometryConstants() {}
}
