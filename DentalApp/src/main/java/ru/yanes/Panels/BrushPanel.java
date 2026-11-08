package ru.yanes.Panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.controllers.DentalGeometry;
import static ru.yanes.config.DentalGeometryConstants.BRUSH_DIAMETER;
import ru.yanes.models.Brush;
import ru.yanes.state.BrushSelection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Objects;



/**
 * A panel for displaying and selecting toothbrush types.
 * Brushes are drawn vertically as colored circles with their diameters indicated.
 * Clicking on a brush selects or deselects it.
 * <p>
 * The selection is stored in a shared {@link BrushSelection}. The panel subscribes to it
 * and repaints itself on every change, so it does not need to know about other panels.
 */
public class BrushPanel extends JPanel {
	/**
	 * Logger engine.
	 */
	private final Logger log = LoggerFactory.getLogger(BrushPanel.class);
	/** Shared state of the selected brush: toggled on click and read when the panel is painted. */
	private final BrushSelection brushSelection;
	/** Font which is used for the brush diameter labels. */
	private final Font font;

	/**
	 * Creates a brush selection panel, configures the background, dimensions, and click handler,
	 * and subscribes the panel to repaint on every change of {@code brushSelection}.
	 *
	 * @param brushSelection shared state of the selected brush;
	 * @param background background color of the panel;
	 * @param preferredSize preferred size of the panel: [0] - width, [1] - height;
	 * @param font font family name of the panel.
	 */
	public BrushPanel(BrushSelection brushSelection, Color background, int[] preferredSize, String font) {
		this.brushSelection = brushSelection;
		this.brushSelection.addListener(ignored -> repaint());
		this.font = new Font(font, Font.BOLD, 14);

		this.setBackground(background);
		this.setPreferredSize(new Dimension(preferredSize[0], preferredSize[1]));

		log.info("Adding mouse listener brush panel...");
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				handlePanelClick(e.getX(), e.getY());
			}
		});

		log.info("Tool panel initialized successfully.");
		log.trace("""
						Panel parameters: [
							preferredSize: '{}',
							background: '{}',
						];
						Class fields: [
							log: '{}',
							brushSelection: '{}',
							font: '{}'
						].""",
				this.getPreferredSize(), this.getBackground(), this.log, this.brushSelection, this.font);
	}

	/**
	 * Handles a mouse click on the panel. Checks whether the click hits one of the brushes
	 * and, if so, toggles its selection in {@link BrushSelection}. Repainting of the panels
	 * is triggered by the {@link BrushSelection} listeners.
	 *
	 * @param mouseX is the x-coordinate of the click;
	 * @param mouseY is the y-coordinate of the click.
	 */
	private void handlePanelClick(int mouseX, int mouseY) {
		log.debug("handlePanelClick: [ mouseX: {}, mouseY: {} ].", mouseX, mouseY);

		// Check Brush click target
		log.debug("CheckBrushClick: [ mouseX: '{}', mouseY: '{}' ].", mouseX, mouseY);

		DentalGeometry.checkBrushClick(mouseX, mouseY, this.getWidth(), this.getHeight()).ifPresentOrElse(
				brushSelection::toggleSelection,
				() -> log.trace("Click at x={}, y={} did not target any brush.", mouseX, mouseY) );
	}

	/**
	 * Draws all available brushes as circles arranged vertically in the center of the panel.
	 * The selected brush is additionally highlighted with a gray ring. Inside each circle,
	 * the brush diameter is displayed.
	 *
	 * @param g graphics context.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int brushCount = Brush.values().length;
		int centerX = this.getWidth() / 2;

		for (int i = 1; i <= brushCount; i++) {
			Brush brush = Brush.values()[i - 1];
			int brushPos = this.getHeight() / (brushCount + 1) * i;

			// Save the original transform state
			AffineTransform originalTransform = g2d.getTransform();

			// Translate the origin to our calculated x, y coordinates
			g2d.translate(centerX, brushPos);

			if (Objects.equals(brush, brushSelection.getSelectedBrush())) {
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.setStroke(new BasicStroke(10F));
				g2d.drawOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER + 5, BRUSH_DIAMETER + 5);
			}

			// Draw the brush shape (centered on 0,0 since we translated)
			g2d.setColor(brush.getColor());
			g2d.fillOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER, BRUSH_DIAMETER);


			// Draw the outline
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2.5F));
			g2d.drawOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER, BRUSH_DIAMETER);

			// Draw brush diameter
			String posText = String.valueOf(brush.getDiameter());
			g2d.setFont(font);
			FontMetrics fontMetrics = g2d.getFontMetrics();
			int textX = -fontMetrics.stringWidth(posText) / 2;
			int textY = fontMetrics.getHeight() / 2 - 2;

			g2d.drawString(posText, textX, textY);

			// Restore the original transform so the next tooth draws correctly
			g2d.setTransform(originalTransform);
		}

		g2d.dispose();
	}


}
