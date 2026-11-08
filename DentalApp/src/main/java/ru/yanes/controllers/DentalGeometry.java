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
	 * A result of clicking on a tooth space.
	 *
	 * @param position index of the space in the {@link ru.yanes.models.Mouth} spaces array (from 0 to 29);
	 * @param isInner {@code true} if the inner part of the space was clicked, {@code false} if the outer part was clicked.
	 */
	public record SpaceHit(int position, boolean isInner) {}

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
		double startAngle = isUpperJaw ? START_ANGLE_UPPER : START_ANGLE_LOWER;
		double endAngle = isUpperJaw ? END_ANGLE_UPPER : END_ANGLE_LOWER;
		int startPosition = isUpperJaw ? 0 : 16;
		double angleStep = (endAngle - startAngle) / 15;

		// 1. Check if the user clicked any Tooth
		for (int i = 0; i < 16; i++) {
			double currentAngle = startAngle + angleStep * (double) i;
			int x = (int) (centerX + RADIUS_X * Math.cos(currentAngle));
			int y = (int) (centerY + RADIUS_Y * Math.sin(currentAngle));

			if (getDistance(mouseX, mouseY, x, y) <= TOOTH_RADIUS) {
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
		double startAngle = isUpperJaw ? START_ANGLE_UPPER : START_ANGLE_LOWER;
		double endAngle = isUpperJaw ? END_ANGLE_UPPER : END_ANGLE_LOWER;
		int startPosition = isUpperJaw ? 0 : 16;
		double angleStep = (endAngle - startAngle) / 15;

		for (int i = 0; i < 15; i++) {
			double currentAngle = startAngle + angleStep * i;
			double midAngle = currentAngle + angleStep / 2;

			int midX = (int) (centerX + RADIUS_X * Math.cos(midAngle));
			int midY = (int) (centerY + RADIUS_Y * Math.sin(midAngle));

			int outerX = (int) Math.round(midX - LOCAL_OUTER_Y * Math.sin(midAngle + Math.PI / 2));
			int outerY = (int) Math.round(midY + LOCAL_OUTER_Y * Math.cos(midAngle + Math.PI / 2));

			int innerX = (int) Math.round(midX - LOCAL_INNER_Y * Math.sin(midAngle + Math.PI / 2));
			int innerY = (int) Math.round(midY + LOCAL_INNER_Y * Math.cos(midAngle + Math.PI / 2));

			int spacePosition = isUpperJaw ? (startPosition + i) : (startPosition + i - 1);

			if (getDistance(mouseX, mouseY, outerX, outerY) <= SPACE_RADIUS) {
				return Optional.of(new SpaceHit(spacePosition, false));
			} else if (getDistance(mouseX, mouseY, innerX, innerY) <= SPACE_RADIUS) {
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

		int brushCount = Brush.values().length;
		int centerX = width / 2;

		for (int i = 1; i <= brushCount; i++) {

			int brushPos = height / (brushCount + 1) * i;

			//If x,y of mouse in brush
			if (getDistance(centerX, brushPos, mouseX, mouseY) <= (double) BRUSH_DIAMETER / 2) {
				return Optional.of(Brush.values()[i - 1]);
			}

		}

		return Optional.empty();
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
