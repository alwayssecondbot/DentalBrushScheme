package ru.yanes.state;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.models.Brush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
	private final List<Consumer<Brush>> listeners = new ArrayList<>();

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

	public void addListener(Consumer<Brush> listener) {
		Objects.requireNonNull(listener, "listener is null");
		listeners.add(listener);
	}

	private void notifyListeners() {
		listeners.forEach(listener -> listener.accept(selectedBrush));
	}
}
