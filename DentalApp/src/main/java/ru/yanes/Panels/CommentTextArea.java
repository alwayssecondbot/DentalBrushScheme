package ru.yanes.Panels;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * A text area for a comment with a fixed number of rows and columns.
 * <p>
 * The area has a rounded border, a semi-transparent background (opaque = false), and displays
 * horizontal lines visually separating lines. Text input is limited by the number of
 * characters and lines using {@link TextLimitDocumentFilter}, which is set
 * on the document of this area.
 * <p>
 * The class inherits {@link JTextArea} and overrides the {@link #paintComponent(Graphics)} method
 * to draw line borders, and also uses the inner {@link RoundBorder} class
 * to create a rounded border.
 */
public class CommentTextArea extends JTextArea {
	/** Border corner radius in pixels. */
	private final int borderRadius = 20;
	/** Borderline thickness. */
	private final float borderThickness = 2F;
	/** Maximum number of lines allowed in a text area. */
	private final int rowLimit;
	/** Maximum allowed number of columns (characters per line). */
	private final int colLimit;
	/** Text area background color. */
	private Color background;

	/**
	 * Creates a comment text area with the specified parameters.
	 * <p>
	 * Sets the font, enables word and line wrap, and makes the area
	 * opaque (opaque = false) so the background can be drawn manually.
	 * A {@link TextLimitDocumentFilter} is applied to the area document, limiting the
	 * total number of characters to {@code colLimit * rowLimit}, and the number
	 * of rows to {@code rowLimit}.
	 *
	 * @param rows number of rows (limit);
	 * @param columns number of columns (character limit per line);
	 * @param font text font;
	 * @param background area background color.
	 */
	public CommentTextArea(int rows, int columns, Font font, Color background) {
		super(rows, columns);
		this.rowLimit = rows;
		this.colLimit = columns;

		this.background = background;

		this.setEditable(true);
		this.setLineWrap(true);
		this.setWrapStyleWord(true);
		this.setOpaque(false);
		this.setFont(font);

		this.setBorder(new CommentTextArea.RoundBorder(borderThickness, borderRadius, background.darker()));

		PlainDocument document = (PlainDocument) this.getDocument();
		document.setDocumentFilter(new TextLimitDocumentFilter(rowLimit, colLimit * rowLimit));
	}

	/**
	 * Overridden component rendering method. First, it calls the parent's rendering method
	 * , then draws horizontal lines on top of it to separate the rows.
	 * The lines are drawn with the {@code background.darker()} color and spaced equal to the row height.
	 * After the lines are drawn, the parent method is called again with the modified
	 * graphics context (to correctly display text on top of the lines).
	 *
	 * @param g graphics context.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(background);

		FontMetrics fontMetrics = g2d.getFontMetrics();
		int lineHeight = fontMetrics.getHeight();
		int textStartX = getInsets().left;

		g2d.setColor(background.darker());
		int y = getInsets().top + fontMetrics.getAscent() + 1;
		for (int i = 0; i < rowLimit; i++) {
			g2d.drawLine(textStartX, y, getWidth() - getInsets().right, y);
			y += lineHeight;
		}

		super.paintComponent(g2d);
		g2d.dispose();
	}

	/**
	 * Inner class implementing a border with rounded corners.
	 * Draws a rounded rectangle outline using the specified width,
	 * radius, and color. Returns the appropriate padding to prevent text from overlapping
	 * the border.
	 */
	class RoundBorder extends AbstractBorder {
		/** Borderline thickness. */
		private final float borderThickness;
		/** Corner rounding radius. */
		private final int cornerRadius;
		/** Borderline color. */
		private final Color borderColor;

		/**
		 * Creates a rounded border with the specified parameters.
		 *
		 * @param borderThickness line thickness;
		 * @param cornerRadius corner radius;
		 * @param borderColor line color.
		 */
		public RoundBorder(float borderThickness, int cornerRadius, Color borderColor) {
			this.borderThickness = borderThickness;
			this.cornerRadius = cornerRadius;
			this.borderColor = borderColor;
		}


		/**
		 * Draws a rounded rectangle border around the component.
		 * Enables anti-aliasing for smooth curves.
		 *
		 * @param c the component for which to draw the border;
		 * @param g the graphics context;
		 * @param x the x-coordinate of the upper-left corner;
		 * @param y the y-coordinate of the upper-left corner;
		 * @param width the width of the drawing area;
		 * @param height the height of the drawing area.
		 */
		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(borderColor);
			g2d.setStroke(new BasicStroke(borderThickness));
			g2d.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);
			g2d.dispose();
		}

		/**
		 * Returns the padding the component must have to prevent its contents
		 * from intersecting with the rounded border. The padding is equal to the corner radius
		 * on all sides.
		 *
		 * @param c the component for which the padding is calculated.
		 * @return an {@link Insets} object with equal padding equal to {@code cornerRadius}.
		 */
		@Override
		public Insets getBorderInsets(Component c) {
			int offset = cornerRadius;
			return new Insets(offset, offset, offset, offset);
		}
	}
}