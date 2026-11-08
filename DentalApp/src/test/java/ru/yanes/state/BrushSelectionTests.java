package ru.yanes.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.yanes.models.Brush;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BrushSelection}.
 * <p>
 * The tests are split into two groups: the toggle behaviour of the selected brush
 * and the notification of listeners. Listeners are checked with a "recording" subscriber
 * ({@code received::add}) that stores every received value in a list, so the tests can verify
 * how many notifications were sent, with which values and in which order.
 */
class BrushSelectionTests {

	/** A fresh selection with no brush selected and no listeners, created before each test. */
	private BrushSelection brushSelection;

	@BeforeEach
	void setUp() {
		brushSelection = new BrushSelection();
	}

	@Test
	@DisplayName("Default brush is null")
	void defaultBrushIsNull() {
		assertNull(brushSelection.getSelectedBrush());
	}


	@Nested
	@DisplayName("Toggle behaviour")
	class ToggleSelectedBrushTests {

		@Test
		@DisplayName("Set selected brush to null if selected and toggled are equal")
		void toggleIfBrushesEqual() {
			Brush brush = Brush.L;

			brushSelection.toggleSelection(brush);
			brushSelection.toggleSelection(brush);

			assertNull(brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Change selected brush if selected and toggled aren't equal")
		void toggleIfBrushesNotEqual() {
			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			Brush newBrush = Brush.M;
			brushSelection.toggleSelection(newBrush);

			assertEquals(newBrush, brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Select same brush as it was if toggle it three times")
		void toggleIfSelectSameBrushThreeTimes() {
			Brush brush = Brush.L;

			brushSelection.toggleSelection(brush);
			brushSelection.toggleSelection(brush);
			brushSelection.toggleSelection(brush);

			assertEquals(brush, brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Toggle null doesn't act on selected brush")
		void toggleNullDoesntActOnSelectedBrush() {
			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			assertAll(
					() -> assertThrows(NullPointerException.class, () -> brushSelection.toggleSelection(null)),
					() -> assertEquals(brush, brushSelection.getSelectedBrush())
			);
		}

		@Test
		@DisplayName("Toggle null brush and get exception")
		void toggleNullBrush() {
			assertThrows(NullPointerException.class, () -> brushSelection.toggleSelection(null));
		}
	}

	@Nested
	@DisplayName("Notify and listeners behaviour")
	class NotifyListenersTests {

		@Test
		@DisplayName("Listener is notified only once")
		void listenerIsNotifiedOnlyOnce() {
			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			assertEquals(List.of(brush), received);
		}

		@Test
		@DisplayName("Listener receives new brush")
		void listenerReceivesNewBrush() {
			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			Brush newBrush = Brush.M;
			brushSelection.toggleSelection(newBrush);

			assertEquals(List.of(newBrush), received);
		}

		@Test
		@DisplayName("Listener doesn't receive new brush if there was no toggles")
		void listenerDoesntReceiveNewBrush() {
			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			assertEquals(List.of(), received);
		}

		@Test
		@DisplayName("Listener doesn't get called if there was toggle with null")
		void listenerDoesntGetCalledIfNull() {
			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			assertAll(
					() -> assertThrows(NullPointerException.class, () -> brushSelection.toggleSelection(null)),
					() -> assertEquals(List.of(), received)
			);
		}

		@Test
		@DisplayName("Listener receives null when same brush is toggled twice")
		void listenerReceivesBrushThenNull() {
			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);
			brushSelection.toggleSelection(brush);

			assertEquals(Arrays.asList(brush, null), received);
		}


		@Test
		@DisplayName("Listener is called twice if added twice")
		void listenerIsCalledTwice() {
			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);
			brushSelection.addListener(received::add);

			Brush brush = Brush.M;
			brushSelection.toggleSelection(brush);

			assertEquals(Arrays.asList(brush, brush), received);
		}

		@Test
		@DisplayName("Listener gets current brush when call selected brush")
		void listenerGetsCurrentBrush() {
			Brush brush = Brush.L;
			brushSelection.toggleSelection(brush);

			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(ignored -> received.add(brushSelection.getSelectedBrush()));

			Brush newBrush = Brush.M;
			brushSelection.toggleSelection(newBrush);

			assertEquals(List.of(newBrush), received);
		}

		@Test
		@DisplayName("All listeners are called")
		void allListenersAreCalled() {
			List<Brush> received1 = new ArrayList<>();
			brushSelection.addListener(received1::add);
			List<Brush> received2 = new ArrayList<>();
			brushSelection.addListener(received2::add);
			List<Brush> received3 = new ArrayList<>();
			brushSelection.addListener(received3::add);

			Brush brush = Brush.M;
			brushSelection.toggleSelection(brush);

			assertAll(
					() -> assertEquals(List.of(brush), received1),
					() -> assertEquals(List.of(brush), received2),
					() -> assertEquals(List.of(brush), received3));
		}

		@Test
		@DisplayName("Toggle works without listeners")
		void toggleWorksWithoutListeners() {
			Brush brush = Brush.M;
			brushSelection.toggleSelection(brush);

			assertEquals(brush, brushSelection.getSelectedBrush());
		}

		@ParameterizedTest(name = "Listener gets brush type {0} correctly")
		@EnumSource(Brush.class)
		void notifyListenersWithEachBrush(Brush brush) {
			List<Brush> received = new ArrayList<>();
			brushSelection.addListener(received::add);

			brushSelection.toggleSelection(brush);
			assertEquals(List.of(brush), received);
		}

		@Test
		@DisplayName("Add null listener and get exception")
		void addNullListener() {
			assertThrows(NullPointerException.class, () -> brushSelection.addListener(null));
		}
	}
}
