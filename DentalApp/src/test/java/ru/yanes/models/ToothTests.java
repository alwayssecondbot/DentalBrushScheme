package ru.yanes.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Tooth}.
 * <p>
 * Placed in the same package ({@code ru.yanes.models}) because the
 * {@link Tooth} constructor is package-private.
 */
class ToothTests {

	@Nested
	@DisplayName("Wisdom teeth (18, 28, 38, 48) are unavailable by default")
	class WisdomTeeth {

		@ParameterizedTest(name = "position {0} is unavailable")
		@ValueSource(ints = {18, 28, 38, 48})
		void wisdomTeethAreUnavailableOnCreation(int position) {
			Tooth tooth = new Tooth(position);
			assertFalse(tooth.isAvailable(), "Wisdom tooth " + position + " should be unavailable");
		}
	}

	@Nested
	@DisplayName("All other teeth are available by default")
	class RegularTeeth {

		@ParameterizedTest(name = "position {0} is available")
		@ValueSource(ints = {11, 12, 13, 14, 15, 16, 17,
				21, 22, 23, 24, 25, 26, 27,
				31, 32, 33, 34, 35, 36, 37,
				41, 42, 43, 44, 45, 46, 47})
		void regularTeethAreAvailableOnCreation(int position) {
			Tooth tooth = new Tooth(position);
			assertTrue(tooth.isAvailable(), "Tooth " + position + " should be available");
		}
	}

	@Test
	@DisplayName("Position is stored correctly and is immutable (no setter generated)")
	void positionIsStoredCorrectly() {
		Tooth tooth = new Tooth(11);
		assertEquals(11, tooth.getPosition());
	}

	@Test
	@DisplayName("Availability can be changed via the generated setter")
	void availabilityCanBeToggled() {
		Tooth tooth = new Tooth(11);
		assertTrue(tooth.isAvailable());

		tooth.setAvailable(false);
		assertFalse(tooth.isAvailable());

		tooth.setAvailable(true);
		assertTrue(tooth.isAvailable());
	}

	@Test
	@DisplayName("Wisdom tooth availability can be manually enabled")
	void wisdomToothAvailabilityCanBeManuallyEnabled() {
		Tooth wisdomTooth = new Tooth(48);
		assertFalse(wisdomTooth.isAvailable());

		wisdomTooth.setAvailable(true);
		assertTrue(wisdomTooth.isAvailable());
	}

	@Nested
	@DisplayName("equals() and hashCode() (Lombok @Data)")
	class EqualsAndHashCode {

		@Test
		@DisplayName("Two teeth with same position and availability are equal")
		void equalTeethAreEqual() {
			Tooth a = new Tooth(11);
			Tooth b = new Tooth(11);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}

		@Test
		@DisplayName("Teeth with different positions are not equal")
		void differentPositionsAreNotEqual() {
			Tooth a = new Tooth(11);
			Tooth b = new Tooth(12);
			assertNotEquals(a, b);
		}

		@Test
		@DisplayName("Teeth with same position but different availability are not equal")
		void differentAvailabilityIsNotEqual() {
			Tooth a = new Tooth(11);
			Tooth b = new Tooth(11);
			b.setAvailable(false);
			assertNotEquals(a, b);
		}
	}

	@Test
	@DisplayName("toString() contains position and availability info")
	void toStringContainsFields() {
		Tooth tooth = new Tooth(21);
		String result = tooth.toString();
		assertTrue(result.contains("21"));
		assertTrue(result.contains("available"));
	}
}