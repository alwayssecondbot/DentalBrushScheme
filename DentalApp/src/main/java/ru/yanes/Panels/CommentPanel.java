package ru.yanes.Panels;

import ru.yanes.DentalScheme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;

class CommentPanel extends JPanel {
	private final Color background;
	private final String comment = "Поле для комментария";
	private final int commentAreaRowLimit = 40;
	private final int commentAreaColumnLimit = 25;

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

		this.add(new CommentTextArea(commentAreaRowLimit, commentAreaColumnLimit, new Font(font, Font.PLAIN, 18), background), gbc);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	class CommentTextArea extends JTextArea {
		private final int borderRadius = 20;
		private final float borderThickness = 1.5F;

		private Color background;

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

		class RoundBorder extends AbstractBorder {
			private final float borderThickness;
			private final int cornerRadius;
			private final Color borderColor;

			public RoundBorder(float borderThickness, int cornerRadius, Color borderColor) {
				this.borderThickness = borderThickness;
				this.cornerRadius = cornerRadius;
				this.borderColor = borderColor;
			}

			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setColor(borderColor);
				g2d.setStroke(new BasicStroke(borderThickness));
				g2d.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);
				g2d.dispose();
			}

			@Override
			public Insets getBorderInsets(Component c) {
				int offset = cornerRadius;
				return new Insets(offset, offset, offset, offset);
			}
		}
	}
}
