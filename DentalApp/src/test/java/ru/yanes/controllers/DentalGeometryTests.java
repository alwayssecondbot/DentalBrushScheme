package ru.yanes.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.yanes.models.Brush;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.yanes.config.DentalGeometryConstants.*;

/**
 * Unit tests for {@link DentalGeometry}.
 * <p>
 * The methods under test are pure geometry functions: given a click point and a jaw
 * arc center, they decide whether the click hit a tooth, a space (inner/outer) or a
 * brush swatch. To click exactly on target, these tests recompute the same pixel
 * positions the production code computes (same formulas from
 * {@code DentalGeometryConstants}), then verify hit/miss behavior and the returned
 * indices/records, rather than guessing coordinates.
 * <p>
 * Note: because the helpers repeat the production formulas, these tests protect against
 * accidental changes of the hit-testing logic, but do not prove that the formulas themselves are correct.
 */
class DentalGeometryTests {

	/** Large enough panel so that brush swatches (BRUSH_DIAMETER = 70) don't overlap: */
	/** spacing = height / (brushCount + 1) = 800 / 8 = 100. */
	private static final int WIDTH = 400;
	private static final int HEIGHT = 800;

	/** X-coordinate of the jaw arc center used in all tests. */
	private static final int CENTER_X = 0;
	/** Y-coordinate of the jaw arc center used in all tests. */
	private static final int CENTER_Y = 0;

	// ---- helpers that mirror DentalGeometry's private formulas ----

	/**
	 * Angle between two adjacent teeth on the given jaw.
	 *
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return the angle step in radians.
	 */
	private static double angleStep(boolean isUpperJaw) {
		double startAngle = isUpperJaw ? START_ANGLE_UPPER : START_ANGLE_LOWER;
		double endAngle = isUpperJaw ? END_ANGLE_UPPER : END_ANGLE_LOWER;
		return (endAngle - startAngle) / 15;
	}

	/**
	 * Start angle of the arc of the given jaw.
	 *
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw.
	 * @return the start angle in radians.
	 */
	private static double startAngle(boolean isUpperJaw) {
		return isUpperJaw ? START_ANGLE_UPPER : START_ANGLE_LOWER;
	}

	/** Pixel of tooth #i (0-15) on the given jaw, as computed by {@code checkToothClick}. */
	private static int[] toothPixel(boolean isUpperJaw, int i) {
		double angle = startAngle(isUpperJaw) + angleStep(isUpperJaw) * i;
		int x = (int) (CENTER_X + RADIUS_X * Math.cos(angle));
		int y = (int) (CENTER_Y + RADIUS_Y * Math.sin(angle));
		return new int[]{x, y};
	}

	/** Mid-angle between tooth i and tooth i+1, as computed by {@code checkSpaceClick}. */
	private static double spaceMidAngle(boolean isUpperJaw, int i) {
		double currentAngle = startAngle(isUpperJaw) + angleStep(isUpperJaw) * i;
		return currentAngle + angleStep(isUpperJaw) / 2;
	}

	/**
	 * Pixel of the center of a space triangle, rotated the same way as in {@code checkSpaceClick}.
	 *
	 * @param isUpperJaw {@code true} for the upper jaw, {@code false} for the lower jaw;
	 * @param i index of the space on the jaw (0-14);
	 * @param localOffset local y-coordinate of the triangle center ({@code LOCAL_OUTER_Y} or {@code LOCAL_INNER_Y}).
	 * @return the pixel as {@code [x, y]}.
	 */
	private static int[] spacePixel(boolean isUpperJaw, int i, double localOffset) {
		double midAngle = spaceMidAngle(isUpperJaw, i);
		int midX = (int) (CENTER_X + RADIUS_X * Math.cos(midAngle));
		int midY = (int) (CENTER_Y + RADIUS_Y * Math.sin(midAngle));
		int x = (int) Math.round(midX - localOffset * Math.sin(midAngle + Math.PI / 2));
		int y = (int) Math.round(midY + localOffset * Math.cos(midAngle + Math.PI / 2));
		return new int[]{x, y};
	}

	/** Pixel of the center of the outer triangle of space #i (0-14) on the given jaw. */
	private static int[] outerSpacePixel(boolean isUpperJaw, int i) {
		return spacePixel(isUpperJaw, i, LOCAL_OUTER_Y);
	}

	/** Pixel of the center of the inner triangle of space #i (0-14) on the given jaw. */
	private static int[] innerSpacePixel(boolean isUpperJaw, int i) {
		return spacePixel(isUpperJaw, i, LOCAL_INNER_Y);
	}

	// ============================================================
	// checkToothClick
	// ============================================================

	@Nested
	@DisplayName("checkToothClick()")
	class CheckToothClick {

		@Test
		@DisplayName("Click at the exact tooth center on the upper jaw returns its index")
		void hitsFirstUpperTooth() {
			int[] p = toothPixel(true, 0);
			assertEquals(Optional.of(0),
					DentalGeometry.checkToothClick(p[0], p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Click at the last upper-jaw tooth (index 15) returns position 15")
		void hitsLastUpperTooth() {
			int[] p = toothPixel(true, 15);
			assertEquals(Optional.of(15),
					DentalGeometry.checkToothClick(p[0], p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Lower jaw positions are offset by 16")
		void lowerJawPositionsAreOffset() {
			int[] first = toothPixel(false, 0);
			int[] last = toothPixel(false, 15);

			assertEquals(Optional.of(16),
					DentalGeometry.checkToothClick(first[0], first[1], CENTER_X, CENTER_Y, false));
			assertEquals(Optional.of(31),
					DentalGeometry.checkToothClick(last[0], last[1], CENTER_X, CENTER_Y, false));
		}

		@Test
		@DisplayName("A click exactly TOOTH_RADIUS away from the center still counts as a hit")
		void boundaryDistanceIsAHit() {
			int[] p = toothPixel(true, 7);
			assertEquals(Optional.of(7),
					DentalGeometry.checkToothClick(p[0] + TOOTH_RADIUS, p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("A click just past TOOTH_RADIUS is a miss")
		void justPastBoundaryIsAMiss() {
			int[] p = toothPixel(true, 7);
			assertEquals(Optional.empty(),
					DentalGeometry.checkToothClick(p[0] + TOOTH_RADIUS + 1, p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("A click far from any tooth returns empty")
		void farClickIsAMiss() {
			assertEquals(Optional.empty(),
					DentalGeometry.checkToothClick(100_000, 100_000, CENTER_X, CENTER_Y, true));
			assertEquals(Optional.empty(),
					DentalGeometry.checkToothClick(100_000, 100_000, CENTER_X, CENTER_Y, false));
		}
	}

	// ============================================================
	// checkSpaceClick
	// ============================================================

	@Nested
	@DisplayName("checkSpaceClick()")
	class CheckSpaceClick {

		@Test
		@DisplayName("Clicking the outer zone of the first upper space returns SpaceHit(0, isInner=false)")
		void hitsOuterZoneOfFirstUpperSpace() {
			int[] p = outerSpacePixel(true, 0);
			assertEquals(Optional.of(new SpaceHit(0, false)),
					DentalGeometry.checkSpaceClick(p[0], p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Clicking the inner zone of the first upper space returns SpaceHit(0, isInner=true)")
		void hitsInnerZoneOfFirstUpperSpace() {
			int[] p = innerSpacePixel(true, 0);
			assertEquals(Optional.of(new SpaceHit(0, true)),
					DentalGeometry.checkSpaceClick(p[0], p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Last upper space (index 14) resolves to position 14")
		void lastUpperSpaceResolvesCorrectly() {
			int[] p = outerSpacePixel(true, 14);
			assertEquals(Optional.of(new SpaceHit(14, false)),
					DentalGeometry.checkSpaceClick(p[0], p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Lower-jaw space positions are shifted (position = 15 + i)")
		void lowerJawSpacePositionsAreShifted() {
			int[] first = outerSpacePixel(false, 0);
			int[] last = outerSpacePixel(false, 14);

			assertEquals(Optional.of(new SpaceHit(15, false)),
					DentalGeometry.checkSpaceClick(first[0], first[1], CENTER_X, CENTER_Y, false));
			assertEquals(Optional.of(new SpaceHit(29, false)),
					DentalGeometry.checkSpaceClick(last[0], last[1], CENTER_X, CENTER_Y, false));
		}

		@Test
		@DisplayName("A click exactly SPACE_RADIUS away from the outer zone still counts as a hit")
		void boundaryDistanceIsAHit() {
			int[] p = outerSpacePixel(true, 5);
			assertEquals(Optional.of(new SpaceHit(5, false)),
					DentalGeometry.checkSpaceClick(p[0] + SPACE_RADIUS, p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("A click just past SPACE_RADIUS is a miss")
		void justPastBoundaryIsAMiss() {
			int[] p = outerSpacePixel(true, 5);
			assertEquals(Optional.empty(),
					DentalGeometry.checkSpaceClick(p[0] + SPACE_RADIUS + 1, p[1], CENTER_X, CENTER_Y, true));
		}

		@Test
		@DisplayName("Outer zone is checked before inner: a click near the outer zone is never reported as inner")
		void outerZoneTakesPriority() {
			int[] p = outerSpacePixel(true, 3);
			Optional<SpaceHit> hit =
					DentalGeometry.checkSpaceClick(p[0], p[1], CENTER_X, CENTER_Y, true);
			assertTrue(hit.isPresent());
			assertFalse(hit.get().isInner());
		}

		@Test
		@DisplayName("A click far from any space returns empty")
		void farClickIsAMiss() {
			assertEquals(Optional.empty(),
					DentalGeometry.checkSpaceClick(100_000, 100_000, CENTER_X, CENTER_Y, true));
			assertEquals(Optional.empty(),
					DentalGeometry.checkSpaceClick(100_000, 100_000, CENTER_X, CENTER_Y, false));
		}
	}

	// ============================================================
	// checkBrushClick
	// ============================================================

	@Nested
	@DisplayName("checkBrushClick()")
	class CheckBrushClick {

		@Test
		@DisplayName("Clicking the exact center of the first brush swatch returns that brush")
		void hitsFirstBrush() {
			Brush brush = Brush.values()[0];
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			assertEquals(Optional.of(brush),
					DentalGeometry.checkBrushClick(brushCenter.x(), brushCenter.y(), WIDTH, HEIGHT));
		}

		@Test
		@DisplayName("Clicking the exact center of the last brush swatch returns that brush")
		void hitsLastBrush() {
			int brushCount = Brush.values().length;
			Brush brush = Brush.values()[brushCount - 1];
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			assertEquals(Optional.of(brush),
					DentalGeometry.checkBrushClick(brushCenter.x(), brushCenter.y(), WIDTH, HEIGHT));
		}

		@Test
		@DisplayName("A click exactly BRUSH_DIAMETER/2 away from a swatch center still counts as a hit")
		void boundaryDistanceIsAHit() {
			Brush brush = Brush.L;
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			assertEquals(Optional.of(brush),
					DentalGeometry.checkBrushClick(brushCenter.x(), brushCenter.y() + BRUSH_DIAMETER / 2, WIDTH, HEIGHT));
		}

		@Test
		@DisplayName("A click just past BRUSH_DIAMETER/2 is a miss")
		void justPastBoundaryIsAMiss() {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.L);
			assertEquals(Optional.empty(),
					DentalGeometry.checkBrushClick(brushCenter.x(), brushCenter.y() + BRUSH_DIAMETER / 2 + 1, WIDTH, HEIGHT));
		}

		@Test
		@DisplayName("A click far from every swatch returns empty")
		void farClickIsAMiss() {
			assertEquals(Optional.empty(),
					DentalGeometry.checkBrushClick(100_000, 100_000, WIDTH, HEIGHT));
		}
	}

	@Nested
	@DisplayName("getBrushCircleCenter()")
	class GetBrushCircleCenter {

		@Test
		@DisplayName("Anchor points of first and last circles")
		void firstAndLastCircles() {
			BrushCenter firstCircleExpected = new BrushCenter(200, 100);
			BrushCenter firstCircleActual = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[0]);

			Brush lastBrush = Brush.values()[Brush.values().length - 1];
			BrushCenter lastCircleExpected = new BrushCenter(200, 700);
			BrushCenter lastCircleActual = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, lastBrush);

			assertAll(
					() -> assertEquals(firstCircleExpected, firstCircleActual),
					() -> assertEquals(lastCircleExpected, lastCircleActual)
			);
		}

		@ParameterizedTest(name = "Brush {0} circle has the same x as other")
		@EnumSource(Brush.class)
		void eachCircleHasTheSameX(Brush brush) {
			assertEquals(WIDTH / 2, DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush).x());
		}

		@Test
		@DisplayName("Y scale with bigger brush")
		void yScaleWithBrush() {
			List<Integer> list = Arrays.stream(Brush.values()).map(brush -> DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush).y())
					.toList();

			assertThat(list).isSorted().doesNotHaveDuplicates();

		}

		@Test
		@DisplayName("Distance between brushes is equal 100")
		void distanceBetweenBrushes() {
			List<Integer> list = Arrays.stream(Brush.values()).map(brush -> DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush).y())
					.toList();
			List<Integer> gaps = IntStream.range(0, list.size() - 1).map(i -> list.get(i + 1) - list.get(i)).boxed().toList();

			assertThat(gaps).hasSize(list.size() - 1).allSatisfy(gap -> assertThat(gap).isEqualTo(100));
		}

		@ParameterizedTest(name = "Brush {0} circle inside panel")
		@EnumSource(Brush.class)
		void brushInsidePanel(Brush brush) {
			int y = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush).y();
			assertAll(
					() -> assertTrue(() -> y - BRUSH_DIAMETER / 2 >= 0),
					() -> assertTrue(() -> y + BRUSH_DIAMETER / 2 <= HEIGHT)
			);
		}

		@Test
		@DisplayName("Brush circles don't cover each other")
		void brushCirclesDontCoverEachOther() {
			List<Integer> list = Arrays.stream(Brush.values()).map(brush -> DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush).y())
					.toList();

			List<Integer> gaps = IntStream.range(0, list.size() - 1).map(i -> list.get(i + 1) - list.get(i)).boxed().toList();

			assertThat(gaps).hasSize(list.size() - 1).allSatisfy(gap -> assertThat(gap).isGreaterThanOrEqualTo(BRUSH_DIAMETER));
		}

		@ParameterizedTest(name = "Brush {0} returns when it's clicked")
		@EnumSource(Brush.class)
		void brushReturnsWhenItIsClicked(Brush brush) {
			BrushCenter brushExpected = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			assertEquals(Optional.of(brush), DentalGeometry.checkBrushClick(brushExpected.x(), brushExpected.y(), WIDTH, HEIGHT));
		}
	}
}