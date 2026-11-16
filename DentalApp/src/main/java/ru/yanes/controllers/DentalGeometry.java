package ru.yanes.controllers;

import ru.yanes.models.Brush;

import static ru.yanes.config.DentalGeometryConstants.*;

import java.util.Optional;

/**
 * A utility class with pure geometric functions for hit-testing clicks on the dental scheme.
 * <p>
 * The methods decide whether a click point hits a tooth, a part of a space (inner or outer) or a brush swatch.
 * They do not depend on Swing and do not change any state, so they can be tested without a graphical environment.
 * All sizes and angles are taken from {@link ru.yanes.config.DentalGeometryConstants}.
 * <p>
 * The returned positions are <b>indices</b> in the {@link ru.yanes.models.Mouth} arrays
 * ({@link ru.yanes.models.Mouth#getTooth(int)}, {@link ru.yanes.models.Mouth#getSpace(int)}),
 * not FDI numbers of teeth.
 */
public class DentalGeometry {

	/**
	 * Checks whether the click hits one of the teeth on the specified jaw.
	 * <p>
	 * Teeth are placed on an elliptical arc around the given center, 16 teeth per jaw.
	 * A click is a hit if its distance to the tooth center is not greater than {@code TOOTH_RADIUS}.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param centerX x-coordinate of the center of the jaw arc;
	 * @param centerY y-coordinate of the center of the jaw arc;
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return {@code Optional.of(index)} with the tooth index in the {@link ru.yanes.models.Mouth} teeth array
	 * (0–15 for the upper jaw, 16–31 for the lower jaw) if a tooth was hit, otherwise {@code Optional.empty()}.
	 */
	public static Optional<Integer> checkToothClick(int mouseX, int mouseY,
	                              int centerX, int centerY, boolean isUpperJaw) {
		double startAngle = isUpperJaw ? UPPER_JAW_START_ANGLE : LOWER_JAW_START_ANGLE;
		double endAngle = isUpperJaw ? UPPER_JAW_END_ANGLE : LOWER_JAW_END_ANGLE;
		int startPosition = isUpperJaw ? 0 : 16;
		double angleStep = (endAngle - startAngle) / 15;

		// 1. Check if the user clicked any Tooth
		for (int i = 0; i < 16; i++) {
			double currentAngle = startAngle + angleStep * (double) i;
			int x = (int) (centerX + JAW_RADIUS_X * Math.cos(currentAngle));
			int y = (int) (centerY + JAW_RADIUS_Y * Math.sin(currentAngle));

			if (getDistance(mouseX, mouseY, x, y) <= TOOTH_CLICK_RADIUS) {
				return Optional.of(startPosition + i);
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks whether the click hits the inner or outer part of one of the spaces on the specified jaw.
	 * <p>
	 * Each space lies between two adjacent teeth and consists of two triangles: outer (further from the
	 * center of the jaw) and inner (closer to the center). A click is a hit if its distance to the center
	 * of a triangle is not greater than {@code SPACE_RADIUS}. The outer part is checked first.
	 * <p>
	 * The method checks geometry only: it does not check whether the space is available
	 * or whether a brush is selected — this is up to the caller.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param centerX x-coordinate of the center of the jaw arc;
	 * @param centerY y-coordinate of the center of the jaw arc;
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return {@code Optional.of(}{@link SpaceHit}{@code )} with the space index (0–14 for the upper jaw,
	 * 15–29 for the lower jaw) and the clicked part if a space was hit, otherwise {@code Optional.empty()}.
	 */
	public static Optional<SpaceHit> checkSpaceClick(int mouseX, int mouseY, int centerX, int centerY, boolean isUpperJaw) {
		double startAngle = isUpperJaw ? UPPER_JAW_START_ANGLE : LOWER_JAW_START_ANGLE;
		double endAngle = isUpperJaw ? UPPER_JAW_END_ANGLE : LOWER_JAW_END_ANGLE;
		int startPosition = isUpperJaw ? 0 : 16;
		double angleStep = (endAngle - startAngle) / 15;

		for (int i = 0; i < 15; i++) {
			double currentAngle = startAngle + angleStep * i;
			double midAngle = currentAngle + angleStep / 2;

			int midX = (int) (centerX + JAW_RADIUS_X * Math.cos(midAngle));
			int midY = (int) (centerY + JAW_RADIUS_Y * Math.sin(midAngle));

			int outerX = (int) Math.round(midX - OUTER_SPACE_CENTER_Y * Math.sin(midAngle + Math.PI / 2));
			int outerY = (int) Math.round(midY + OUTER_SPACE_CENTER_Y * Math.cos(midAngle + Math.PI / 2));

			int innerX = (int) Math.round(midX - INNER_SPACE_CENTER_Y * Math.sin(midAngle + Math.PI / 2));
			int innerY = (int) Math.round(midY + INNER_SPACE_CENTER_Y * Math.cos(midAngle + Math.PI / 2));

			int spacePosition = isUpperJaw ? (startPosition + i) : (startPosition + i - 1);

			if (getDistance(mouseX, mouseY, outerX, outerY) <= SPACE_CLICK_RADIUS) {
				return Optional.of(new SpaceHit(spacePosition, false));
			} else if (getDistance(mouseX, mouseY, innerX, innerY) <= SPACE_CLICK_RADIUS) {
				return Optional.of(new SpaceHit(spacePosition, true));
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks whether the click hits one of the brush swatches.
	 * <p>
	 * Brushes are drawn as circles with the {@code BRUSH_DIAMETER} diameter, arranged vertically
	 * along the horizontal center of the panel at equal intervals. The method only finds the clicked brush;
	 * it does not change the selection — see {@link ru.yanes.state.BrushSelection#toggleSelection(Brush)}.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param width width of the brush panel;
	 * @param height height of the brush panel.
	 * @return {@code Optional.of(brush)} with the clicked brush if a swatch was hit, otherwise {@code Optional.empty()}.
	 */
	public static Optional<Brush> checkBrushClick(int mouseX, int mouseY, int width, int height) {

		for (Brush brush : Brush.values()) {

			BrushCenter brushCenter = getBrushCircleCenter(width, height, brush);

			//If x,y of mouse in brush
			if (getDistance(brushCenter.x(), brushCenter.y(), mouseX, mouseY) <= (double) BRUSH_DIAMETER / 2) {
				return Optional.of(brush);
			}

		}

		return Optional.empty();
	}

	/**
	 * Calculates the center of the circle (swatch) of the given brush on the brush panel.
	 * <p>
	 * The circles are arranged in one column along the horizontal center of the panel
	 * ({@code x = width / 2}) at equal vertical intervals of {@code height / (brushCount + 1)},
	 * in the order of the {@link Brush} constants: the first brush is at the top, the last one is at the bottom.
	 * <p>
	 * This is the single source of the swatch positions: it is used both for drawing the brushes
	 * and for hit-testing in {@link #checkBrushClick(int, int, int, int)}, so a click is always
	 * checked against the same position where the circle is drawn.
	 * <p>
	 * The interval is calculated with integer division, so for a panel lower than
	 * {@code brushCount + 1} pixels all the centers have {@code y = 0}.
	 *
	 * @param width width of the brush panel;
	 * @param height height of the brush panel;
	 * @param brush the brush whose circle center is calculated; must not be {@code null}.
	 * @return the {@link BrushCenter} with the coordinates of the circle center inside the panel.
	 * @throws NullPointerException if {@code brush} is {@code null}.
	 */
	public static BrushCenter getBrushCircleCenter(int width, int height, Brush brush) {
		int x = width / 2;
		int y = height / (Brush.values().length + 1) * (brush.ordinal() + 1);
		return new BrushCenter(x, y);
	}

	/**
	 * Calculates the Euclidean distance between two points.
	 *
	 * @param x1 is the x-coordinate of the first point;
	 * @param y1 is the y-coordinate of the first point;
	 * @param x2 is the x-coordinate of the second point;
	 * @param y2 is the y-coordinate of the second point.
	 * @return the distance between the points.
	 */
	private static double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
}
