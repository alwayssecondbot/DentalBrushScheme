package ru.yanes.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static ru.yanes.config.DentalGeometryConstants.INNER_SPACE_CENTER_Y;
import static ru.yanes.config.DentalGeometryConstants.OUTER_SPACE_CENTER_Y;

/**
 * Unit tests for {@link DentalGeometryConstants}.
 * <p>
 * Plain numeric constants are not tested: a test like "the value is 45" only repeats the constant.
 * Instead, the tests check the parts that contain logic or relations between constants:
 * <ul>
 *     <li>the methods returning the space triangle vertices return defensive copies,
 *     so changing a returned array does not change the shared data;</li>
 *     <li>the outer triangle points away from the center of the jaw and the inner one points towards it;</li>
 *     <li>the centers of the triangles used for hit-testing match the average of their vertices.</li>
 * </ul>
 */
class DentalGeometryConstantsTests {

	/**
	 * Allowed difference when comparing {@code double} values.
	 * Coordinates are measured in pixels, so such a difference is negligible, but any real error is caught.
	 */
	private static final double DELTA = 1e-9;

	/**
	 * Converts an {@code int[]} to a list, so AssertJ collection checks (like {@code allSatisfy})
	 * can be used: they are not available for primitive arrays.
	 *
	 * @param array the array to convert.
	 * @return a list with the same values in the same order.
	 */
	private static List<Integer> toList(int[] array) {
		return Arrays.stream(array).boxed().toList();
	}

	@Test
	@DisplayName("Changing an array returned by spaceTriangleX() does not change the next result")
	void spaceTriangleXReturnsCopy() {
		int[] original = DentalGeometryConstants.spaceTriangleX();
		int[] returned = DentalGeometryConstants.spaceTriangleX();

		returned[0] = returned[0] + 1;

		assertAll(
				() -> assertArrayEquals(original, DentalGeometryConstants.spaceTriangleX()),
				() -> assertNotSame(returned, DentalGeometryConstants.spaceTriangleX())
		);
	}

	@Test
	@DisplayName("Changing an array returned by outerSpaceTriangleY() does not change the next result")
	void outerSpaceTriangleYReturnsCopy() {
		int[] original = DentalGeometryConstants.outerSpaceTriangleY();
		int[] returned = DentalGeometryConstants.outerSpaceTriangleY();

		returned[0] = returned[0] + 1;

		assertAll(
				() -> assertArrayEquals(original, DentalGeometryConstants.outerSpaceTriangleY()),
				() -> assertNotSame(returned, DentalGeometryConstants.outerSpaceTriangleY())
		);
	}

	@Test
	@DisplayName("Changing an array returned by innerSpaceTriangleY() does not change the next result")
	void innerSpaceTriangleYReturnsCopy() {
		int[] original = DentalGeometryConstants.innerSpaceTriangleY();
		int[] returned = DentalGeometryConstants.innerSpaceTriangleY();

		returned[0] = returned[0] + 1;

		assertAll(
				() -> assertArrayEquals(original, DentalGeometryConstants.innerSpaceTriangleY()),
				() -> assertNotSame(returned, DentalGeometryConstants.innerSpaceTriangleY())
		);
	}

	@Test
	@DisplayName("Each y of the inner space triangle is >= 0 (points towards the center of the jaw)")
	void eachYOfInnerSpaceIsNotNegative() {
		assertThat(toList(DentalGeometryConstants.innerSpaceTriangleY())).allSatisfy(y -> assertThat(y).isGreaterThanOrEqualTo(0));
	}

	@Test
	@DisplayName("Each y of the outer space triangle is <= 0 (points away from the center of the jaw)")
	void eachYOfOuterSpaceIsNotPositive() {
		assertThat(toList(DentalGeometryConstants.outerSpaceTriangleY())).allSatisfy(y -> assertThat(y).isLessThanOrEqualTo(0));
	}

	@Test
	@DisplayName("Outer space center y is equal to the average of the outer space triangle y")
	void outerSpaceCenterYEqualsAverageTriangleY() {
		double average = Arrays.stream(DentalGeometryConstants.outerSpaceTriangleY()).average().orElseThrow();
		assertEquals(average, OUTER_SPACE_CENTER_Y, DELTA);
	}

	@Test
	@DisplayName("Inner space center y is equal to the average of the inner space triangle y")
	void innerSpaceCenterYEqualsAverageTriangleY() {
		double average = Arrays.stream(DentalGeometryConstants.innerSpaceTriangleY()).average().orElseThrow();
		assertEquals(average, INNER_SPACE_CENTER_Y, DELTA);
	}
}
