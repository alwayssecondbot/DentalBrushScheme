package ru.yanes.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yanes.config.UITheme;
import ru.yanes.controllers.DentalGeometry;
import ru.yanes.models.Brush;
import ru.yanes.models.Mouth;
import ru.yanes.models.Space;
import ru.yanes.models.Tooth;
import ru.yanes.state.BrushSelection;

import static ru.yanes.config.UIConstants.*;
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
 * <p>
 * The panel does not own its state: the {@link Mouth} model and the {@link BrushSelection} are passed
 * to the constructor. The panel subscribes to {@link BrushSelection} and repaints itself on every change,
 * because the spaces are shown only while a brush is selected or already placed in them.
 * Hit-testing of clicks is delegated to {@link DentalGeometry}.
 */
public class DentalPanel extends JPanel implements Printable {
	/**
	 * Logger engine for {@link DentalPanel}.
	 */
	private final Logger log = LoggerFactory.getLogger(DentalPanel.class);
	/**
	 * Mouth model containing teeth and spaces. It is passed from outside and changed on clicks.
	 */
	private final Mouth mouth;
	/**
	 * Shared state of the selected brush: read on clicks and when the spaces are painted.
	 */
	private final BrushSelection brushSelection;
	/**
	 * Comment title text.
	 */
	private final String comment = "Поле для комментария";
	private final UITheme theme;


	/**
	 * Creates the main panel, saves the mouth model, adds a comment text area
	 * and a click handler for teeth and spaces, and subscribes the panel to repaint
	 * on every change of {@code brushSelection}. The size limits of the comment text area are taken from
	 * {@link ru.yanes.config.UIConstants#COMMENT_AREA_ROW_LIMIT} and {@link ru.yanes.config.UIConstants#COMMENT_AREA_COLUMN_LIMIT}.
	 *
	 * @param brushSelection shared state of the selected brush;
	 * @param mouth mouth model with teeth and spaces to display and change;
	 */
	public DentalPanel(BrushSelection brushSelection, Mouth mouth, UITheme theme) {
		this.mouth = mouth;
		this.theme = theme;
		this.brushSelection = brushSelection;
		this.brushSelection.addListener(ignored -> repaint());

		this.setBackground(theme.backgroundColor());
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.CENTER;

		CommentTextArea commentTextArea = new CommentTextArea(COMMENT_AREA_ROW_LIMIT, COMMENT_AREA_COLUMN_LIMIT,
				new Font(theme.panelSubFontFamily(), Font.PLAIN, 18), theme.backgroundColor());
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
                        	theme: '{}',
                        	brushSelection: '{}',
                        	comment: '{}'
                        ].""",
				this.getBackground(), this.getLayout(), this.log, this.mouth, this.theme,
				this.brushSelection, this.comment);
	}

	/**
	 * Handles a mouse click on the panel. First, it checks the upper jaw, then the lower.
	 * <p>
	 * A click on a tooth toggles its availability. A click on a part of a space sets the selected brush
	 * to this part, but only if a brush is selected and the space is available.
	 * If a tooth or space is hit, the panel is redrawn.
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
		DentalGeometry.checkToothClick(mouseX, mouseY, centerX, centerY - JAW_VERTICAL_OFFSET, true)
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
			DentalGeometry.checkSpaceClick(mouseX, mouseY, centerX, centerY - JAW_VERTICAL_OFFSET, true)
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
		DentalGeometry.checkToothClick(mouseX, mouseY, centerX, centerY + JAW_VERTICAL_OFFSET, false)
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
			DentalGeometry.checkSpaceClick(mouseX, mouseY, centerX, centerY + JAW_VERTICAL_OFFSET, false)
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
	 * Renders the upper and lower jaws, as well as the labels "Upper Jaw",
	 * "Lower Jaw", "Left" and "Right". Enables antialiasing for high-quality rendering.
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
		this.drawJaw(g2d, true, centerX, centerY - JAW_VERTICAL_OFFSET, UPPER_JAW_START_ANGLE, UPPER_JAW_END_ANGLE);


		// Draw Lower Jaw (Angles from roughly 190 degrees to 350 degrees)
		this.drawJaw(g2d, false, centerX, centerY + JAW_VERTICAL_OFFSET, LOWER_JAW_START_ANGLE, LOWER_JAW_END_ANGLE);

		g2d.setFont(new Font(theme.panelMainFontFamily(), Font.BOLD, 25));
		g2d.setColor(theme.panelSubColor());

		// Define padding and metrics for jaws markers
		FontMetrics fm = g2d.getFontMetrics();

		// Upper Jaw marker definition
		String topText = "Верхняя челюсть";
		int topTextWidth = fm.stringWidth(topText);
		int topTextX = centerX - topTextWidth / 2;
		int topTextY = centerY - JAW_RADIUS_Y - JAW_LABEL_MARGIN;
		g2d.drawString(topText, topTextX, topTextY);

		// Lower Jaw marker definition
		String bottomText = "Нижняя челюсть";
		int bottomTextWidth = fm.stringWidth(bottomText);
		int bottomTextX = centerX - bottomTextWidth / 2;
		int bottomTextY = centerY + JAW_RADIUS_Y + JAW_LABEL_MARGIN + fm.getAscent();
		g2d.drawString(bottomText, bottomTextX, bottomTextY);

		// Left side marker
		String leftText = "Лево";
		int leftTextWidth = fm.stringWidth(leftText);
		int leftTextX = centerX - JAW_RADIUS_X - leftTextWidth / 2;
		g2d.drawString(leftText, leftTextX, centerY);

		//Right side marker
		String rightText = "Право";
		int rightTextWidth = fm.stringWidth(rightText);
		int rightTextX = centerX + JAW_RADIUS_X - rightTextWidth / 2;
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
			int x = (int) (centerX + JAW_RADIUS_X * Math.cos(currentAngle));
			int y = (int) (centerY + JAW_RADIUS_Y * Math.sin(currentAngle));

			this.drawTooth(g2d, tooth, x, y, currentAngle);


			if (i < 15) {
				Space space;
				if (upper) {
					space = this.mouth.getSpace(start + i);
				} else {
					space = this.mouth.getSpace(start + i - 1);
				}

				if (Objects.nonNull(brushSelection.getSelectedBrush()) ||
						(Objects.nonNull(space.getOuterBrush()) || Objects.nonNull(space.getInnerBrush()))) {
					double midAngle = currentAngle + angleStep / 2;
					int midX = (int) (centerX + JAW_RADIUS_X * Math.cos(midAngle));
					int midY = (int) (centerY + JAW_RADIUS_Y * Math.sin(midAngle));

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
		g2d.setColor(tooth.isAvailable() ? theme.toothColor() : theme.unavailableToothColor());
		g2d.fillRoundRect(-TOOTH_WIDTH / 2, -TOOTH_HEIGHT / 2, TOOTH_WIDTH, TOOTH_HEIGHT, TOOTH_CORNER_ARC_WIDTH, TOOTH_CORNER_ARC_HEIGHT);

		// Draw the outline
		g2d.setColor(theme.toothOutlineColor());
		g2d.setStroke(new BasicStroke(TOOTH_OUTLINE_THICKNESS));
		g2d.drawRoundRect(-TOOTH_WIDTH / 2, -TOOTH_HEIGHT / 2, TOOTH_WIDTH, TOOTH_HEIGHT, TOOTH_CORNER_ARC_WIDTH, TOOTH_CORNER_ARC_HEIGHT);

		// Draw the FDI position text
		String posText = String.valueOf(tooth.getPosition());
		g2d.setFont(new Font(theme.panelSubFontFamily(), Font.BOLD, 16));
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
					return theme.availableSpaceColor();
				}
			}
			return theme.unavailableSpaceColor();
		};

		log.trace("Rendering spaces '{}' between left tooth - '{}' and right tooth - '{}'.", space.getPosition(), space.getLeftTooth().getPosition(), space.getRightTooth().getPosition());

		Stroke stroke;
		//Set stroke params
		if (space.isAvailable() && Objects.isNull(space.getInnerBrush()) && Objects.isNull(space.getOuterBrush())) {
			stroke = EMPTY_SPACE_STROKE;
		} else {
			stroke = FILLED_SPACE_STROKE;
		}

		// Draw outer space
		if (Objects.isNull(space.getInnerBrush())) {
			drawSpace(g2d, spaceTriangleX(), outerSpaceTriangleY(), getColor.apply(space.getOuterBrush()), stroke);
		}

		// Draw inner space
		if (Objects.isNull(space.getOuterBrush())) {
			drawSpace(g2d, spaceTriangleX(), innerSpaceTriangleY(), getColor.apply(space.getInnerBrush()), stroke);
		}

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
		g2d.setColor(theme.spaceOutlineColor());
		g2d.setStroke(stroke);
		g2d.drawPolygon(xPoints, yPoints, 3);
	}

	/**
	 * Implementation of the {@link Printable} interface for printing the panel's contents.
	 * Creates an image of the panel, scales it to the printable area
	 * and outputs it to the page. Only one page is supported.
	 *
	 * @param graphics the printer's graphics context;
	 * @param pageFormat the page format;
	 * @param pageIndex the page index (0 is the only page).
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
