package ru.yanes.config;

import lombok.Builder;

import java.awt.*;
import java.util.Objects;

/**
 * The visual theme of the application: colors and font families used by all panels.
 * <p>
 * The theme describes only <b>how</b> the interface looks. Sizes and styles of fonts are not part of the theme:
 * each panel derives its own font from {@link #panelMainFontFamily} or {@link #panelSubFontFamily}
 * with the size and style passed from {@link ru.yanes.DentalScheme}. Positions and sizes of shapes are stored
 * in {@link DentalGeometryConstants} and {@link UIConstants}.
 * <p>
 * The theme is immutable. {@link #DEFAULT} is the current look of the application; other themes
 * (for example, with another group of fonts) are created from it with {@code DEFAULT.toBuilder()},
 * changing only the needed components.
 * <p>
 * Some colors are not stored but derived from the base ones (for example, {@link #unavailableToothColor()}),
 * so they always follow a change of the base color.
 * <p>
 * Lombok's {@code @Builder(toBuilder = true)} annotation generates {@code UITheme.builder()} and {@code toBuilder()}.
 *
 * @param backgroundColor background color of all panels;
 * @param panelMainFontFamily font family name of the main elements of the panels;
 * @param panelMainColor color of the main elements of the panels (for example, jaw labels);
 * @param panelSubFontFamily font family name of the secondary elements of the panels;
 * @param panelSubColor color of the secondary elements of the panels;
 * @param selectionShadowColor color of the shadow ring around the selected brush;
 * @param brushOutlineColor color of the outline of the brush circles;
 * @param toothOutlineColor color of the outline of the teeth;
 * @param toothColor fill color of an available tooth;
 * @param spaceOutlineColor color of the outline of the space triangles.
 */
@Builder(toBuilder = true)
public record UITheme(
		Color backgroundColor,
		String panelMainFontFamily,
		Color panelMainColor,
		String panelSubFontFamily,
		Color panelSubColor,
		Color selectionShadowColor,
		Color brushOutlineColor,
		Color toothOutlineColor,
		Color toothColor,
		Color spaceOutlineColor
		) {

	/**
	 * The default theme: the current look of the application.
	 * <p>
	 * Use it directly, or as a base for other themes with {@code DEFAULT.toBuilder()}.
	 */
	public static final UITheme DEFAULT = UITheme.builder()
			.backgroundColor(new Color(230, 240, 230))
			.panelMainFontFamily("Arial")
			.panelMainColor(Color.DARK_GRAY)
			.panelSubFontFamily("SansSerif")
			.panelSubColor(Color.WHITE)
			.selectionShadowColor(Color.LIGHT_GRAY)
			.brushOutlineColor(Color.BLACK)
			.toothOutlineColor(Color.BLACK)
			.toothColor(Color.WHITE)
			.spaceOutlineColor(Color.BLACK)
			.build();

	/**
	 * Compact constructor: checks that no component is {@code null}, so an incomplete theme
	 * fails at creation and not later during painting.
	 *
	 * @throws NullPointerException if any component is {@code null}; the message contains the component name.
	 */
	public UITheme {
		Objects.requireNonNull(backgroundColor, "backgroundColor can't be null");
		Objects.requireNonNull(panelMainFontFamily, "panelMainFontFamily can't be null");
		Objects.requireNonNull(panelMainColor, "panelMainColor can't be null");
		Objects.requireNonNull(panelSubFontFamily, "panelSubFontFamily can't be null");
		Objects.requireNonNull(panelSubColor, "panelSubColor can't be null");
		Objects.requireNonNull(selectionShadowColor, "selectionShadowColor can't be null");
		Objects.requireNonNull(brushOutlineColor, "brushOutlineColor can't be null");
		Objects.requireNonNull(toothOutlineColor, "toothOutlineColor can't be null");
		Objects.requireNonNull(toothColor, "toothColor can't be null");
		Objects.requireNonNull(spaceOutlineColor, "spaceOutlineColor can't be null");
	}

	/**
	 * Fill color of an unavailable (missing) tooth: a darker variant of {@link #toothColor}.
	 *
	 * @return {@code toothColor.darker()}.
	 */
	public Color unavailableToothColor() {
		return toothColor.darker();
	}

	/**
	 * Fill color of an available space without a brush: the same as {@link #backgroundColor},
	 * so an empty space looks like a hole in the scheme.
	 *
	 * @return {@code backgroundColor}.
	 */
	public Color availableSpaceColor() {
		return backgroundColor;
	}

	/**
	 * Fill color of an unavailable space (both neighbour teeth are missing):
	 * a darker variant of {@link #backgroundColor}.
	 *
	 * @return {@code backgroundColor.darker()}.
	 */
	public Color unavailableSpaceColor() {
		return backgroundColor.darker();
	}

	/**
	 * Background color of the comment text area: the same as {@link #backgroundColor}.
	 *
	 * @return {@code backgroundColor}.
	 */
	public Color commentTextAreaBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Color of the rounded border and the row lines of the comment text area:
	 * a darker variant of {@link #backgroundColor}.
	 *
	 * @return {@code backgroundColor.darker()}.
	 */
	public Color commentTextAreaOutlineColor() {
		return backgroundColor.darker();
	}
}
