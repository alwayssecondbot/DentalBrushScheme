package ru.yanes.panels;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;

/**
 * A standalone side panel with a comment text area.
 * <p>
 * <b>Currently not used:</b> the comment field is embedded in the {@link DentalPanel} instead
 * (see {@link ru.yanes.panels.CommentTextArea}). The panel is kept for a possible return to the side layout;
 * its creation in {@link ru.yanes.DentalScheme} is commented out.
 * <p>
 * The panel contains its own inner copy of the comment area ({@link CommentTextArea})
 * that draws lines up to the bottom of the area instead of a fixed number of rows.
 */
class CommentPanel extends JPanel {
	/** Background color of the panel and the comment area. */
	private final Color background;
	/** Comment title text. */
	private final String comment = "Поле для комментария";
	/** Maximum number of rows in the comment text area. */
	private final int commentAreaRowLimit = 40;
	/** Maximum number of columns in the comment text area. */
	private final int commentAreaColumnLimit = 25;

	/**
	 * Creates a comment panel with a centered comment text area.
	 *
	 * @param background background color of the panel;
	 * @param preferredSize preferred size of the panel: [0] - width, [1] - height;
	 * @param font font family name of the comment text.
	 */
	public CommentPanel(Color background, int[] preferredSize, String font) {
		this.background = background;
		this.setBackground(this.background);
		this.setPreferredSize(new Dimension(preferredSize[0], preferredSize[1]));
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(15, 15, 15, 15);
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.CENTER;

		CommentTextArea commentTextArea = new CommentTextArea(commentAreaRowLimit, commentAreaColumnLimit, new Font(font, Font.PLAIN, 18), background);
		commentTextArea.setName("commentTextArea");
		this.add(commentTextArea, gbc);
	}

	/**
	 * Paints the panel background and enables antialiasing for shapes and text.
	 *
	 * @param g graphics context.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	/**
	 * Inner comment text area of the {@link CommentPanel} with a rounded border and lines under the text.
	 * <p>
	 * Unlike {@link ru.yanes.panels.CommentTextArea}, the lines are drawn up to the bottom of the area.
	 * Input is limited by {@link TextLimitDocumentFilter}.
	 */
	class CommentTextArea extends JTextArea {
		/** Border corner radius in pixels. */
		private final int borderRadius = 20;
		/** Borderline thickness. */
		private final float borderThickness = 1.5F;
		/** Text area background color. */
		private Color background;

		/**
		 * Creates a comment text area with the specified parameters.
		 *
		 * @param rows number of rows (limit);
		 * @param columns number of columns (character limit per line);
		 * @param font text font;
		 * @param background area background color.
		 */
		public CommentTextArea(int rows, int columns, Font font, Color background) {
			super(rows, columns);

			this.background = background;

			this.setEditable(true);
			this.setLineWrap(true);
			this.setWrapStyleWord(true);
			this.setOpaque(false);

			this.setFont(font);

			this.setBorder(new CommentTextArea.RoundBorder(borderThickness, borderRadius, background.darker()));
			PlainDocument document = (PlainDocument) this.getDocument();
			document.setDocumentFilter(new TextLimitDocumentFilter(rows, columns * rows));
		}

		/**
		 * Draws horizontal lines under the text rows up to the bottom of the area,
		 * then paints the text on top of them.
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

			g2d.setColor(this.background.darker());
			int y = getInsets().top + fontMetrics.getAscent();
			while (y < getHeight()) {
				g2d.drawLine(textStartX, y, getWidth() - getInsets().right, y);
				y += lineHeight;
			}

			super.paintComponent(g2d);
			g2d.dispose();
		}

		/**
		 * Inner class implementing a border with rounded corners.
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
			 * Draws a rounded rectangle border around the component with antialiasing.
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
			 * Returns padding equal to the corner radius on all sides,
			 * so the text does not overlap the rounded border.
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
}
