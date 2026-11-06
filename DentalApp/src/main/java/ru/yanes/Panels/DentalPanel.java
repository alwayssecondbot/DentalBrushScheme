package ru.yanes.Panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yanes.controllers.DentalGeometry;
import ru.yanes.models.Brush;
import ru.yanes.models.Mouth;
import ru.yanes.models.Space;
import ru.yanes.models.Tooth;
import ru.yanes.state.BrushSelection;

import static ru.yanes.config.DentalGeometryConstants.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Objects;
import java.util.function.Function;


/**
 * The main panel displays the upper and lower jaws with teeth and spaces,
 * as well as a comment field. Implements the {@link Printable} interface for printing.
 * Handles clicks on teeth (toggle accessibility) and spaces
 * (set the selected toothbrush).
 */
public class DentalPanel extends JPanel implements Printable {
	/**
	 * Logger engine for {@link DentalPanel}.
	 */
	private final Logger log = LoggerFactory.getLogger(DentalPanel.class);
	/**
	 * Basic tooth color (white).
	 */
	private final Color basicToothColor = Color.WHITE;
	/**
	 * Mouth model containing teeth and spaces.
	 */
	private final Mouth mouth;
	/**
	 * The color of panel background and empty spaces.
	 */
	private final Color background;
	/**
	 * Link to the panel for setting {@code selectedBrush}
	 */
	private final BrushSelection brushSelection;
	/**
	 * Comment title text.
	 */
	private final String comment = "Поле для комментария";
	/**
	 * Maximum number of rows in the comment text area.
	 */
	private final int commentAreaRowLimit = 20;
	/**
	 * Maximum number of columns in the comment text area.
	 */
	private final int commentAreaColumnLimit = 25;


	/**
	 * Creates the main panel, saves the mouth model, adds a comment text area
	 * and a click handler for teeth and spaces.
	 *
	 * @param brushSelection reference to the main class with {@code selectedBrush} field;
	 * @param background     background color of the panel;
	 * @param font           font family name of the panel.
	 */
	public DentalPanel(BrushSelection brushSelection, Mouth mouth, Color background, String font) {
		this.mouth = mouth;

		this.background = background;
		this.brushSelection = brushSelection;
		this.brushSelection.addListener(brush -> repaint());

		this.setBackground(this.background);
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		CommentTextArea commentTextArea = new CommentTextArea(commentAreaRowLimit, commentAreaColumnLimit, new Font(font, Font.PLAIN, 18), this.background);
		commentTextArea.setName("commentTextArea");
		this.add(commentTextArea, gbc);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				handlePanelClick(e.getX(), e.getY());
			}
		});
		log.info("DentalPanel initialized successfully.");
		log.trace("""
                        Panel parameters: [
                        	background: '{}',
                        	layout: '{}'
                        ];
                        Class fields: [
                        	log: '{}',
                        	mouth: '{}',
                        	basicToothColor: '{}',
                        	background: '{}'
                        	brushSelection: '{}',
                        	comment: '{}',
                        	commentAreaRowLimit: '{}',
                        	commentAreaColumnLimit: '{}'
                        ].""",
				this.getBackground(), this.getLayout(), this.log, this.mouth, this.basicToothColor, this.background, this.brushSelection,
				this.comment, this.commentAreaRowLimit, this.commentAreaColumnLimit);
	}

	/**
	 * Handles a mouse click on the panel. First, it checks the upper jaw, then the lower.
	 * If a tooth or gap is hit, it causes the panel to be redrawn.
	 *
	 * @param mouseX is the x-coordinate of the click;
	 * @param mouseY is the y-coordinate of the click.
	 */
	private void handlePanelClick(int mouseX, int mouseY) {

		int centerX = getWidth() / 2;
		int centerY = getHeight() / 2;

		Brush selectedBrush = brushSelection.getSelectedBrush();
		log.debug("""
                	HandlePanelClick: [
                		x: '{}',
                		y: '{}',
                		selectedBrush: '{}'
                ].""", mouseX, mouseY, selectedBrush);

		// Check Upper Jaw click targets
		DentalGeometry.checkToothClick(mouseX, mouseY, centerX, centerY - 40, true)
				.ifPresentOrElse(pos -> {
					Tooth clickedTooth = mouth.getTooth(pos);
					clickedTooth.setAvailable(!clickedTooth.isAvailable());
					log.debug("""
                            Clicked tooth: [
                            	FDI: '{}',
                            	available: '{}'
                            ].""", clickedTooth.getPosition(), clickedTooth.isAvailable());
					this.repaint();
				}, () -> log.trace("Click at x={}, y={} did not target any tooth on the upper jaw.", mouseX, mouseY));

		if (Objects.nonNull(selectedBrush)) {
			// Check Upper Jaw click targets
			DentalGeometry.checkSpaceClick(mouseX, mouseY, centerX, centerY - 40, true)
					.ifPresentOrElse(hit -> {
						Space space = mouth.getSpace(hit.position());
						if (space.isAvailable()) {
							if (hit.isInner()) {
								space.setInnerBrush(selectedBrush);
							} else {
								space.setOuterBrush(selectedBrush);
							}
						}
						log.debug("""
                                Clicked space: [
                                	position: '{}',
                                	inner: '{}',
                                	brush: '{}'
                                ].""", space.getPosition(), hit.isInner(), hit.isInner() ? space.getInnerBrush() : space.getOuterBrush());

						this.repaint();
					}, () -> log.trace("Click at x={}, y={} did not target any space on the upper jaw.", mouseX, mouseY));
		}


		// Check Lower Jaw click targets
		DentalGeometry.checkToothClick(mouseX, mouseY, centerX, centerY + 40, false)
				.ifPresentOrElse(pos -> {
					Tooth clickedTooth = mouth.getTooth(pos);
					clickedTooth.setAvailable(!clickedTooth.isAvailable());
					log.debug("""
                            Clicked tooth: [
                            	FDI: '{}',
                            	available: '{}'
                            ].""", clickedTooth.getPosition(), clickedTooth.isAvailable());
					this.repaint();
				}, () -> log.trace("Click at x={}, y={} did not target any tooth on the lower jaw.", mouseX, mouseY));


		if (Objects.nonNull(selectedBrush)) {
			// Check Lower Jaw click targets
			DentalGeometry.checkSpaceClick(mouseX, mouseY, centerX, centerY + 40, false)
					.ifPresentOrElse(hit -> {
						Space space = mouth.getSpace(hit.position());
						if (space.isAvailable()) {
							if (hit.isInner()) {
								space.setInnerBrush(selectedBrush);
							} else {
								space.setOuterBrush(selectedBrush);
							}
						}

						log.debug("""
                                Clicked space: [
                                	position: '{}',
                                	inner: '{}',
                                	brush: '{}'
                                ].""", space.getPosition(), hit.isInner(), hit.isInner() ? space.getInnerBrush() : space.getOuterBrush());
						this.repaint();
					}, () -> log.trace("Click at x={}, y={} did not target any space on the lower jaw.", mouseX, mouseY));
		}
	}

	/**
	 * Renders the upper and lower jaws, as well as the labels "Upper Jaw,"
	 * "Lower Jaw," "Left," and "Right." Enables antialiasing for high-quality rendering.
	 *
	 * @param g graphics context.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Enable antialiasing for smooth circles and text
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int centerX = this.getWidth() / 2;
		int centerY = this.getHeight() / 2;

		// Draw Upper Jaw (Angles from roughly 170 degrees to 10 degrees) and it is wider than lower jaw (radius X and Y smaller)
		this.drawJaw(g2d, true, centerX, centerY - 40, START_ANGLE_UPPER, END_ANGLE_UPPER);


		// Draw Lower Jaw (Angles from roughly 190 degrees to 350 degrees)
		this.drawJaw(g2d, false, centerX, centerY + 40, START_ANGLE_LOWER, END_ANGLE_LOWER);

		g2d.setFont(new Font("Arial", Font.BOLD, 25));
		g2d.setColor(Color.DARK_GRAY);

		// Define padding and metrics for jaws markers
		FontMetrics fm = g2d.getFontMetrics();
		int padding = 100;

		// Upper Jaw marker definition
		String topText = "Верхняя челюсть";
		int topTextWidth = fm.stringWidth(topText);
		int topTextX = centerX - topTextWidth / 2;
		int topTextY = centerY - RADIUS_Y - padding;
		g2d.drawString(topText, topTextX, topTextY);

		// Lower Jaw marker definition
		String bottomText = "Нижняя челюсть";
		int bottomTextWidth = fm.stringWidth(bottomText);
		int bottomTextX = centerX - bottomTextWidth / 2;
		int bottomTextY = centerY + RADIUS_Y + padding + fm.getAscent();
		g2d.drawString(bottomText, bottomTextX, bottomTextY);

		// Left side marker
		String leftText = "Лево";
		int leftTextWidth = fm.stringWidth(leftText);
		int leftTextX = centerX - RADIUS_X - leftTextWidth / 2;
		g2d.drawString(leftText, leftTextX, centerY);

		//Right side marker
		String rightText = "Право";
		int rightTextWidth = fm.stringWidth(rightText);
		int rightTextX = centerX + RADIUS_X - rightTextWidth / 2;
		g2d.drawString(rightText, rightTextX, centerY);
	}

	/**
	 * Draws a single jaw: all 16 teeth and spaces between them (if necessary).
	 * Spaces are drawn only if a toothbrush is selected or if a toothbrush is already placed in them.
	 *
	 * @param g2d        2D graphics context;
	 * @param upper      {@code true} for the upper jaw, {@code false} for the lower jaw;
	 * @param centerX    x-coordinate of the arc center;
	 * @param centerY    y-coordinate of the arc center;
	 * @param startAngle start angle of the arc;
	 * @param endAngle   end angle of the arc.
	 */
	private void drawJaw(Graphics2D g2d, boolean upper, int centerX, int centerY, double startAngle, double endAngle) {

		double angleStep = (endAngle - startAngle) / 15;
		int start = upper ? 0 : 16;

		for (int i = 0; i < 16; ++i) {
			// Retrieve the specific tooth based on our visual map[cite: 2]
			Tooth tooth = this.mouth.getTooth(start + i);

			double currentAngle = startAngle + angleStep * (double) i;

			// Calculate x and y using polar equations for an ellipse
			int x = (int) (centerX + RADIUS_X * Math.cos(currentAngle));
			int y = (int) (centerY + RADIUS_Y * Math.sin(currentAngle));

			this.drawTooth(g2d, tooth, x, y, currentAngle);


			if (i < 15) {
				Space space;
				if (upper) {
					space = this.mouth.getSpace(start + i);
				} else {
					space = this.mouth.getSpace(start + i - 1);
				}

				if (Objects.nonNull(brushSelection.getSelectedBrush()) || (Objects.nonNull(space.getOuterBrush()) || Objects.nonNull(space.getInnerBrush()))) {
					double midAngle = currentAngle + angleStep / 2;
					int midX = (int) (centerX + RADIUS_X * Math.cos(midAngle));
					int midY = (int) (centerY + RADIUS_Y * Math.sin(midAngle));

					this.drawSpaces(g2d, space, midX, midY, midAngle);
				}
			}
		}
	}

	/**
	 * Draws a single tooth at a given position, taking rotation into account. The tooth is displayed as a
	 * rounded rectangle. If a tooth is unavailable, it is darkened. Inside the tooth,
	 * its number (position according to FDI) is displayed.
	 *
	 * @param g2d   2D graphics context;
	 * @param tooth tooth object;
	 * @param x     x-coordinate of the tooth center;
	 * @param y     y-coordinate of the tooth center;
	 * @param angle tooth rotation angle (in radians).
	 */
	private void drawTooth(Graphics2D g2d, Tooth tooth, int x, int y, double angle) {

		log.trace("Rendering tooth FDI: '{}'.", tooth.getPosition());

		// Save the original transform state
		AffineTransform originalTransform = g2d.getTransform();

		// Translate the origin to our calculated x, y coordinates
		g2d.translate(x, y);

		// Rotate the canvas so the tooth points outward from the center
		// Adding Math.PI / 2 ensures the "top" of the tooth shape points away from the center
		g2d.rotate(angle + (Math.PI / 2));

		// Draw the tooth shape (centered on 0,0 since we translated)
		g2d.setColor(tooth.isAvailable() ? basicToothColor : basicToothColor.darker());
		g2d.fillRoundRect(-TOOTH_WIDTH / 2, -TOOTH_HEIGHT / 2, TOOTH_WIDTH, TOOTH_HEIGHT, 20, 20);

		// Draw the outline
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(2.5F));
		g2d.drawRoundRect(-TOOTH_WIDTH / 2, -TOOTH_HEIGHT / 2, TOOTH_WIDTH, TOOTH_HEIGHT, 20, 20);

		// Draw the FDI position text
		String posText = String.valueOf(tooth.getPosition());
		g2d.setFont(new Font("Arial", Font.BOLD, 16));
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int textX = -fontMetrics.stringWidth(posText) / 2;
		int textY = -fontMetrics.getAscent() / 2 - 2;

		g2d.drawString(posText, textX, textY);

		// Restore the original transform so the next tooth draws correctly
		g2d.setTransform(originalTransform);
	}

	/**
	 * Draws a gap between two teeth. The gap can consist of two triangles:
	 * outer and inner. If one of the parts is already occupied by a toothbrush,
	 * the corresponding triangle is not drawn. The color of the triangle depends on the installed toothbrush
	 * or the availability of the gap.
	 *
	 * @param g2d   2D graphics context;
	 * @param space gap object;
	 * @param x     x-coordinate of the gap center;
	 * @param y     y-coordinate of the gap center;
	 * @param angle gap rotation angle (in radians).
	 */
	private void drawSpaces(Graphics2D g2d, Space space, int x, int y, double angle) {
		// Save the original transform state
		AffineTransform originalTransform = g2d.getTransform();

		// Translate the origin to our calculated x, y coordinates
		g2d.translate(x, y);

		// Rotate the canvas so the tooth points outward from the center
		// Adding Math.PI / 2 ensures the "top" of the tooth shape points away from the center
		g2d.rotate(angle + (Math.PI / 2));

		Function<Brush, Color> getColor = (brush) -> {
			if (space.isAvailable()) {
				if (Objects.nonNull(brush)) {
					return brush.getColor();
				} else {
					return background;
				}
			}
			return background.darker();
		};

		log.trace("Rendering spaces '{}' between left tooth - '{}' and right tooth - '{}'.", space.getPosition(), space.getLeftTooth().getPosition(), space.getRightTooth().getPosition());

		Stroke stroke;
		//Set stroke params
		if (space.isAvailable() && Objects.isNull(space.getInnerBrush()) && Objects.isNull(space.getOuterBrush())) {
			stroke = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new float[]{19.0f, 5.0f}, 0.0f);
		} else {
			stroke = new BasicStroke(2.0F);
		}

		// Draw outer space
		if (Objects.isNull(space.getInnerBrush())) {
			drawSpace(g2d, BASIC_SPACE_X, OUTER_SPACE_Y, getColor.apply(space.getOuterBrush()), stroke);
		}

		// Draw inner space
		if (Objects.isNull(space.getOuterBrush())) {
			drawSpace(g2d, BASIC_SPACE_X, INNER_SPACE_Y, getColor.apply(space.getInnerBrush()), stroke);
		}

		// Draw the FDI position text
//			String posText = String.valueOf(space.getPosition());
//			g2d.setFont(new Font("Arial", Font.BOLD, 16));
//			FontMetrics fontMetrics = g2d.getFontMetrics();
//			int textX = -fontMetrics.stringWidth(posText) / 2;
//			int textY = fontMetrics.getAscent() / 2 - 2;
//			g2d.drawString(posText, textX, textY);

		// Restore the original transform so the next space draws correctly
		g2d.setTransform(originalTransform);
	}

	/**
	 * Draws a single triangular space (a polygon of three points) with the specified fill color
	 * and stroke style. Used as a helper method for {@link #drawSpaces}.
	 *
	 * @param g2d     2D graphics context;
	 * @param xPoints array of triangle vertex x-coordinates;
	 * @param yPoints array of triangle vertex y-coordinates;
	 * @param color   fill color;
	 * @param stroke  stroke style.
	 */
	//TODO refactor to inner method
	private void drawSpace(Graphics2D g2d, int[] xPoints, int[] yPoints, Color color, Stroke stroke) {

		//Draw the shape
		g2d.setColor(color);
		g2d.fillPolygon(xPoints, yPoints, 3);

		// Draw the outline
		g2d.setColor(Color.BLACK);
		g2d.setStroke(stroke);
		g2d.drawPolygon(xPoints, yPoints, 3);
	}

	/**
	 * Implementation of the {@link Printable} interface for printing the panel's contents.
	 * Creates an image of the panel, scales it to the printable area
	 * and outputs it to the page. Only one page is supported.
	 *
	 * @param graphics   : the printer's graphics context;
	 * @param pageFormat : the page format;
	 * @param pageIndex  : the page index (0 is the only page).
	 * @return {@link Printable#PAGE_EXISTS} if the page was successfully rendered,
	 * {@link Printable#NO_SUCH_PAGE} if a non-existent page was requested.
	 * @throws PrinterException if a printing error occurs.
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		if (pageIndex > 0) {
			log.debug("Print requested for non-existent page index: {}", pageIndex);
			return Printable.NO_SUCH_PAGE;
		}

		log.info("Starting print process for DentalPanel (page index 0)...");

		int width = this.getWidth();
		int height = this.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2dImage = image.createGraphics();

		this.printAll(g2dImage);
		g2dImage.dispose();

		Graphics2D g2d = (Graphics2D) graphics;

		double scaleX = pageFormat.getImageableWidth() / width;
		double scaleY = pageFormat.getImageableHeight() / height;
		double scale = Math.min(scaleX, scaleY);

		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		g2d.scale(scale, scale);
		g2d.drawImage(image, 0, 0, null);

		log.info("DentalPanel printable area successfully rendered to printer graphics.");
		return Printable.PAGE_EXISTS;
	}
}
