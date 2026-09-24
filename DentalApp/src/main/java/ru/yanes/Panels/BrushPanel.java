package ru.yanes.Panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.DentalScheme;
import ru.yanes.data.Brush;

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
 */
public class BrushPanel extends JPanel {
	/**
	 * Logger engine.
	 */
	private final Logger log = LoggerFactory.getLogger(BrushPanel.class);
	/**
	 * Link to the main panel to notify about the need to redraw after selecting a brush.
	 */
	private final DentalPanel dentalPanel;
	/**
	 * The diameter of the circle representing the brush, in pixels.
	 */
	private final int brushDiameter = 70;
	/** Link to the panel for setting {@code selectedBrush} */
	private final DentalScheme dentalScheme;
	/**Font family name which is used for the panel.*/
	private final Font font;

	/**
	 * Creates a brush selection panel, configures the background, dimensions, and click handler.
	 *
	 * @param dentalScheme reference to the main class with {@code selectedBrush} field;
	 * @param dentalPanel reference to the main panel for display refresh;
	 * @param background background color of the panel;
	 * @param preferredSize preferred size of the panel: [0] - width, [1] - height;
	 * @param font font family name of the panel.
	 */
	public BrushPanel(DentalScheme dentalScheme, DentalPanel dentalPanel, Color background, int[] preferredSize, String font) {
		this.dentalPanel = dentalPanel;
		this.dentalScheme = dentalScheme;
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
							dentalPanel: '{}'
							brushDiameter: '{}',
							dentalScheme: '{}'
						].""",
				this.getPreferredSize(), this.getBackground(), this.log, this.dentalPanel, this.brushDiameter, this.dentalScheme);
	}

	/**
	 * Handles a mouse click on the panel. Checks whether the click hits one of the brushes,
	 * and redraws the panel and the main panel if necessary.
	 *
	 * @param mouseX is the x-coordinate of the click;
	 * @param mouseY is the y-coordinate of the click.
	 */
	private void handlePanelClick(int mouseX, int mouseY) {
		log.debug("handlePanelClick: [ mouseX: {}, mouseY: {} ].", mouseX, mouseY);
		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		// Check Brush click target
		if (checkBrushClick(mouseX, mouseY, centerX, centerY)) {
			repaint();
			dentalPanel.repaint();
			return;
		}

		log.trace("Click at x={}, y={} did not target any brush.", mouseX, mouseY);
	}

	/**
	 * Checks whether the click hits one of the brushes. If a hit is detected,
	 * toggles the state of the selected brush ({@code selectedBrush}): if the brush was already
	 * selected, deselects it; otherwise, selects it.
	 *
	 * @param mouseX  : x-coordinate of the click;
	 * @param mouseY  : y-coordinate of the click;
	 * @param centerX : x-coordinate of the panel center (brush axis);
	 * @param centerY : y-coordinate of the panel center (not used in the current logic).
	 * @return {@code true} if the click was processed (hit a brush), otherwise {@code false}.
	 */
	private boolean checkBrushClick(int mouseX, int mouseY, int centerX, int centerY) {
		log.debug("CheckBrushClick: [ mouseX: '{}', mouseY: '{}', centerX: '{}', centerY: '{}' ].", mouseX, mouseY, centerX, centerY);

		int brushCount = Brush.values().length;

		for (int i = 1; i <= brushCount; i++) {

			int brushPos = this.getHeight() / (brushCount + 1) * i;

			//If x,y of mouse in brush
			if (getDistance(centerX, brushPos, mouseX, mouseY) <= (double) brushDiameter / 2) {
				Brush clickedBrush = Brush.values()[i - 1];

				//Then if brush has been selected - unpin, if it hasn't - pin
				if (Objects.equals(clickedBrush, dentalScheme.getSelectedBrush())) {
					log.info("Deselected brush: [ diameter: '{}' ].", clickedBrush.getDiameter());
					dentalScheme.setSelectedBrush(null);
				} else {
					log.info("Selected brush: [ diameter: '{}' ].", clickedBrush.getDiameter());
					dentalScheme.setSelectedBrush(clickedBrush);
				}

				return true;
			}

		}

		return false;
	}

	/**
	 * Calculates the Euclidean distance between two points.
	 *
	 * @param x1 is the x-coordinate of the first point;
	 * @param y1 is the y-coordinate of the first point;
	 * @param x2 is the x-coordinate of the second point;
	 * @param y2 is the y-coordinate of the second point.
	 * @return the distance between the points.
	 */
	private double getDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
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

			if (Objects.equals(brush, dentalScheme.getSelectedBrush())) {
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.setStroke(new BasicStroke(10F));
				g2d.drawOval(-brushDiameter / 2, -brushDiameter / 2, brushDiameter + 5, brushDiameter + 5);
			}

			// Draw the brush shape (centered on 0,0 since we translated)
			g2d.setColor(brush.getColor());
			g2d.fillOval(-brushDiameter / 2, -brushDiameter / 2, brushDiameter, brushDiameter);


			// Draw the outline
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2.5F));
			g2d.drawOval(-brushDiameter / 2, -brushDiameter / 2, brushDiameter, brushDiameter);

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
