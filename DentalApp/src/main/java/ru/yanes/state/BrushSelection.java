package ru.yanes.state;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.models.Brush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A state holder for the currently selected brush that notifies subscribers about every change.
 * <p>
 * The class implements the Observer pattern with a push model: when the selection changes,
 * each registered listener receives the new value as an argument. Listeners that do not need the value
 * (for example, panels that just repaint themselves) may ignore it and read the state
 * with {@code getSelectedBrush()} instead.
 * <p>
 * The selection can be changed only through {@link #toggleSelection(Brush)}, so a change
 * can never bypass the notification of listeners. If no brush is selected, the value is {@code null}.
 * <p>
 * The class is not thread-safe and is intended to be used from the Swing event dispatch thread (EDT) only.
 * <p>
 * Lombok's {@code @Getter} annotation generates the getter for the {@link #selectedBrush} field.
 */
public class BrushSelection {
	/**
	 * Logger engine.
	 */
	private final Logger log = LoggerFactory.getLogger(BrushSelection.class);
	/**
	 * The currently selected brush. If no brush is selected, the value is {@code null}.
	 */
	@Getter
	private Brush selectedBrush;
	/**
	 * Subscribers notified on every selection change. They are called in the order they were added;
	 * a listener added twice is called twice.
	 */
	private final List<Consumer<Brush>> listeners = new ArrayList<>();

	/**
	 * Toggles the selection of the given brush and notifies all listeners.
	 * <p>
	 * If the given brush is already selected, the selection is cleared (the value becomes {@code null});
	 * otherwise, the given brush becomes selected. Listeners are notified after the new value is saved,
	 * so {@code getSelectedBrush()} already returns the new value inside a listener.
	 *
	 * @param brush the brush to select or deselect; must not be {@code null}.
	 * @throws NullPointerException if {@code brush} is {@code null}; the selection and listeners stay untouched.
	 */
	public void toggleSelection(Brush brush) {
		Objects.requireNonNull(brush, "Brush is null");
		Brush selectedBrush = this.getSelectedBrush();
		if (Objects.equals(brush, selectedBrush)) {
			log.info("Deselected brush: [ diameter: '{}' ].", brush.getDiameter());
			this.selectedBrush = null;
		} else {
			log.info("Selected brush: [ diameter: '{}' ].", brush.getDiameter());
			this.selectedBrush = brush;
		}

		notifyListeners();
	}

	/**
	 * Subscribes a listener to selection changes.
	 * <p>
	 * The listener is called on every subsequent change and receives the new selected brush,
	 * or {@code null} if the selection was cleared. Changes made before subscribing are not replayed.
	 *
	 * @param listener the action to call on every selection change; must not be {@code null}.
	 * @throws NullPointerException if {@code listener} is {@code null}.
	 */
	public void addListener(Consumer<Brush> listener) {
		Objects.requireNonNull(listener, "listener is null");
		listeners.add(listener);
	}

	/**
	 * Calls every listener in the order they were added and passes the current {@link #selectedBrush} to it.
	 */
	private void notifyListeners() {
		listeners.forEach(listener -> listener.accept(selectedBrush));
	}
}
