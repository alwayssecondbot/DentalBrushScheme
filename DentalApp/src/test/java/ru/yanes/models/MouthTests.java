package ru.yanes.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Mouth}.
 */
class MouthTests {

	/** Traversal order used by {@link Mouth}: upper jaw left→right, then lower jaw right→left. */
	private static final int[] EXPECTED_POSITIONS = {
			28, 27, 26, 25, 24, 23, 22, 21, 11, 12, 13, 14, 15, 16, 17, 18,
			48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38
	};

	private Mouth mouth;

	@BeforeEach
	void setUp() {
		mouth = new Mouth();
	}

	@Test
	@DisplayName("Creates exactly 32 teeth")
	void createsThirtyTwoTeeth() {
		assertEquals(32, mouth.getTeeth().length);
	}

	@Test
	@DisplayName("Creates exactly 30 spaces")
	void createsThirtySpaces() {
		assertEquals(30, mouth.getSpaces().length);
	}

	@Test
	@DisplayName("Teeth are created in the expected FDI traversal order")
	void teethAreInExpectedOrder() {
		for (int i = 0; i < EXPECTED_POSITIONS.length; i++) {
			assertEquals(EXPECTED_POSITIONS[i], mouth.getTeeth()[i].getPosition(),
					"Unexpected position at index " + i);
		}
	}

	@Nested
	@DisplayName("Initial tooth availability")
	class InitialAvailability {

		@Test
		@DisplayName("Wisdom teeth (18, 28, 38, 48) are unavailable")
		void wisdomTeethAreUnavailable() {
			for (Tooth tooth : mouth.getTeeth()) {
				int p = tooth.getPosition();
				boolean isWisdom = (p == 18 || p == 28 || p == 38 || p == 48);
				assertEquals(!isWisdom, tooth.isAvailable(),
						"Tooth " + p + " availability is wrong");
			}
		}
	}

	@Nested
	@DisplayName("Space connectivity")
	class SpaceConnectivity {

		@Test
		@DisplayName("Last space of the upper jaw connects teeth 17 and 18")
		void lastUpperJawSpace() {
			Space space = mouth.getSpace(14);
			assertEquals(17, space.getLeftTooth().getPosition());
			assertEquals(18, space.getRightTooth().getPosition());
		}

		@Test
		@DisplayName("First space of the lower jaw skips the 18-48 jump and connects 48 and 47")
		void firstLowerJawSpaceSkipsJawGap() {
			Space space = mouth.getSpace(15);
			assertEquals(48, space.getLeftTooth().getPosition());
			assertEquals(47, space.getRightTooth().getPosition());
		}

		@Test
		@DisplayName("Last space connects teeth 37 and 38")
		void lastSpace() {
			Space space = mouth.getSpace(29);
			assertEquals(37, space.getLeftTooth().getPosition());
			assertEquals(38, space.getRightTooth().getPosition());
		}

		@Test
		@DisplayName("First space connects teeth 28 and 27")
		void firstSpace() {
			Space space = mouth.getSpace(0);
			assertEquals(28, space.getLeftTooth().getPosition());
			assertEquals(27, space.getRightTooth().getPosition());
		}

		@Test
		@DisplayName("Every space's stored position matches its index")
		void spacePositionsMatchIndex() {
			for (int i = 0; i < mouth.getSpaces().length; i++) {
				assertEquals(i, mouth.getSpace(i).getPosition());
			}
		}
	}

	@Nested
	@DisplayName("getTooth()")
	class GetTooth {

		@Test
		@DisplayName("Returns the tooth at a valid index")
		void returnsToothAtValidIndex() {
			assertEquals(28, mouth.getTooth(0).getPosition());
			assertEquals(38, mouth.getTooth(31).getPosition());
		}

		@Test
		@DisplayName("Throws for a negative index")
		void throwsForNegativeIndex() {
			assertThrows(IllegalArgumentException.class, () -> mouth.getTooth(-1));
		}

		@Test
		@DisplayName("Throws for an index past the end")
		void throwsForTooLargeIndex() {
			assertThrows(IllegalArgumentException.class, () -> mouth.getTooth(32));
		}
	}

	@Nested
	@DisplayName("getSpace()")
	class GetSpace {

		@Test
		@DisplayName("Returns the space at a valid index")
		void returnsSpaceAtValidIndex() {
			assertEquals(0, mouth.getSpace(0).getPosition());
			assertEquals(29, mouth.getSpace(29).getPosition());
		}

		@Test
		@DisplayName("Throws for a negative index")
		void throwsForNegativeIndex() {
			assertThrows(IllegalArgumentException.class, () -> mouth.getSpace(-1));
		}

		@Test
		@DisplayName("Throws for an index past the end")
		void throwsForTooLargeIndex() {
			assertThrows(IllegalArgumentException.class, () -> mouth.getSpace(30));
		}
	}

	@Nested
	@DisplayName("resetAll()")
	class ResetAll {

		@Test
		@DisplayName("Makes every tooth available again")
		void makesAllTeethAvailable() {
			mouth.resetAll();
			for (Tooth tooth : mouth.getTeeth()) {
				assertTrue(tooth.isAvailable());
			}
		}

		@Test
		@DisplayName("Clears inner and outer brushes from every space")
		void clearsAllBrushes() {
			// Install a brush somewhere in a space that starts out available.
			mouth.getSpace(0).setInnerBrush(Brush.M);

			mouth.resetAll();

			for (Space space : mouth.getSpaces()) {
				assertNull(space.getInnerBrush());
				assertNull(space.getOuterBrush());
			}
		}

		@Test
		@DisplayName("Also restores spaces that were unavailable before reset")
		void restoresPreviouslyUnavailableSpace() {
			// Space 14 connects teeth 17 (available) and 18 (wisdom, unavailable) -> available overall.
			// Space involving two wisdom teeth doesn't exist directly, but we can force
			// unavailability by disabling both teeth of a regular space first.
			Space space = mouth.getSpace(0); // teeth 28 (wisdom) - 27 (regular)
			mouth.getTooth(1).setAvailable(false); // make tooth 27 unavailable too
			assertFalse(space.isAvailable());

			mouth.resetAll();

			assertTrue(space.isAvailable());
		}
	}
}