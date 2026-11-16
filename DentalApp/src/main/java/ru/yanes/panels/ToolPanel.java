package ru.yanes.panels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yanes.config.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import static ru.yanes.config.UIConstants.TOOL_PANEL_PREFERRED_HEIGHT;
import static ru.yanes.config.UIConstants.TOOL_PANEL_PREFERRED_WIDTH;

/**
 * A toolbar containing action buttons (for example, printing a diagram).
 * Located at the top of the window.
 */
public class ToolPanel extends JPanel {
	/**
	 * Link to the main panel {@link DentalPanel} that will be printed.
	 */
	private final DentalPanel frameToPrint;
	/**
	 * Logger engine.
	 */
	private final Logger log = LoggerFactory.getLogger(ToolPanel.class);
	/** Font which is used for the tool buttons. */
	private final UITheme theme;

	/**
	 * Creates a toolbar and adds a print button to it.
	 * The preferred size is taken from {@link ru.yanes.config.UIConstants#TOOL_PANEL_PREFERRED_WIDTH}
	 * and {@link ru.yanes.config.UIConstants#TOOL_PANEL_PREFERRED_HEIGHT}.
	 *
	 * @param dentalPanel the {@link DentalPanel} panel to print;
	 * @param background background color of the panel;
	 * @param font font family name of the panel.
	 */
	public ToolPanel(DentalPanel dentalPanel, UITheme theme) {
		this.frameToPrint = dentalPanel;
		this.theme = theme;

		this.setPreferredSize(new Dimension(TOOL_PANEL_PREFERRED_WIDTH, TOOL_PANEL_PREFERRED_HEIGHT));
		this.setBackground(theme.backgroundColor());
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setAlignmentY(TOP_ALIGNMENT);

		JButton printButton = getPrintButton();
		printButton.setName("printButton");
		this.add(printButton);

		log.info("Tool panel initialized successfully.");
		log.trace("""
						Panel parameters: [
							preferredSize: '{}',
							background: '{}',
							layout: '{}',
							alignmentY: '{}'
						];
						Class fields: [
							log: '{}',
							frameToPrint: '{}'
							theme: '{}'
						].""",
				this.getPreferredSize(), this.getBackground(), this.getLayout(), this.getAlignmentY(), this.log, this.frameToPrint, this.theme);
	}

	/**
	 * Creates and returns a "Print" button. When clicked, a print dialog opens,
	 * after confirmation, the {@link DentalPanel} is printed.
	 *
	 * @return button with a configured print handler.
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
	 * @param text text on the button;
	 * @param actionListener click handler;
	 * @return a ready-made button with a customized font, size, and handler.
	 */
	private JButton getToolButton(String text, ActionListener actionListener) {
		log.debug("Creating tool button with label: '{}'", text);
		JButton button = new JButton(text);
		button.setFont(new Font(theme.panelMainFontFamily(), Font.PLAIN, 18));
		button.setPreferredSize(new Dimension(100, 30));
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
