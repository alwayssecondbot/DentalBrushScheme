package ru.yanes.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Brush}.
 * <p>
 * Checks the set of declared brushes, their diameters and colors.
 */
class BrushTests {

	@Test
	@DisplayName("Enum declares exactly 7 brush sizes, from XXS to XXL")
	void hasSevenValues() {
		assertEquals(7, Brush.values().length);
	}

	@Test
	@DisplayName("XXS has the smallest diameter (0.4 mm) and is pink")
	void xxsProperties() {
		assertEquals(0.4F, Brush.XXS.getDiameter());
		assertEquals(new Color(237, 60, 202), Brush.XXS.getColor());
	}

	@Test
	@DisplayName("XXL has the largest diameter (1.1 mm) and is purple")
	void xxlProperties() {
		assertEquals(1.1F, Brush.XXL.getDiameter());
		assertEquals(new Color(128, 0, 255), Brush.XXL.getColor());
	}

	@ParameterizedTest(name = "{0} has a non-null color")
	@EnumSource(Brush.class)
	void everyBrushHasAColor(Brush brush) {
		assertNotNull(brush.getColor());
	}

	@Test
	@DisplayName("Diameters strictly increase from XXS to XXL")
	void diametersAreOrdered() {
		Brush[] orderedBySize = {Brush.XXS, Brush.XS, Brush.S, Brush.M, Brush.L, Brush.XL, Brush.XXL};
		for (int i = 1; i < orderedBySize.length; i++) {
			assertTrue(orderedBySize[i].getDiameter() > orderedBySize[i - 1].getDiameter(),
					orderedBySize[i] + " should be larger than " + orderedBySize[i - 1]);
		}
	}

	@Test
	@DisplayName("valueOf() resolves brushes by name")
	void valueOfResolvesByName() {
		assertEquals(Brush.M, Brush.valueOf("M"));
	}
}