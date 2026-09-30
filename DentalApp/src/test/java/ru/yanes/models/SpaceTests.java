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
 * Unit tests for {@link Space}.
 * <p>
 * Placed in the same package ({@code ru.yanes.models}) because {@link Tooth}'s
 * constructor is package-private and is used here to build test fixtures.
 */
class SpaceTests {

	private Tooth leftTooth;
	private Tooth rightTooth;
	private Space space;

	@BeforeEach
	void setUp() {
		// Regular (non-wisdom) teeth, available by default.
		leftTooth = new Tooth(11);
		rightTooth = new Tooth(12);
		space = new Space(leftTooth, rightTooth, 0);
	}

	@Test
	@DisplayName("New space has no brushes installed")
	void newSpaceIsEmpty() {
		assertTrue(space.isInnerEmpty());
		assertTrue(space.isOuterEmpty());
		assertNull(space.getInnerBrush());
		assertNull(space.getOuterBrush());
	}

	@Test
	@DisplayName("Constructor stores left tooth, right tooth and position")
	void constructorStoresFields() {
		assertEquals(leftTooth, space.getLeftTooth());
		assertEquals(rightTooth, space.getRightTooth());
		assertEquals(0, space.getPosition());
	}

	@Nested
	@DisplayName("isAvailable()")
	class Availability {

		@Test
		@DisplayName("Available when both teeth are available")
		void bothTeethAvailable() {
			assertTrue(space.isAvailable());
		}

		@Test
		@DisplayName("Available when only the left tooth is available")
		void onlyLeftAvailable() {
			rightTooth.setAvailable(false);
			assertTrue(space.isAvailable());
		}

		@Test
		@DisplayName("Available when only the right tooth is available")
		void onlyRightAvailable() {
			leftTooth.setAvailable(false);
			assertTrue(space.isAvailable());
		}

		@Test
		@DisplayName("Unavailable when both teeth are unavailable")
		void bothTeethUnavailable() {
			leftTooth.setAvailable(false);
			rightTooth.setAvailable(false);
			assertFalse(space.isAvailable());
		}
	}

	@Nested
	@DisplayName("setInnerBrush()")
	class InnerBrush {

		@Test
		@DisplayName("Installs the brush when space is available and outer is empty")
		void installsBrush() {
			space.setInnerBrush(Brush.M);
			assertEquals(Brush.M, space.getInnerBrush());
			assertFalse(space.isInnerEmpty());
		}

		@Test
		@DisplayName("Setting the same brush again removes it (toggle behavior)")
		void togglesOffSameBrush() {
			space.setInnerBrush(Brush.M);
			space.setInnerBrush(Brush.M);
			assertNull(space.getInnerBrush());
			assertTrue(space.isInnerEmpty());
		}

		@Test
		@DisplayName("Setting a different brush replaces the current one")
		void replacesDifferentBrush() {
			space.setInnerBrush(Brush.M);
			space.setInnerBrush(Brush.L);
			assertEquals(Brush.L, space.getInnerBrush());
		}

		@Test
		@DisplayName("Throws when the outer brush is already installed")
		void throwsWhenOuterOccupied() {
			space.setOuterBrush(Brush.S);
			assertThrows(IllegalStateException.class, () -> space.setInnerBrush(Brush.M));
		}

		@Test
		@DisplayName("Throws when the space is unavailable")
		void throwsWhenSpaceUnavailable() {
			leftTooth.setAvailable(false);
			rightTooth.setAvailable(false);
			assertThrows(IllegalStateException.class, () -> space.setInnerBrush(Brush.M));
		}
	}

	@Nested
	@DisplayName("setOuterBrush()")
	class OuterBrush {

		@Test
		@DisplayName("Installs the brush when space is available and inner is empty")
		void installsBrush() {
			space.setOuterBrush(Brush.XL);
			assertEquals(Brush.XL, space.getOuterBrush());
			assertFalse(space.isOuterEmpty());
		}

		@Test
		@DisplayName("Setting the same brush again removes it (toggle behavior)")
		void togglesOffSameBrush() {
			space.setOuterBrush(Brush.XL);
			space.setOuterBrush(Brush.XL);
			assertNull(space.getOuterBrush());
			assertTrue(space.isOuterEmpty());
		}

		@Test
		@DisplayName("Throws when the inner brush is already installed")
		void throwsWhenInnerOccupied() {
			space.setInnerBrush(Brush.S);
			assertThrows(IllegalStateException.class, () -> space.setOuterBrush(Brush.M));
		}

		@Test
		@DisplayName("Throws when the space is unavailable")
		void throwsWhenSpaceUnavailable() {
			leftTooth.setAvailable(false);
			rightTooth.setAvailable(false);
			assertThrows(IllegalStateException.class, () -> space.setOuterBrush(Brush.M));
		}
	}

	@Nested
	@DisplayName("emptyInnerBrush() / emptyOuterBrush()")
	class ClearingBrushes {

		@Test
		@DisplayName("emptyInnerBrush() clears an installed inner brush")
		void clearsInnerBrush() {
			space.setInnerBrush(Brush.M);
			space.emptyInnerBrush();
			assertTrue(space.isInnerEmpty());
		}

		@Test
		@DisplayName("emptyOuterBrush() clears an installed outer brush")
		void clearsOuterBrush() {
			space.setOuterBrush(Brush.M);
			space.emptyOuterBrush();
			assertTrue(space.isOuterEmpty());
		}

		@Test
		@DisplayName("emptyInnerBrush() throws when space is unavailable")
		void emptyInnerThrowsWhenUnavailable() {
			leftTooth.setAvailable(false);
			rightTooth.setAvailable(false);
			assertThrows(IllegalStateException.class, () -> space.emptyInnerBrush());
		}

		@Test
		@DisplayName("emptyOuterBrush() throws when space is unavailable")
		void emptyOuterThrowsWhenUnavailable() {
			leftTooth.setAvailable(false);
			rightTooth.setAvailable(false);
			assertThrows(IllegalStateException.class, () -> space.emptyOuterBrush());
		}

		@Test
		@DisplayName("Clearing an already-empty brush is a no-op and does not throw")
		void clearingEmptyBrushIsNoOp() {
			space.emptyInnerBrush();
			space.emptyOuterBrush();
			assertTrue(space.isInnerEmpty());
			assertTrue(space.isOuterEmpty());
		}
	}

	@Nested
	@DisplayName("equals() and hashCode() (Lombok @Data)")
	class EqualsAndHashCode {

		@Test
		@DisplayName("Spaces with same fields and brush state are equal")
		void equalSpacesAreEqual() {
			Space other = new Space(leftTooth, rightTooth, 0);
			assertEquals(space, other);
			assertEquals(space.hashCode(), other.hashCode());
		}

		@Test
		@DisplayName("Spaces become unequal once one has a brush installed")
		void differingBrushStateBreaksEquality() {
			Space other = new Space(leftTooth, rightTooth, 0);
			space.setInnerBrush(Brush.M);
			assertFalse(space.equals(other));
		}
	}
}