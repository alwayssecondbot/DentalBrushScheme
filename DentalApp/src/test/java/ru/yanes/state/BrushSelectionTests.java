package ru.yanes.state;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.yanes.models.Brush;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BrushSelectionTests {

    private BrushSelection brushSelection;

    @BeforeEach
    void setUp() {
        brushSelection = new BrushSelection();
    }

    @Test
    @DisplayName("Default brush is null")
    void defaultBrushIsNull(){
        assertNull(brushSelection.getSelectedBrush());
    }


    @Nested
    @DisplayName("Test toggle behaviour")
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
            List<Brush> listener = new ArrayList<>();
            Brush brush = Brush.L;
            brushSelection.addListener(listener::add);
            brushSelection.toggleSelection(brush);
            assertArrayEquals(new Brush[]{brush}, listener.toArray());
        }

        @Test
        @DisplayName("Listener receives new brush")
        void listenerReceivesNewBrush() {
            Brush brush = Brush.L;
            brushSelection.toggleSelection(brush);
            List<Brush> listener = new ArrayList<>();
            brushSelection.addListener(listener::add);
            Brush newBrush = Brush.M;
            brushSelection.toggleSelection(newBrush);
            assertEquals(newBrush, listener.getLast());
        }

        @ParameterizedTest(name = "{0} ")
        @EnumSource(Brush.class)
        void testNotifyListeners() {}

        @Test
        @DisplayName("Add null listener and get exception")
        void addNullListener() {
            assertThrows(NullPointerException.class, () -> brushSelection.addListener(null));
        }
    }
}
