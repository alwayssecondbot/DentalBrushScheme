package ru.yanes.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.config.UITheme;
import ru.yanes.controllers.BrushCenter;
import ru.yanes.controllers.DentalGeometry;

import ru.yanes.models.Brush;
import ru.yanes.state.BrushSelection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Objects;

import static ru.yanes.config.UIConstants.BRUSH_PANEL_PREFERRED_HEIGHT;
import static ru.yanes.config.UIConstants.BRUSH_PANEL_PREFERRED_WIDTH;
import static ru.yanes.config.DentalGeometryConstants.*;


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

	private final UITheme theme;

	/**
	 * Creates a brush selection panel, configures the background, dimensions, and click handler,
	 * and subscribes the panel to repaint on every change of {@code brushSelection}.
	 * The preferred size is taken from {@link ru.yanes.config.UIConstants#BRUSH_PANEL_PREFERRED_WIDTH}
	 * and {@link ru.yanes.config.UIConstants#BRUSH_PANEL_PREFERRED_HEIGHT}.
	 *
	 * @param brushSelection shared state of the selected brush;
	 */
	public BrushPanel(BrushSelection brushSelection, UITheme theme) {
		this.brushSelection = brushSelection;
		this.brushSelection.addListener(ignored -> repaint());
		this.theme = theme;

		this.setBackground(this.theme.backgroundColor());
		this.setPreferredSize(new Dimension(BRUSH_PANEL_PREFERRED_WIDTH, BRUSH_PANEL_PREFERRED_HEIGHT));

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
							theme: '{}'
						].""",
				this.getPreferredSize(), this.getBackground(), this.log, this.brushSelection, this.theme);
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

		for (Brush brush : Brush.values()) {
			BrushCenter brushCenter = DentalGeometry.getBrushCircleCenter(this.getWidth(), this.getHeight(), brush);

			// Save the original transform state
			AffineTransform originalTransform = g2d.getTransform();

			// Translate the origin to our calculated x, y coordinates
			g2d.translate(brushCenter.x(), brushCenter.y());

			if (Objects.equals(brush, brushSelection.getSelectedBrush())) {
				g2d.setColor(theme.selectionShadowColor());
				g2d.setStroke(new BasicStroke(SELECTION_SHADOW_THICKNESS));
				g2d.drawOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER + SELECTION_SHADOW_OFFSET,
						BRUSH_DIAMETER + SELECTION_SHADOW_OFFSET);
			}

			// Draw the brush shape (centered on 0,0 since we translated)
			g2d.setColor(brush.getColor());
			g2d.fillOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER, BRUSH_DIAMETER);


			// Draw the outline
			g2d.setColor(theme.brushOutlineColor());
			g2d.setStroke(new BasicStroke(BRUSH_OUTLINE_THICKNESS));
			g2d.drawOval(-BRUSH_DIAMETER / 2, -BRUSH_DIAMETER / 2, BRUSH_DIAMETER, BRUSH_DIAMETER);

			// Draw brush diameter
			String posText = String.valueOf(brush.getDiameter());
			g2d.setFont(new Font(theme.panelMainFontFamily(), Font.BOLD, 14));
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
