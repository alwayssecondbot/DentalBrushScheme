package ru.yanes.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UITheme}.
 * <p>
 * The tests check that the default theme is complete, that the compact constructor rejects
 * {@code null} components, and that the derived colors are calculated from the base ones.
 * <p>
 * Themes with changed components are created with {@code UITheme.DEFAULT.toBuilder()}:
 * a copy of the default theme where only the components under test are replaced.
 */
class UIThemeTests {

	/**
	 * Names of all record components of {@link UITheme}, read with reflection.
	 * Used to make sure that the tests cover every component, including the ones added in the future.
	 *
	 * @return a stream with the names of the record components.
	 */
	static Stream<String> componentNames() {
		return Arrays.stream(UITheme.class.getRecordComponents()).map(RecordComponent::getName);
	}

	/**
	 * Builder modifications that replace exactly one component of the default theme with {@code null}.
	 * <p>
	 * Each argument is the name of the component and the modification of the builder.
	 * When a component is added to {@link UITheme}, a new line must be added here;
	 * {@link ConstructorChecks#everyComponentHasNullCheckCase()} fails until it is done.
	 *
	 * @return a stream of arguments: component name and builder modification.
	 */
	static Stream<Arguments> nullComponentCases() {
		return Stream.of(
				nullCase("backgroundColor", b -> b.backgroundColor(null)),
				nullCase("panelMainFontFamily", b -> b.panelMainFontFamily(null)),
				nullCase("panelMainColor", b -> b.panelMainColor(null)),
				nullCase("panelSubFontFamily", b -> b.panelSubFontFamily(null)),
				nullCase("panelSubColor", b -> b.panelSubColor(null)),
				nullCase("selectionShadowColor", b -> b.selectionShadowColor(null)),
				nullCase("brushOutlineColor", b -> b.brushOutlineColor(null)),
				nullCase("toothOutlineColor", b -> b.toothOutlineColor(null)),
				nullCase("toothColor", b -> b.toothColor(null)),
				nullCase("spaceOutlineColor", b -> b.spaceOutlineColor(null))
		);
	}

	/**
	 * Creates one argument set for {@link #nullComponentCases()}.
	 *
	 * @param componentName name of the component that is replaced with {@code null};
	 * @param modification modification of the builder that sets this component to {@code null}.
	 * @return arguments for the parameterized test.
	 */
	private static Arguments nullCase(String componentName, UnaryOperator<UITheme.UIThemeBuilder> modification) {
		return Arguments.of(componentName, modification);
	}

	/**
	 * Checks that the second color is strictly darker than the first one:
	 * no RGB channel is lighter, and the colors are not equal.
	 *
	 * @param base the base color;
	 * @param derived the color that is expected to be darker.
	 * @return {@code true} if {@code derived} is darker than {@code base}.
	 */
	private static boolean isDarker(Color base, Color derived) {
		return !base.equals(derived)
				&& derived.getRed() <= base.getRed()
				&& derived.getGreen() <= base.getGreen()
				&& derived.getBlue() <= base.getBlue();
	}

	@Nested
	@DisplayName("Default theme")
	class DefaultTheme {

		@Test
		@DisplayName("DEFAULT is created without exceptions")
		void defaultIsCreated() {
			assertDoesNotThrow(() -> UITheme.DEFAULT);
		}

		@ParameterizedTest(name = "DEFAULT component {0} is not null")
		@MethodSource("ru.yanes.config.UIThemeTests#componentNames")
		void defaultComponentIsNotNull(String componentName) throws ReflectiveOperationException {
			Object value = UITheme.class.getMethod(componentName).invoke(UITheme.DEFAULT);
			assertNotNull(value, () -> "DEFAULT." + componentName + " is null");
		}

		@Test
		@DisplayName("toBuilder() without changes creates a theme equal to DEFAULT")
		void toBuilderCopyEqualsDefault() {
			assertEquals(UITheme.DEFAULT, UITheme.DEFAULT.toBuilder().build());
		}

		@Test
		@DisplayName("Changing a copy made with toBuilder() does not change DEFAULT")
		void changingCopyDoesNotChangeDefault() {
			Color originalBackground = UITheme.DEFAULT.backgroundColor();
			Color newBackground = new Color(1, 2, 3);

			UITheme copy = UITheme.DEFAULT.toBuilder().backgroundColor(newBackground).build();

			assertAll(
					() -> assertEquals(newBackground, copy.backgroundColor()),
					() -> assertEquals(originalBackground, UITheme.DEFAULT.backgroundColor()),
					() -> assertNotEquals(UITheme.DEFAULT, copy)
			);
		}
	}

	@Nested
	@DisplayName("Constructor null checks")
	class ConstructorChecks {

		@ParameterizedTest(name = "null {0} throws NullPointerException with the component name")
		@MethodSource("ru.yanes.config.UIThemeTests#nullComponentCases")
		void nullComponentThrows(String componentName, UnaryOperator<UITheme.UIThemeBuilder> modification) {
			UITheme.UIThemeBuilder builder = modification.apply(UITheme.DEFAULT.toBuilder());

			NullPointerException exception = assertThrows(NullPointerException.class, builder::build);
			assertTrue(exception.getMessage().contains(componentName),
					() -> "Message '" + exception.getMessage() + "' doesn't mention " + componentName);
		}

		@Test
		@DisplayName("Every record component has a null check case")
		void everyComponentHasNullCheckCase() {
			assertEquals(
					componentNames().sorted().toList(),
					nullComponentCases().map(arguments -> (String) arguments.get()[0]).sorted().toList(),
					"nullComponentCases() must contain exactly one case for every record component"
			);
		}
	}

	@Nested
	@DisplayName("Derived colors")
	class DerivedColors {

		@Test
		@DisplayName("Unavailable tooth color is darker than tooth color")
		void unavailableToothColorIsDarker() {
			UITheme theme = UITheme.DEFAULT;
			assertAll(
					() -> assertEquals(theme.toothColor().darker(), theme.unavailableToothColor()),
					() -> assertTrue(isDarker(theme.toothColor(), theme.unavailableToothColor()))
			);
		}

		@Test
		@DisplayName("Available space color equals backgroundColor")
		void availableSpaceColorEqualsBackground() {
			assertEquals(UITheme.DEFAULT.backgroundColor(), UITheme.DEFAULT.availableSpaceColor());
		}

		@Test
		@DisplayName("Unavailable space color is darker than backgroundColor")
		void unavailableSpaceColorIsDarker() {
			UITheme theme = UITheme.DEFAULT;
			assertAll(
					() -> assertEquals(theme.backgroundColor().darker(), theme.unavailableSpaceColor()),
					() -> assertTrue(isDarker(theme.backgroundColor(), theme.unavailableSpaceColor()))
			);
		}

		@Test
		@DisplayName("Comment text area backgroundColor equals backgroundColor")
		void commentTextAreaBackgroundEqualsBackground() {
			assertEquals(UITheme.DEFAULT.backgroundColor(), UITheme.DEFAULT.commentTextAreaBackgroundColor());
		}

		@Test
		@DisplayName("Comment text area outline is darker than backgroundColor")
		void commentTextAreaOutlineIsDarker() {
			UITheme theme = UITheme.DEFAULT;
			assertAll(
					() -> assertEquals(theme.backgroundColor().darker(), theme.commentTextAreaOutlineColor()),
					() -> assertTrue(isDarker(theme.backgroundColor(), theme.commentTextAreaOutlineColor()))
			);
		}

		@Test
		@DisplayName("Derived colors follow changed base colors")
		void derivedColorsFollowBaseColors() {
			Color background = new Color(100, 150, 200);
			Color tooth = new Color(250, 200, 150);
			UITheme theme = UITheme.DEFAULT.toBuilder()
					.backgroundColor(background)
					.toothColor(tooth)
					.build();

			assertAll(
					() -> assertEquals(tooth.darker(), theme.unavailableToothColor()),
					() -> assertEquals(background, theme.availableSpaceColor()),
					() -> assertEquals(background.darker(), theme.unavailableSpaceColor()),
					() -> assertEquals(background, theme.commentTextAreaBackgroundColor()),
					() -> assertEquals(background.darker(), theme.commentTextAreaOutlineColor())
			);
		}
	}
}
