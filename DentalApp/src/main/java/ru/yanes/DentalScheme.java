package ru.yanes;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import javax.swing.*;

import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import ru.yanes.data.Brush;
import ru.yanes.data.Mouth;
import ru.yanes.data.Space;
import ru.yanes.data.Tooth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class of the application, representing the main window of the dental scheme.
 * <p>
 * The window contains three main panels:
 * <ol>
 *     <li>{@link DentalPanel} — displays jaws, teeth, spaces and a comment field;</li>
 *     <li>{@link BrushPanel} — displays the available brush types and allows you to select one of them;</li>
 *     <li>{@link ToolPanel} — contains tool buttons (eg print button).</li>
 * </ol>
 * The app uses the {@link Mouth} model to store the state of teeth and spaces.
 *
 * @author Skorokhodov Ilia
 * @version 0.1b
 * @since 2026-08
 */
public class DentalScheme extends JFrame {
	/** Logger engine for {@link DentalScheme} */
	private static final Logger log = LoggerFactory.getLogger(DentalScheme.class);
	/** The currently selected brush. If no brush is selected, the value is {@code null}. */
	private Brush selectedBrush;
	/** Minimal width and height of app window ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height). */
	private final int[] windowMinSize = new int[]{1600, 1300};
	/** Minimal width and height of {@link BrushPanel} ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height). */
	private final int[] brushPanelPreferSize = new int[]{400, 0};
	/** Minimal width and height of {@code CommentPanel} ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height).
	 * @deprecated The {@code CommentPanel} is disabled, instead the comment is embedded in the {@link DentalPanel}
	 */
	private final int[] commentPanelPreferSize = new int[]{400, 0};
	/** Minimal width and height of {@link ToolPanel} ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height). */
	private final int[] toolPanelPreferSize = new int[]{0, 30};
	/**Font family name which is used by default in app*/
	private String basicFont;
	/**Temporary field with counter for font families*/
	private int count = 0;
	/**Temporary field with list of font family names*/
	private final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	/**Default background color for app. Currently - light green.*/
	private final Color defaultBackground = new Color(230, 240, 230);

	/**
	 * Creates the main application window and initializes the mouth model {@link Mouth},
	 * configures window settings and adds all panels.
	 */
	public DentalScheme() {

		Mouth mouth = new Mouth();
		basicFont = fonts[count];
		System.out.println("Font Name: " + basicFont);

		this.setTitle("\ud83e\uddb7 Дентальная схема");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(windowMinSize[0], windowMinSize[1]));

		DentalPanel dentalPanel = new DentalPanel(mouth);
		this.add(new BrushPanel(dentalPanel), BorderLayout.EAST);
//		this.add(new CommentPanel(), BorderLayout.WEST);
		this.add(new ToolPanel(dentalPanel), BorderLayout.NORTH);
		this.add(dentalPanel, BorderLayout.CENTER);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		log.info("DentalScheme initialized successfully.");
		log.trace("""
				Parameters: [
					mouth: '{}',
					title: '{}',
					defaultCloseOperation: '{}',
					minimumSize: '{}',
					dentalPanel: '{}',
					visible: '{}',
					logger: '{}',
					selectedBrush: '{}',
					windowMinSize: '{}',
					brushPanelPreferSize: '{}',
					commentPanelPreferSize: '{}',
					toolPanelPreferSize: '{}',
					basicFont: '{}',
					defaultBackground: '{}'
				].""",
				mouth, this.getTitle(), this.getDefaultCloseOperation(), this.getMinimumSize(), dentalPanel, this.isVisible(), log,
				this.selectedBrush, this.windowMinSize, this.brushPanelPreferSize, this.commentPanelPreferSize, this.toolPanelPreferSize,
				this.basicFont, this.defaultBackground);
	}

	/**
	 * The entry point to the application.
	 * Sets the system look and feel and launches the graphical interface in the Swing event thread (EDT).
	 * @param args command line arguments (not used)
	 */
	public static void main(String[] args) {
		log.info("Initializing DentalScheme...");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log.error("Error while initializing DentalScheme", e); //
		}

		log.info("Initializing complete.");
		log.trace("""
				Parameters: [
					args: '{}'
				].
				""", (Object) args);
		SwingUtilities.invokeLater(DentalScheme::new);
	}

	/**
	 * A toolbar containing action buttons (for example, printing a diagram).
	 * Located at the top of the window.
	 */
	class ToolPanel extends JPanel {
		/** Link to the main panel {@link ToolPanel} that will be printed. */
		private final DentalPanel frameToPrint;
		/** Logger engine for {@link DentalScheme} */
		private final Logger log = LoggerFactory.getLogger(ToolPanel.class);

		/**
		 * Creates a toolbar and adds a print button to it.
		 *
		 * @param dentalPanel the {@link DentalPanel} panel to print
		 */
		public ToolPanel(DentalPanel dentalPanel) {


			this.frameToPrint = dentalPanel;
			this.setPreferredSize(new Dimension(toolPanelPreferSize[0], toolPanelPreferSize[1]));
			this.setBackground(defaultBackground);
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.setAlignmentY(TOP_ALIGNMENT);
			this.add(getPrintButton());

			log.info("Tool panel initialized successfully.");
			log.trace("""
					Parameters: [
						log: '{}'
						frameToPrint: '{}',
						preferredSize: '{}',
						background: '{}',
						layout: '{}',
						alignmentY: '{}'
					].""",
					this.log, this.frameToPrint, this.getPreferredSize(), this.getBackground(), this.getLayout(), this.getAlignmentY());
		}

		/**
		 * Creates and returns a "Print" button. When clicked, a print dialog opens,
		 * after confirmation, the {@link DentalPanel} is printed.
		 *
		 * @return button with a configured print handler
		 */
		private JButton getPrintButton() {
			log.debug("Configuring print button...");
			return getToolButton("Печать", e -> {

				log.info("Print requested by user. Opening printer job dialog...");

				PrinterJob job = PrinterJob.getPrinterJob();
				job.setPrintable(frameToPrint);

				if (job.printDialog()) {
					try {
						log.info("Print dialog confirmed. Starting print process...");
						job.print();
						log.info("Printing completed successfully.");
					} catch (PrinterException e1) {
						log.error("Error occurred while printing DentalPanel:", e1);
						JOptionPane.showMessageDialog(frameToPrint, "Error while printing: " + e1.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					log.info("Printing was cancelled by the user in the print dialog.");
				}
			});
		}

		/**
		 * Factory method for creating a standard tool button with the given text and action.
		 *
		 * @param text           text on the button
		 * @param actionListener click handler
		 * @return a ready-made button with a customized font, size, and handler
		 */
		private JButton getToolButton(String text, ActionListener actionListener) {
			log.debug("Creating tool button with label: '{}'", text);
			JButton button = new JButton(text);
			button.setFont(new Font(basicFont, Font.PLAIN, 18));
			button.setPreferredSize(new Dimension(100,30));
			button.setFocusPainted(false);
			button.setOpaque(true);

			button.addActionListener(actionListener);

			log.trace("""
					Parameters: [
						button: '{}',
						text: '{}',
						font: '{}',
						preferredSize: '{}',
						focusPainted: '{}',
						opaque: '{}',
						actionListener: '{}'
					].""",
					button, button.getText(), button.getFont(), button.getPreferredSize(), button.isFocusPainted(), button.isOpaque(),
					button.getActionListeners());

			return button;
		}
	}

//	class CommentPanel extends JPanel {
//		private final String comment = "Поле для комментария";
//		private final int commentAreaRowLimit = 40;
//		private final int commentAreaColumnLimit = 25;
//
//		public CommentPanel() {
//			this.setBackground(defaultBackground);
//			this.setPreferredSize(new Dimension(commentPanelPreferSize[0], commentPanelPreferSize[1]));
//			this.setLayout(new GridBagLayout());
//
//			GridBagConstraints gbc = new GridBagConstraints();
//
//			gbc.gridx = 0;
//			gbc.gridy = 0;
//			gbc.insets = new Insets(15, 15, 15, 15);
//			gbc.weightx = 0.0;
//			gbc.weighty = 0.0;
//			gbc.anchor = GridBagConstraints.CENTER;
//
//			this.add(new CommentTextArea(commentAreaRowLimit, commentAreaColumnLimit, new Font(basicFont, Font.PLAIN, 18), defaultBackground), gbc);
//		}
//
//		@Override
//		protected void paintComponent(Graphics g) {
//			super.paintComponent(g);
//			Graphics2D g2d = (Graphics2D) g;
//			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		}
//
//		class CommentTextArea extends JTextArea {
//			private final int borderRadius = 20;
//			private final float borderThickness = 1.5F;
//
//			private Color background;
//
//			public CommentTextArea(int rows, int columns, Font font, Color background) {
//				super(rows, columns);
//
//				this.background = background;
//
//				this.setEditable(true);
//				this.setLineWrap(true);
//				this.setWrapStyleWord(true);
//				this.setOpaque(false);
//
//				this.setFont(font);
//
//				this.setBorder(new RoundBorder(borderThickness, borderRadius, background.darker()));
//				PlainDocument document = (PlainDocument) this.getDocument();
//				document.setDocumentFilter(new TextLimitDocumentFilter(rows, columns * rows));
//			}
//
//			@Override
//			protected void paintComponent(Graphics g) {
//				super.paintComponent(g);
//				Graphics2D g2d = (Graphics2D) g.create();
//				g2d.setColor(background);
//
//				FontMetrics fontMetrics = g2d.getFontMetrics();
//				int lineHeight = fontMetrics.getHeight();
//				int textStartX = getInsets().left;
//
//				g2d.setColor(defaultBackground.darker());
//				int y = getInsets().top + fontMetrics.getAscent();
//				while (y < getHeight()) {
//					g2d.drawLine(textStartX, y, getWidth() - getInsets().right, y);
//					y += lineHeight;
//				}
//
//				super.paintComponent(g2d);
//				g2d.dispose();
//			}
//
//			class RoundBorder extends AbstractBorder {
//				private final float borderThickness;
//				private final int cornerRadius;
//				private final Color borderColor;
//
//				public RoundBorder(float borderThickness, int cornerRadius, Color borderColor) {
//					this.borderThickness = borderThickness;
//					this.cornerRadius = cornerRadius;
//					this.borderColor = borderColor;
//				}
//
//				@Override
//				public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
//					Graphics2D g2d = (Graphics2D) g.create();
//					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//					g2d.setColor(borderColor);
//					g2d.setStroke(new BasicStroke(borderThickness));
//					g2d.drawRoundRect(x, y, width - 1, height - 1, cornerRadius, cornerRadius);
//					g2d.dispose();
//				}
//
//				@Override
//				public Insets getBorderInsets(Component c) {
//					int offset = cornerRadius;
//					return new Insets(offset, offset, offset, offset);
//				}
//			}
//		}
//	}

	/**
	 * A panel for displaying and selecting toothbrush types.
	 * Brushes are drawn vertically as colored circles with their diameters indicated.
	 * Clicking on a brush selects or deselects it.
	 */
	class BrushPanel extends JPanel {
		/** Logger engine for {@link BrushPanel} */
		private final Logger log = LoggerFactory.getLogger(BrushPanel.class);
		/** Link to the main panel to notify about the need to redraw after selecting a brush */
		private final DentalPanel dentalPanel;
		/** The diameter of the circle representing the brush, in pixels.*/
		private final int brushDiameter = 70;

		/**
		 * Creates a brush selection panel, configures the background, dimensions, and click handler.
		 * @param dentalPanel reference to the main panel for display refresh
		 */
		BrushPanel(DentalPanel dentalPanel) {
			this.dentalPanel = dentalPanel;
			this.setBackground(defaultBackground);
			this.setPreferredSize(new Dimension(brushPanelPreferSize[0], brushPanelPreferSize[1]));

			log.info("Adding mouse listener brush panel...");
			this.addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						handlePanelClick(e.getX(), e.getY());
					}
			});

			log.info("Tool panel initialized successfully.");
			log.trace("""
					Parameters: [
						log: '{}',
						dentalPanel: '{}',
						preferredSize: '{}',
						background: '{}',
						brushDiameter: '{}'
					].""",
					this.log, this.dentalPanel, this.getPreferredSize(), this.getBackground(), this.brushDiameter);
		}

		/**
		 * Handles a mouse click on the panel. Checks whether the click hits one of the brushes,
		 * and redraws the panel and the main panel if necessary.
		 *
		 * @param mouseX is the x-coordinate of the click
		 * @param mouseY is the y-coordinate of the click
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
		 * toggles the state of the selected brush ({@link DentalScheme#selectedBrush}): if the brush was already
		 * selected, deselects it; otherwise, selects it.
		 *
		 * @param mouseX : x-coordinate of the click
		 * @param mouseY : y-coordinate of the click
		 * @param centerX : x-coordinate of the panel center (brush axis)
		 * @param centerY : y-coordinate of the panel center (not used in the current logic)
		 * @return {@code true} if the click was processed (hit a brush), otherwise {@code false}
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
					if (Objects.equals(clickedBrush, selectedBrush)) {
						log.info("Deselected brush: [ diameter: '{}' ].", clickedBrush.getDiameter());
						selectedBrush = null;
					} else {
						log.info("Selected brush: [ diameter: '{}' ].", clickedBrush.getDiameter());
						selectedBrush = clickedBrush;
					}

					return true;
				}

			}

			return false;
		}

		/**
		 * Calculates the Euclidean distance between two points.
		 *
		 * @param x1 is the x-coordinate of the first point
		 * @param y1 is the y-coordinate of the first point
		 * @param x2 is the x-coordinate of the second point
		 * @param y2 is the y-coordinate of the second point
		 * @return the distance between the points
		 */
		private double getDistance(int x1, int y1, int x2, int y2) {
			return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		}

		/**
		 * Draws all available brushes as circles arranged vertically in the center of the panel.
		 * The selected brush is additionally highlighted with a gray ring. Inside each circle,
		 * the brush diameter is displayed.
		 *
		 * @param g graphics context
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

				if (Objects.equals(brush,selectedBrush)){
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
				g2d.setFont(new Font("Arial", Font.BOLD, 14));
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

	/**
	 * The main panel displays the upper and lower jaws with teeth and spaces,
	 * as well as a comment field. Implements the {@link Printable} interface for printing.
	 * Handles clicks on teeth (toggle accessibility) and spaces
	 * (set the selected toothbrush).
	 */
	class DentalPanel extends JPanel implements Printable {
		/** Logger engine for {@link DentalPanel} */
		private final Logger log = LoggerFactory.getLogger(DentalPanel.class);
		/** Mouth model containing teeth and spaces. */
		private final Mouth mouth;
		/** Horizontal and vertical radius of the elliptical arcs for the jaws. */
		private final int radiusX = 350;
		private final int radiusY = 430;
		/** Start and end angles of the upper jaw arc (in radians). */
		private final double startAngleUpperJaw = Math.PI * 1.05;
		private final double endAngleUpperJaw = Math.PI * 1.95;
		/** Start and end angles of the lower jaw arc (in radians). */
		private final double startAngleLowerJaw = Math.PI * 0.95;
		private final double endAngleLowerJaw = Math.PI * 0.05;

		/** Tooth width and height in pixels. */
		private final int toothWidth = 45;
		private final int toothHeight = 55;
		/** Tooth click radius. */
		private final int toothRadius = 25;
		/** Basic tooth color (white). */
		private final Color basicToothColor = Color.WHITE;

		/** Space (triangle) width and height in pixels. */
		private final int spaceWidth = 15;
		private final int spaceHeight = 45;
		/** Space click radius. */
		private final int spaceRadius = 13;
		/** Array of x-coordinates of the space's base triangle (relative to the center). */
		private final int[] basicSpaceX = new int[]{0, spaceWidth, -spaceWidth};
		/**Arrays of y-coordinates of the space's outer and inner triangles. */
		private final int[] outerSpaceY = new int[]{-toothHeight / 3, -spaceHeight, -spaceHeight};
		private final int[] innerSpaceY = new int[]{toothHeight / 3, spaceHeight, spaceHeight};
		/** The space's base color (same as {@link DentalScheme#defaultBackground}). */
		private final Color basicSpaceColor = defaultBackground;


		/** Comment title text. */
		private final String comment = "Поле для комментария";
		/** Maximum number of rows in the comment text area. */
		private final int commentAreaRowLimit = 20;
		/** Maximum number of columns in the comment text area. */
		private final int commentAreaColumnLimit = 25;


		/**
		 * Creates the main panel, saves the mouth model, adds a comment text area
		 * and a click handler for teeth and spaces.
		 *
		 * @param mouth - a mouth model with teeth and spaces
		 */
		public DentalPanel(Mouth mouth) {
			this.mouth = mouth;
			this.setBackground(defaultBackground);
			this.setLayout(new GridBagLayout());

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.anchor = GridBagConstraints.CENTER;

			this.add(new CommentTextArea(commentAreaRowLimit, commentAreaColumnLimit, new Font(basicFont, Font.PLAIN, 18), defaultBackground), gbc);

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					handlePanelClick(e.getX(), e.getY());
				}
			});
			log.info("DentalPanel initialized successfully.");
			log.trace("""
							Parameters: [ mouth: '{}',
							 background: '{}',
							 layout: '{}',
							 radiusX: '{}',
							 radiusY: '{}',
							 startAngleUpperJaw: '{}',
							 endAngleUpperJaw: '{}',
							 startAngleLowerJaw: '{}',
							 endAngleLowerJaw: '{}',
							 toothWidth: '{}',
							 toothHeight: '{}',
							 toothRadius: '{}',
							 basicToothColor: '{}',
							 spaceWidth: '{}',
							 spaceHeight: '{}',
							 spaceRadius: '{}',
							 basicSpaceX: '{}',
							 outerSpaceY: '{}',
							 innerSpaceY: '{}',
							 basicSpaceColor: '{}',
							 commentAreaRowLimit: '{}',
							 commentAreaColumnLimit: '{}'].""",
					this.mouth, this.getBackground(), this.getLayout(), this.radiusX, this.radiusY, this.startAngleUpperJaw, this.endAngleUpperJaw,
					this.startAngleLowerJaw, this.endAngleLowerJaw, this.toothWidth, this.toothHeight, this.toothRadius, this.basicToothColor,
					this.spaceWidth, this.spaceHeight, this.spaceRadius, this.basicSpaceX, this.outerSpaceY, this.innerSpaceY,
					this.basicSpaceColor, this.commentAreaRowLimit, this.commentAreaColumnLimit);
		}

		/**
		 * Handles a mouse click on the panel. First, it checks the upper jaw, then the lower.
		 * If a tooth or gap is hit, it causes the panel to be redrawn.
		 *
		 * @param mouseX is the x-coordinate of the click
		 * @param mouseY is the y-coordinate of the click
		 */
		private void handlePanelClick(int mouseX, int mouseY) {
			log.debug("HandlePanelClick: [ x: '{}', y: '{}' ].", mouseX, mouseY);
			int centerX = getWidth() / 2;
			int centerY = getHeight() / 2;

			// Check Upper Jaw click targets
			if (checkJawClick(mouseX, mouseY, true, centerX, centerY - 40, startAngleUpperJaw, endAngleUpperJaw)) {
				repaint();
				return;
			}

			// Check Lower Jaw click targets
			if (checkJawClick(mouseX, mouseY, false, centerX, centerY + 40, startAngleLowerJaw, endAngleLowerJaw)) {
				repaint();
				return;
			}

			log.trace("Click at x={}, y={} did not target any tooth or space.", mouseX, mouseY);
		}

		/**
		 * Checks whether the click hits teeth and spaces on the specified jaw.
		 * If the click hits a tooth, toggles its availability. If the click hits an
		 * available space and a brush is selected, assigns that brush to the corresponding
		 * part of the space (inner or outer).
		 *
		 * @param mouseX : x-coordinate of the click
		 * @param mouseY : y-coordinate of the click
		 * @param upper : {@code true} for the upper jaw, {@code false} for the lower jaw
		 * @param centerX : x-coordinate of the center of the jaw arc
		 * @param centerY : y-coordinate of the center of the jaw arc
		 * @param startAngle : start angle of the arc (in radians)
		 * @param endAngle : end angle of the arc (in radians)
		 * @return {@code true} if the click was processed (tooth or gap), otherwise {@code false}
		 */
		private boolean checkJawClick(int mouseX, int mouseY, boolean upper,
		                              int centerX, int centerY, double startAngle, double endAngle) {

			int start = upper ? 0 : 16;
			double angleStep = (endAngle - startAngle) / 15;

			// 1. Check if the user clicked any Tooth
			for (int i = 0; i < 16; i++) {
				double currentAngle = startAngle + angleStep * (double) i;
				int x = (int) (centerX + radiusX * Math.cos(currentAngle));
				int y = (int) (centerY + radiusY * Math.sin(currentAngle));

				if (getDistance(mouseX, mouseY, x, y) <= toothRadius) {
					Tooth clickedTooth = mouth.getTooth(start + i);
					// Toggle availability state
					clickedTooth.setAvailable(!clickedTooth.isAvailable());
					log.info("Toggled Tooth FDI {} to available = {}", clickedTooth.getPosition(), clickedTooth.isAvailable());
					return true;
				}

				if (i < 15 && Objects.nonNull(selectedBrush)) {
					Space clickedSpace;
					if (upper) {
						clickedSpace = this.mouth.getSpace(start + i);
					} else {
						clickedSpace = this.mouth.getSpace(start + i - 1);
					}

					if (clickedSpace.isAvailable()) {
						double midAngle = currentAngle + angleStep / 2;
						int midX = (int) (centerX + radiusX * Math.cos(midAngle));
						int midY = (int) (centerY + radiusY * Math.sin(midAngle));

						//Y of outer triangle center
						double localOuterY = Arrays.stream(outerSpaceY).average().getAsDouble();
						//Y of inner triangle center
						double localInnerY = Arrays.stream(innerSpaceY).average().getAsDouble();


						//Rotated outer x - (midX + dx * cos(θ) - dy * sin(θ))
						int outerX = (int) Math.round(midX + basicSpaceX[0] * Math.cos(midAngle + Math.PI / 2) - localOuterY * Math.sin(midAngle + Math.PI / 2));
						// and y - (midY + dx * sin(θ) + dy * cos(θ))
						int outerY = (int) Math.round(midY + basicSpaceX[0] * Math.sin(midAngle + Math.PI / 2) + localOuterY * Math.cos(midAngle + Math.PI / 2));
						// dx = x - midX; x = localX + midX -> dx = localX + midX - midX -> dx = localX
						// dy = y - midY; y = localY + midY -> dy = localY + midY - midY -> dy = localY

						//Rotated inner x and y
						int innerX = (int) Math.round(midX - localInnerY * Math.sin(midAngle + Math.PI / 2));
						int innerY = (int) Math.round(midY + localInnerY * Math.cos(midAngle + Math.PI / 2));

						//Check if inner space was clicked
						if (getDistance(mouseX, mouseY, outerX, outerY) <= spaceRadius) {
							clickedSpace.setOuterBrush(selectedBrush);
							log.info("Assigned outer brush {} to Space position {}", selectedBrush, clickedSpace.getPosition());
							return true;
						//Check if outer space was clicked
						} else if (getDistance(mouseX, mouseY, innerX, innerY) <= spaceRadius) {
							clickedSpace.setInnerBrush(selectedBrush);
							log.info("Assigned inner brush {} to Space position {}", selectedBrush, clickedSpace.getPosition());
							return true;
						}
					}
				}
			}

			return false;
		}

		/**
		 * Calculates the Euclidean distance between two points.
		 *
		 * @param x1 is the x-coordinate of the first point
		 * @param y1 is the y-coordinate of the first point
		 * @param x2 is the x-coordinate of the second point
		 * @param y2 is the y-coordinate of the second point
		 * @return the distance between the points
		 */
		private double getDistance(int x1, int y1, int x2, int y2) {
			return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		}

		/**
		 * Renders the upper and lower jaws, as well as the labels "Upper Jaw,"
		 * "Lower Jaw," "Left," and "Right." Enables antialiasing for high-quality rendering.
		 *
		 * @param g graphics context
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
			this.drawJaw(g2d, true, centerX, centerY - 40, startAngleUpperJaw, endAngleUpperJaw);


			// Draw Lower Jaw (Angles from roughly 190 degrees to 350 degrees)
			this.drawJaw(g2d, false, centerX, centerY + 40, startAngleLowerJaw, endAngleLowerJaw);

			g2d.setFont(new Font("Arial", Font.BOLD, 25));
			g2d.setColor(Color.DARK_GRAY);

			// Define padding and metrics for jaws markers
			FontMetrics fm = g2d.getFontMetrics();
			int padding = 100;

			// Upper Jaw marker definition
			String topText = "Верхняя челюсть";
			int topTextWidth = fm.stringWidth(topText);
			int topTextX = centerX - topTextWidth / 2;
			int topTextY = centerY - radiusY - padding;
			g2d.drawString(topText, topTextX, topTextY);

			// Lower Jaw marker definition
			String bottomText = "Нижняя челюсть";
			int bottomTextWidth = fm.stringWidth(bottomText);
			int bottomTextX = centerX - bottomTextWidth / 2;
			int bottomTextY = centerY + radiusY + padding + fm.getAscent();
			g2d.drawString(bottomText, bottomTextX, bottomTextY);

			// Left side marker
			String leftText = "Лево";
			int leftTextWidth = fm.stringWidth(leftText);
			int leftTextX = centerX - radiusX - leftTextWidth / 2;
			g2d.drawString(leftText, leftTextX, centerY);

			//Right side marker
			String rightText = "Право";
			int rightTextWidth = fm.stringWidth(rightText);
			int rightTextX = centerX + radiusX - rightTextWidth / 2;
			g2d.drawString(rightText, rightTextX, centerY);
		}

		/**
		 * Draws a single jaw: all 16 teeth and spaces between them (if necessary).
		 * Spaces are drawn only if a toothbrush is selected or if a toothbrush is already placed in them.
		 *
		 * @param g2d 2D graphics context
		 * @param upper {@code true} for the upper jaw, {@code false} for the lower jaw
		 * @param centerX x-coordinate of the arc center
		 * @param centerY y-coordinate of the arc center
		 * @param startAngle start angle of the arc
		 * @param endAngle end angle of the arc
		 */
		private void drawJaw(Graphics2D g2d, boolean upper, int centerX, int centerY, double startAngle, double endAngle) {

			double angleStep = (endAngle - startAngle) / 15;
			int start = upper ? 0 : 16;

			for(int i = 0; i < 16; ++i) {
				// Retrieve the specific tooth based on our visual map[cite: 2]
				Tooth tooth = this.mouth.getTooth(start + i);

				double currentAngle = startAngle + angleStep * (double) i;

				// Calculate x and y using polar equations for an ellipse
				int x = (int)(centerX + radiusX * Math.cos(currentAngle));
				int y = (int)(centerY + radiusY * Math.sin(currentAngle));

				this.drawTooth(g2d, tooth, x, y, currentAngle);


				if (i < 15) {
					Space space;
					if (upper) {
						space = this.mouth.getSpace(start + i);
					} else {
						space = this.mouth.getSpace(start + i - 1);
					}

					if (Objects.nonNull(selectedBrush) || (Objects.nonNull(space.getOuterBrush()) || Objects.nonNull(space.getInnerBrush()))) {
						double midAngle = currentAngle + angleStep / 2;
						int midX = (int)(centerX + radiusX * Math.cos(midAngle));
						int midY = (int)(centerY + radiusY * Math.sin(midAngle));

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
		 * @param g2d 2D graphics context
		 * @param tooth tooth object
		 * @param x x-coordinate of the tooth center
		 * @param y y-coordinate of the tooth center
		 * @param angle tooth rotation angle (in radians)
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
			g2d.setColor(tooth.isAvailable()? basicToothColor : basicToothColor.darker());
			g2d.fillRoundRect(-toothWidth / 2, -toothHeight / 2, toothWidth, toothHeight, 20, 20);

			// Draw the outline
			g2d.setColor(Color.BLACK);
			g2d.setStroke(new BasicStroke(2.5F));
			g2d.drawRoundRect(-toothWidth / 2, -toothHeight / 2, toothWidth, toothHeight, 20, 20);

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
		 * @param g2d 2D graphics context
		 * @param space gap object
		 * @param x x-coordinate of the gap center
		 * @param y y-coordinate of the gap center
		 * @param angle gap rotation angle (in radians)
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
					} else  {
						return basicSpaceColor;
					}
				}
				return basicSpaceColor.darker();
			};

			log.trace("Rendering spaces '{}' between left tooth - '{}' and right tooth - '{}'.", space.getPosition(), space.getLeftTooth().getPosition(), space.getRightTooth().getPosition());

			Stroke stroke;
			//Set stroke params
			if (space.isAvailable() && Objects.isNull(space.getInnerBrush()) &&  Objects.isNull(space.getOuterBrush())) {
				stroke = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new  float[]{19.0f, 5.0f}, 0.0f);
			} else {
				stroke = new BasicStroke(2.0F);
			}

			// Draw outer space
			if (Objects.isNull(space.getInnerBrush())) {
				drawSpace(g2d, basicSpaceX, outerSpaceY, getColor.apply(space.getOuterBrush()), stroke);
			}

			// Draw inner space
			if (Objects.isNull(space.getOuterBrush())) {
				drawSpace(g2d, basicSpaceX, innerSpaceY, getColor.apply(space.getInnerBrush()), stroke);
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
		 * @param g2d 2D graphics context
		 * @param xPoints array of triangle vertex x-coordinates
		 * @param yPoints array of triangle vertex y-coordinates
		 * @param color fill color
		 * @param stroke stroke style
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
		 * @param graphics : the printer's graphics context
		 * @param pageFormat : the page format
		 * @param pageIndex : the page index (0 is the only page)
		 * @return {@link Printable#PAGE_EXISTS} if the page was successfully rendered,
		 * {@link Printable#NO_SUCH_PAGE} if a non-existent page was requested
		 * @throws PrinterException if a printing error occurs
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
}
