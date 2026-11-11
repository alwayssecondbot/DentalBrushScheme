package ru.yanes.panels;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
 * Tests for {@link BrushPanel}: selecting and deselecting brushes by mouse clicks.
 * <p>
 * The panel is created without a window, so the tests run in headless mode. Because of that,
 * the panel gets its real size explicitly with {@link BrushPanel#setSize(int, int)}: the preferred size
 * passed to the constructor is only a hint for a layout manager, and without a window
 * the actual size would stay 0×0.
 * <p>
 * Clicks are simulated with {@link MouseEvent#MOUSE_PRESSED} events sent through
 * {@link BrushPanel#dispatchEvent(java.awt.AWTEvent)}, so the real mouse listener of the panel is tested.
 * Click coordinates are taken from {@link DentalGeometry#getBrushCircleCenter(int, int, Brush)}.
 * The result is checked through the shared {@link BrushSelection}.
 */
class BrushPanelTests {
	/** Shared selection state passed to the panel; the panel subscribes to it on creation. Created before each test. */
	private BrushSelection brushSelection;
	/** The panel under test, created before each test with the size {@link #WIDTH} × {@link #HEIGHT}. */
	private BrushPanel brushPanel;
	/** Width of the panel under test, in pixels. */
	private static final int WIDTH = 400;
	/**
	 * Height of the panel under test, in pixels. It is large enough so that brush circles
	 * ({@code BRUSH_DIAMETER = 70}) don't overlap: spacing = {@code HEIGHT / (brushCount + 1) = 100}.
	 */
	private static final int HEIGHT = 800;

	private static final Color BACKGROUND = new Color(230, 240, 230);

	/**
	 * Creates a new selection and a new panel, and sets the real size of the panel.
	 */
	@BeforeEach
	void setUp() {
		brushSelection = new BrushSelection();
		brushPanel = new BrushPanel(brushSelection, BACKGROUND, new int[] {WIDTH, HEIGHT}, Font.DIALOG);
		brushPanel.setSize(WIDTH, HEIGHT);
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

	private BufferedImage getRender(BrushPanel brushPanel) {
		BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();
		brushPanel.paint(g);

		g.dispose();
		return image;
	}

	private Color getColorInPoint(int x, int y, BufferedImage image) {
		return new Color(image.getRGB(x, y));
	}

	@Nested
	@DisplayName("Brush panel backend tests")
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
	@DisplayName("Brush panel frontend tests")
	class BrushPanelRenderingTests {

		int getSelectedOutlineStrokeY(int y){
			return y + (BRUSH_DIAMETER / 2) + (int) Math.ceil(SELECTED_BRUSH_STROKE_THICKNESS / 2);
		}


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
					brushCenter.y() + (BRUSH_DIAMETER / 2) - (int) Math.ceil(BRUSH_STROKE_THICKNESS * 2), image));
		}

		@Test
		@DisplayName("Background has received color")
		void backgroundHasReceivedColor() {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, Brush.values()[0]);

			BufferedImage image = getRender(brushPanel);
			assertEquals(BACKGROUND, getColorInPoint(brushCenter.x() - BRUSH_DIAMETER, brushCenter.y() - BRUSH_DIAMETER, image));
		}

		@ParameterizedTest(name = "Brush {0} doesn't have outline stroke because nearest color not LIGHT_GRAY")
		@EnumSource(Brush.class)
		void brushDoesntHaveOutline(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);

			BufferedImage image = getRender(brushPanel);
			assertNotEquals(Color.LIGHT_GRAY, getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}

		@ParameterizedTest(name = "Brush {0} has outline stroke because nearest color LIGHT_GRAY")
		@EnumSource(Brush.class)
		void brushHasOutline(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			brushSelection.toggleSelection(brush);

			BufferedImage image = getRender(brushPanel);
			assertEquals(Color.LIGHT_GRAY, getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}

		@Test
		@DisplayName("Panel shows brush selection even if brush has been selected not by click")
		void brushHasBeenSelectedNotByClick() {
			Brush brush = Brush.values()[0];
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			brushSelection.toggleSelection(brush);

			BufferedImage image = getRender(brushPanel);
			assertEquals(Color.LIGHT_GRAY, getColorInPoint(brushCenter.x(), getSelectedOutlineStrokeY(brushCenter.y()), image));
		}

		@ParameterizedTest(name = "Selected brush {0} has outline stroke but other doesn't")
		@EnumSource(Brush.class)
		void onlySelectedBrushHasOutlineStroke(Brush brush) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, brush);
			brushSelection.toggleSelection(brush);

			BufferedImage image = getRender(brushPanel);

			for (Brush b : Brush.values()) {
				BrushCenter bCenter = DentalGeometry.getBrushCircleCenter(WIDTH, HEIGHT, b);
				if (Objects.equals(b, brush)) {
					assertEquals(Color.LIGHT_GRAY, getColorInPoint(bCenter.x(), getSelectedOutlineStrokeY(bCenter.y()), image));
				} else {
					assertNotEquals(Color.LIGHT_GRAY, getColorInPoint(bCenter.x(), getSelectedOutlineStrokeY(bCenter.y()), image));
				}
			}
		}
	}
}
