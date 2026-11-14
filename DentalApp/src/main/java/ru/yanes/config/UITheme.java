package ru.yanes.config;

import lombok.Builder;

import java.awt.*;
import java.util.Objects;

@Builder(toBuilder = true)
public record UITheme(
		Color background,
		String panelMainFont,
		Color panelMainColor,
		String panelSubFont,
		Color panelSubColor,
		Color selectedBrushOutlineColor,
		Color brushOutlineColor,
		Color toothOutlineColor,
		Color toothColor,
		Color spaceOutlineColor
		) {

	public static final UITheme DEFAULT = UITheme.builder()
			.background(new Color(230, 240, 230))
			.panelMainFont("Arial")
			.panelMainColor(Color.DARK_GRAY)
			.panelSubFont("SansSerif")
			.panelSubColor(Color.WHITE)
			.selectedBrushOutlineColor(Color.LIGHT_GRAY)
			.brushOutlineColor(Color.BLACK)
			.toothOutlineColor(Color.BLACK)
			.toothColor(Color.WHITE)
			.spaceOutlineColor(Color.BLACK)
			.build();

	public UITheme {
		Objects.requireNonNull(background, "background can't be null");
		Objects.requireNonNull(panelMainFont, "panelMainFont can't be null");
		Objects.requireNonNull(panelMainColor, "panelMainColor can't be null");
		Objects.requireNonNull(panelSubFont, "panelSubFont can't be null");
		Objects.requireNonNull(panelSubColor, "panelSubColor can't be null");
		Objects.requireNonNull(selectedBrushOutlineColor, "selectedBrushOutlineColor can't be null");
		Objects.requireNonNull(brushOutlineColor, "brushOutlineColor can't be null");
		Objects.requireNonNull(toothOutlineColor, "toothOutlineColor can't be null");
		Objects.requireNonNull(toothColor, "toothColor can't be null");
		Objects.requireNonNull(spaceOutlineColor, "spaceOutlineColor can't be null");
	}

	public Color unavailableToothColor() {
		return toothColor.darker();
	}

	public Color availableSpaceColor() {
		return background;
	}

	public Color unavailableSpaceColor() {
		return background.darker();
	}

	public Color commentTextAreaBackgroundColor() {
		return background;
	}

	public Color commentTextAreaOutlineColor() {
		return background.darker();
	}
}
