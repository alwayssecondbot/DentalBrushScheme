package ru.yanes.panels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.yanes.config.UITheme;
import ru.yanes.controllers.DentalGeometry;
import ru.yanes.controllers.BrushCenter;
import ru.yanes.models.Brush;
import ru.yanes.state.BrushSelection;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static ru.yanes.config.DentalGeometryConstants.*;

/**
 * Tests for {@link BrushPanel}: selecting and deselecting brushes by mouse clicks, and rendering of the panel.
 * <p>
 * The panel is created without a window, so the tests run in headless mode. Because of that,
 * the panel gets its real size explicitly with {@link BrushPanel#setSize(int, int)}: the preferred size
 * is only a hint for a layout manager, and without a window the actual size would stay 0×0.
 * <p>
 * Clicks are simulated with {@link MouseEvent#MOUSE_PRESSED} events sent through
 * {@link BrushPanel#dispatchEvent(java.awt.AWTEvent)}, so the real mouse listener of the panel is tested.
 * Click coordinates are taken from {@link DentalGeometry#getBrushCircleCenter(int, int, Brush)}.
 * The result is checked through the shared {@link BrushSelection}.
 * <p>
 * Rendering is checked by painting the panel into a {@link BufferedImage} and reading colors of single pixels.
 * The points are chosen far from the edges of the shapes, because antialiasing mixes colors on the edges.
 * Expected colors are taken from the theme of the panel, not hardcoded. The tests with {@link #CONTRAST_THEME}
 * prove that the panel really uses the colors of its theme: with {@link UITheme#DEFAULT} a hardcoded color
 * equal to the default one could not be distinguished from a color taken from the theme.
 */
class BrushPanelTests {
	/** Shared selection state passed to the panel; the panel subscribes to it on creation. Created before each test. */
	private BrushSelection brushSelection;
	/** The panel under test with {@link #THEME}, created before each test with the size {@link #WIDTH} × {@link #HEIGHT}. */
	private BrushPanel brushPanel;
	/** Width of the panel under test, in pixels. */
	private static final int WIDTH = 400;
	/**
	 * Height of the panel under test, in pixels. It is large enough so that brush circles
	 * ({@code BRUSH_DIAMETER = 70}) don't overlap: spacing = {@code HEIGHT / (brushCount + 1) = 100}.
	 */
	private static final int HEIGHT = 800;
	/** Theme of the panel in most tests: the default look of the application. */
	private static final UITheme THEME = UITheme.DEFAULT;
	/**
	 * A theme with unusual colors that differ from {@link UITheme#DEFAULT} and from all brush colors.
	 * Used to check that the panel takes its colors from the theme and not from hardcoded values.
	 */
	private static final UITheme CONTRAST_THEME = UITheme.DEFAULT.toBuilder()
			.backgroundColor(new Color(255, 255, 0))
			.selectionShadowColor(new Color(0, 255, 255))
			.build();

	/**
	 * Creates a new selection and a new panel with {@link #THEME}, and sets the real size of the panel.
	 */
	@BeforeEach
	void setUp() {
		brushSelection = new BrushSelection();
		brushPanel = createPanel(brushSelection, THEME);
	}

	/**
	 * Creates a brush panel with the given selection and theme and sets its real size to {@link #WIDTH} × {@link #HEIGHT}.
	 *
	 * @param selection shared selection state for the panel;
	 * @param theme theme of the panel.
	 * @return a panel ready for clicks and rendering.
	 */
	private static BrushPanel createPanel(BrushSelection selection, UITheme theme) {
		BrushPanel panel = new BrushPanel(selection, theme);
		panel.setSize(WIDTH, HEIGHT);
		return panel;
	}

	/**
	 * Simulates a single left mouse press on the panel at the given point.
	 *
	 * @param x x-coordinate of the click inside the panel;
	 * @param y y-coordinate of the click inside the panel.
	 */
	private void click(int x, int y) {
		brushPanel.dispatchEvent(new MouseEvent(
				brushPanel,								// source - same panel
				MouseEvent.MOUSE_PRESSED,				// event type - listen mousePressed
				System.currentTimeMillis(),				// time
				0,										// modificators - none
				x, y,									// coordinates inside panel
				1,										// number of clicks
				false									// not context menu call
		));
	}

	/**
	 * Paints the given panel into a new image of the size {@link #WIDTH} × {@link #HEIGHT}.
	 *
	 * @param panel the panel to paint.
	 * @return the image with the painted panel.
	 */
	private BufferedImage getRender(BrushPanel panel) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		panel.paint(g);

		g.dispose();
		return image;
	}

	/**
	 * Reads the color of a single pixel of the image.
	 *
	 * @param x x-coordinate of the pixel;
	 * @param y y-coordinate of the pixel;
	 * @param image the image to read from.
	 * @return the color of the pixel.
	 */
	private Color getColorInPoint(int x, int y, BufferedImage image) {
		return new Color(image.getRGB(x, y));
	}

	/**
	 * Y-coordinate of a point inside the visible part of the selection shadow ring, below the brush circle.
	 * <p>
	 * The shadow is shifted to the right and down, so its visible part is the widest below the circle.
	 *
	 * @param y y-coordinate of the brush circle center.
	 * @return y-coordinate of the point on the shadow ring.
	 */
	private static int getSelectedOutlineStrokeY(int y) {
		return y + (BRUSH_DIAMETER / 2) + (int) Math.ceil(SELECTION_SHADOW_THICKNESS / 2);
	}

	@Nested
	@DisplayName("Click handling")
	class BrushPanelClickHandlingTests {
		@ParameterizedTest(name = "Click on the center of {0} brush's circle")
		@EnumSource(Brush.class)
		void clickOnEachBrushCircle(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			click(brushCenter.x(), brushCenter.y());
			assertEquals(brush, brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Click on the same brush twice set it and then unset it")
		void clickOnBrushTwiceUnsetIt() {
			Brush brush = Brush.values()[3];
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);

			click(brushCenter.x(), brushCenter.y());
			Brush selectedBrush = brushSelection.getSelectedBrush();
			click(brushCenter.x(), brushCenter.y());

			assertAll(
					() -> assertEquals(brush, selectedBrush),
					() -> assertNull(brushSelection.getSelectedBrush())
			);
		}

		@Test
		@DisplayName("Two clicks on different brushes set first, then second and keep it")
		void twoClickOnDifferentBrushes() {
			Brush brush1 = Brush.values()[2];
			BrushCenter brushCenter1 = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush1);
			click(brushCenter1.x(), brushCenter1.y());
			Brush firstBrush = brushSelection.getSelectedBrush();

			Brush brush2 = Brush.values()[3];
			BrushCenter brushCenter2 = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush2);
			click(brushCenter2.x(), brushCenter2.y());

			assertAll(
					() -> assertEquals(brush1, firstBrush),
					() -> assertEquals(brush2, brushSelection.getSelectedBrush())
			);
		}

		@Test
		@DisplayName("Click between brush circles sets nothing")
		void clickBetweenBrushesDoNothing() {
			BrushCenter brushCenter1 = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[0]);
			BrushCenter brushCenter2 = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[1]);

			click(brushCenter1.x(), (brushCenter1.y() + brushCenter2.y()) / 2);
			assertNull(brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Click on the edge of brush circle sets it")
		void clickOnBrushEdgeSetsIt() {
			Brush brush = Brush.values()[2];
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);

			click(brushCenter.x(), brushCenter.y() + (BRUSH_DIAMETER) / 2);
			assertEquals(brush, brushSelection.getSelectedBrush());
		}

		@Test
		@DisplayName("Click one pixel down from the brush circle edge do nothing")
		void clickOnOnePixelDownEdgeDoNothing() {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.L);

			click(brushCenter.x(), brushCenter.y() + (BRUSH_DIAMETER) / 2 + 1);
			assertNull(brushSelection.getSelectedBrush());
		}
	}

	@Nested
	@DisplayName("Rendering")
	class BrushPanelRenderingTests {

		@Test
		@DisplayName("Painting panel doesn't throw exception if there is no selected brush")
		void paintingPanelDoesntThrowException() {
			assertDoesNotThrow(() -> getRender(brushPanel));
		}

		@ParameterizedTest(name = "Brush {0} has correct own color")
		@EnumSource(Brush.class)
		void brushHasCorrectOwnColor(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);

			BufferedImage image = getRender(brushPanel);
			assertEquals(brush.getColor(), getColorInPoint(
					brushCenter.x(),
					brushCenter.y() + (BRUSH_DIAMETER / 2) - (int) Math.ceil(BRUSH_OUTLINE_THICKNESS * 2), image));
		}

		@Test
		@DisplayName("Background has the theme background color")
		void backgroundHasReceivedColor() {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[0]);

			BufferedImage image = getRender(brushPanel);
			assertEquals(THEME.backgroundColor(), getColorInPoint(brushCenter.x() - BRUSH_DIAMETER, brushCenter.y() - BRUSH_DIAMETER, image));
		}

		@ParameterizedTest(name = "Brush {0} doesn't have the selection shadow when nothing is selected")
		@EnumSource(Brush.class)
		void brushDoesntHaveOutline(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);

			BufferedImage image = getRender(brushPanel);
			assertNotEquals(THEME.selectionShadowColor(), getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}

		@ParameterizedTest(name = "Selected brush {0} has the selection shadow")
		@EnumSource(Brush.class)
		void brushHasOutline(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			brushSelection.toggleSelection(brush);

			BufferedImage image = getRender(brushPanel);
			assertEquals(THEME.selectionShadowColor(), getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}

		@ParameterizedTest(name = "Selected brush {0} has the selection shadow but others don't")
		@EnumSource(Brush.class)
		void onlySelectedBrushHasOutlineStroke(Brush brush) {
			brushSelection.toggleSelection(brush);

			BufferedImage image = getRender(brushPanel);

			for (Brush b : Brush.values()) {
				BrushCenter bCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, b);
				if (Objects.equals(b, brush)) {
					assertEquals(THEME.selectionShadowColor(), getColorInPoint(bCenter.x(), getSelectedOutlineStrokeY(bCenter.y()), image));
				} else {
					assertNotEquals(THEME.selectionShadowColor(), getColorInPoint(bCenter.x(), getSelectedOutlineStrokeY(bCenter.y()), image));
				}
			}
		}
	}

	@Nested
	@DisplayName("Rendering with a contrast theme")
	class BrushPanelContrastThemeTests {

		@Test
		@DisplayName("Contrast theme colors differ from the default ones, so the tests below are meaningful")
		void contrastThemeDiffersFromDefault() {
			assertAll(
					() -> assertNotEquals(UITheme.DEFAULT.backgroundColor(), CONTRAST_THEME.backgroundColor()),
					() -> assertNotEquals(UITheme.DEFAULT.selectionShadowColor(), CONTRAST_THEME.selectionShadowColor())
			);
		}

		@Test
		@DisplayName("Background is painted with the background color of the panel's theme")
		void backgroundUsesThemeColor() {
			BrushPanel panel = createPanel(new BrushSelection(), CONTRAST_THEME);
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[0]);

			BufferedImage image = getRender(panel);
			assertEquals(CONTRAST_THEME.backgroundColor(),
					getColorInPoint(brushCenter.x() - BRUSH_DIAMETER, brushCenter.y() - BRUSH_DIAMETER, image));
		}

		@ParameterizedTest(name = "Selection shadow of {0} is painted with the shadow color of the panel's theme")
		@EnumSource(Brush.class)
		void selectionShadowUsesThemeColor(Brush brush) {
			BrushSelection selection = new BrushSelection();
			BrushPanel panel = createPanel(selection, CONTRAST_THEME);
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			selection.toggleSelection(brush);

			BufferedImage image = getRender(panel);
			assertEquals(CONTRAST_THEME.selectionShadowColor(),
					getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}
	}
}
