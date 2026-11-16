package ru.yanes.config;

import java.awt.*;

import static ru.yanes.config.DentalGeometryConstants.SPACE_OUTLINE_THICKNESS;

/**
 * Stores layout and drawing constants of the user interface that are not part of the dental geometry:
 * sizes of the window and panels, limits of the comment field and line styles (strokes) of the spaces.
 * <p>
 * Geometry of the scheme (positions and sizes of teeth, spaces and brushes) is stored in
 * {@link DentalGeometryConstants}; colors and fonts are stored in {@link UITheme}.
 * All sizes are in pixels.
 * <p>
 * All constants are immutable: sizes are stored as pairs of primitive values instead of arrays,
 * and strokes are {@link BasicStroke} objects, which can't be changed after creation.
 */
public final class UIConstants {
	/** Minimal width of the app window. */
	public static final int WINDOW_MIN_WIDTH = 1600;
	/** Minimal height of the app window. */
	public static final int WINDOW_MIN_HEIGHT = 1300;

	/**
	 * Preferred width of {@code CommentPanel}.
	 *
	 * @deprecated The {@code CommentPanel} is disabled, instead the comment is embedded in the {@link ru.yanes.panels.DentalPanel}.
	 */
	@Deprecated
	public static final int COMMENT_PANEL_PREFERRED_WIDTH = 400;
	/**
	 * Preferred height of {@code CommentPanel}. {@code 0} means that the height is defined by the layout manager of the window.
	 *
	 * @deprecated The {@code CommentPanel} is disabled, instead the comment is embedded in the {@link ru.yanes.panels.DentalPanel}.
	 */
	@Deprecated
	public static final int COMMENT_PANEL_PREFERRED_HEIGHT = 0;

	/** Preferred width of {@link ru.yanes.panels.BrushPanel}. */
	public static final int BRUSH_PANEL_PREFERRED_WIDTH = 400;
	/**
	 * Preferred height of {@link ru.yanes.panels.BrushPanel}.
	 * {@code 0} means that the height is defined by the layout manager of the window.
	 */
	public static final int BRUSH_PANEL_PREFERRED_HEIGHT = 0;

	/**
	 * Preferred width of {@link ru.yanes.panels.ToolPanel}.
	 * {@code 0} means that the width is defined by the layout manager of the window.
	 */
	public static final int TOOL_PANEL_PREFERRED_WIDTH = 0;
	/** Preferred height of {@link ru.yanes.panels.ToolPanel}. */
	public static final int TOOL_PANEL_PREFERRED_HEIGHT = 30;

	/** Maximum number of rows in the comment text area of the {@link ru.yanes.panels.DentalPanel}. */
	public static final int COMMENT_AREA_ROW_LIMIT = 20;
	/**
	 * Maximum number of columns (characters per row) in the comment text area of the {@link ru.yanes.panels.DentalPanel}.
	 * The total character limit is {@code COMMENT_AREA_ROW_LIMIT * COMMENT_AREA_COLUMN_LIMIT}.
	 */
	public static final int COMMENT_AREA_COLUMN_LIMIT = 25;

	/*
	 * The dash parameters must be declared before the strokes that use them:
	 * static fields are initialized in the order they are written.
	 */

	/** Miter limit of the dashed outline of an empty space (see {@link BasicStroke}). */
	private static final float DASH_MITER_LIMIT = 10F;
	/**
	 * Pattern of the dashed outline of an empty space: length of a dash and of a gap, in pixels.
	 * Private and safe to share: {@link BasicStroke} copies the array on creation.
	 */
	private static final float[] DASH_PATTERN = new float[]{19.0f, 5.0f};
	/** Offset of the start of the dash pattern, in pixels. */
	private static final float DASH_PHASE = 0.0f;

	/**
	 * Line style of the outline of an available space without a brush: a dashed line
	 * with the {@link DentalGeometryConstants#SPACE_OUTLINE_THICKNESS} thickness,
	 * so an empty space is visually different from a filled one.
	 */
	public static final BasicStroke EMPTY_SPACE_STROKE = new BasicStroke(SPACE_OUTLINE_THICKNESS,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, DASH_MITER_LIMIT, DASH_PATTERN, DASH_PHASE);

	/**
	 * Line style of the outline of a space with a brush or of an unavailable space: a solid line
	 * with the {@link DentalGeometryConstants#SPACE_OUTLINE_THICKNESS} thickness.
	 */
	public static final BasicStroke FILLED_SPACE_STROKE = new BasicStroke(SPACE_OUTLINE_THICKNESS);

	/**
	 * Private constructor: the class only holds constants and must not be instantiated.
	 */
	private UIConstants() {}
}
