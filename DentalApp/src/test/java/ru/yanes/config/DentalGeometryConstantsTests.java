package ru.yanes.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yanes.config.DentalGeometryConstants.INNER_SPACE_CENTER_Y;
import static ru.yanes.config.DentalGeometryConstants.OUTER_SPACE_CENTER_Y;

class DentalGeometryConstantsTests {

	@Test
	@DisplayName("Get copy of array when ask for space triangle x array")
	void getCopyWhenAskForSpaceTriangleX() {
		int[] spaceTriangleX = DentalGeometryConstants.spaceTriangleX();
		spaceTriangleX[2] = spaceTriangleX[2] + 1;

		assertNotSame(spaceTriangleX, DentalGeometryConstants.spaceTriangleX());
	}

	@Test
	@DisplayName("Get copy of array when ask for outer space triangle y array")
	void getCopyWhenAskForOuterSpaceTriangleY() {
		int[] outerSpaceTriangleY = DentalGeometryConstants.outerSpaceTriangleY();
		outerSpaceTriangleY[2] = outerSpaceTriangleY[2] + 1;

		assertNotSame(outerSpaceTriangleY, DentalGeometryConstants.outerSpaceTriangleY());
	}

	@Test
	@DisplayName("Get copy of array when ask for outer space triangle y array")
	void getCopyWhenAskForInnerSpaceTriangleY() {
		int[] innerSpaceTriangleY = DentalGeometryConstants.innerSpaceTriangleY();
		innerSpaceTriangleY[2] = innerSpaceTriangleY[2] + 1;

		assertNotSame(innerSpaceTriangleY, DentalGeometryConstants.outerSpaceTriangleY());
	}

	@Test
	@DisplayName("Each y of inner space >= 0")
	void eachYOfInnerSpaceMoreThanOrEqualZero() {
		for (int y : DentalGeometryConstants.innerSpaceTriangleY()) {
			assertTrue(y >= 0);
		}
	}

	@Test
	@DisplayName("Each y of outer space <= 0")
	void eachYOfOuterSpaceMoreThanOrEqualZero() {
		for (int y : DentalGeometryConstants.outerSpaceTriangleY()) {
			assertTrue(y <= 0);
		}
	}

	@Test
	@DisplayName("Outer space center y is equal to average outer space triangle y")
	void outerSpaceCenterYEqualsAverageTriangleY() {
		double average = Arrays.stream(DentalGeometryConstants.outerSpaceTriangleY()).average().getAsDouble();
		assertTrue(average >= OUTER_SPACE_CENTER_Y);
	}

	@Test
	@DisplayName("Inner space center y is equal to average outer space triangle y")
	void innerSpaceCenterYEqualsAverageTriangleY() {
		double average = Arrays.stream(DentalGeometryConstants.innerSpaceTriangleY()).average().getAsDouble();
		assertTrue(average >= INNER_SPACE_CENTER_Y);
	}
}
