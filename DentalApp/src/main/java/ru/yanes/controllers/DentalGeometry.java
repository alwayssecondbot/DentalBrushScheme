package ru.yanes.controllers;

import ru.yanes.models.Brush;

import static ru.yanes.config.DentalGeometryConstants.*;

import java.util.Optional;

public class DentalGeometry {

	/**
	 * A result of clicking on tooth space
	 * @param position FDI-position of space;
	 * @param isInner {@code true} if space is inner, {@code false} if space is outer.
	 */
	public record SpaceHit(int position, boolean isInner) {}

	/**
	 * Checks whether the click hits teeth on the specified jaw.
	 * If the click hits a tooth, returns FDI of this tooth or nothing.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param centerX x-coordinate of the center of the jaw arc;
	 * @param centerY y-coordinate of the center of the jaw arc;
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return {@code Optional.of(toothFDI)} if the click was processed (tooth or gap), otherwise {@code Optional.empty()}.
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
	 * Checks whether the click hits space on the specified jaw.
	 * If the click hits an available space and a brush is selected,
	 * returns space position or nothing.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param centerX x-coordinate of the center of the jaw arc;
	 * @param centerY y-coordinate of the center of the jaw arc;
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return Optional.of({@link SpaceHit}) if the click was processed (tooth or gap), otherwise {@code Optional.empty()}.
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

			// Повернутые координаты внешней зоны
			int outerX = (int) Math.round(midX + BASIC_SPACE_X[0] * Math.cos(midAngle + Math.PI / 2) - LOCAL_OUTER_Y * Math.sin(midAngle + Math.PI / 2));
			int outerY = (int) Math.round(midY + BASIC_SPACE_X[0] * Math.cos(midAngle + Math.PI / 2) + LOCAL_OUTER_Y * Math.cos(midAngle + Math.PI / 2));

			// Повернутые координаты внутренней зоны
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
	 * Checks whether the click hits one of the brushes. If a hit is detected,
	 * toggles the state of the selected brush ({@code selectedBrush}): if the brush was already
	 * selected, deselects it; otherwise, selects it.
	 *
	 * @param mouseX x-coordinate of the click;
	 * @param mouseY y-coordinate of the click;
	 * @param width width of the panel center;
	 * @param height height of the panel center.
	 * @return {@code Optional.of(Brush)} with brush inside if the click was processed (hit a brush), otherwise {@code Optional.empty()}.
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
