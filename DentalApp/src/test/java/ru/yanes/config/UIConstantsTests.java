package ru.yanes.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yanes.config.DentalGeometryConstants.SPACE_OUTLINE_THICKNESS;

class UIConstantsTests {

	@Test
	@DisplayName("Dash array in empty space is not null")
	 void dashArrayInEmptySpaceIsNotNull() {
		assertNotNull(UIConstants.EMPTY_SPACE_STROKE.getDashArray());
	}

	@Test
	@DisplayName("Empty space stroke thickness equals SPACE_OUTLINE_THICKNESS")
	void emptySpaceStrokeThickAsSpaceOutline() {
		assertEquals(SPACE_OUTLINE_THICKNESS, UIConstants.EMPTY_SPACE_STROKE.getLineWidth());
	}

	@Test
	@DisplayName("Dash array in filled space is null")
	void dashArrayInFilledSpaceIsNull() {
		assertNull(UIConstants.FILLED_SPACE_STROKE.getDashArray());
	}

	@Test
	@DisplayName("Filled space stroke thickness equals SPACE_OUTLINE_THICKNESS")
	void filledSpaceStrokeThickAsSpaceOutline() {
		assertEquals(SPACE_OUTLINE_THICKNESS, UIConstants.FILLED_SPACE_STROKE.getLineWidth());
	}
}
