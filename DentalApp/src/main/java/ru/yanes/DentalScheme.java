package ru.yanes;

import java.awt.*;

import javax.swing.*;

import ru.yanes.config.UITheme;
import ru.yanes.state.BrushSelection;
import ru.yanes.panels.BrushPanel;
import ru.yanes.panels.DentalPanel;
import ru.yanes.panels.ToolPanel;
import ru.yanes.models.Mouth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ru.yanes.config.UIConstants.WINDOW_MIN_HEIGHT;
import static ru.yanes.config.UIConstants.WINDOW_MIN_WIDTH;


/**
 * The main class of the application, representing the main window of the dental scheme.
 * <p>
 * The window contains three main panels:
 * <ol>
 *     <li>{@link DentalPanel} — displays jaws, teeth, spaces and a comment field;</li>
 *     <li>{@link BrushPanel} — displays the available brush types and allows you to select one of them;</li>
 *     <li>{@link ToolPanel} — contains tool buttons (eg print button).</li>
 * </ol>
 * The app uses the {@link Mouth} model to store the state of teeth and spaces, and the {@link BrushSelection}
 * to store the currently selected brush.
 * <p>
 * The class is the composition root of the application: it creates the shared state objects
 * ({@link Mouth}, {@link BrushSelection}) and passes them to the panels, so the panels do not depend
 * on the main window and on each other.
 *
 * @author Skorokhodov Ilia
 * @version 0.1b
 * @since 2026-08
 */
public class DentalScheme extends JFrame {
	/**
	 * Logger engine for {@link DentalScheme}.
	 */
	private static final Logger log = LoggerFactory.getLogger(DentalScheme.class);
	/**
	 * Panel which displays the available brush types and allows you to select one of them.
	 */
	private final BrushPanel brushPanel;
	/**
	 * Shared state of the currently selected brush. It is changed by the {@link BrushPanel}
	 * and read by the {@link DentalPanel}; both panels repaint themselves on every change.
	 */
	private final BrushSelection brushSelection;
	/**
	 * Panel with teeth, spaces and comment area.
	 */
	private final DentalPanel dentalPanel;
	/**
	 * Mouth model containing teeth and spaces. It is created here and passed to the {@link DentalPanel}.
	 */
	private final Mouth mouth;
	/**
	 * Panel with tool buttons.
	 */
	private final ToolPanel toolPanel;


	/**
	 * Creates the main application window: initializes the shared state ({@link Mouth} and {@link BrushSelection}),
	 * configures window settings, creates all panels, passes the shared state to them and shows the window.
	 */
	public DentalScheme() {

		this.setTitle("\ud83e\uddb7 Дентальная схема");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(WINDOW_MIN_WIDTH, WINDOW_MIN_HEIGHT));

		this.brushSelection = new BrushSelection();
		this.mouth = new Mouth();
		dentalPanel = new DentalPanel(brushSelection, mouth, UITheme.DEFAULT);
		dentalPanel.setName("dentalPanel");

		brushPanel = new BrushPanel(brushSelection, UITheme.DEFAULT);
		brushPanel.setName("brushPanel");

		this.add(brushPanel, BorderLayout.EAST);

		toolPanel = new ToolPanel(dentalPanel, UITheme.DEFAULT);
		toolPanel.setName("toolPanel");

		this.add(toolPanel, BorderLayout.NORTH);

		this.add(dentalPanel, BorderLayout.CENTER);
		this.setLocationRelativeTo(null);
		this.setVisible(true);

		log.info("DentalScheme initialized successfully.");
		log.trace("""
						Panel parameters: [
							title: '{}',
							defaultCloseOperation: '{}',
							minimumSize: '{}',
							visible: '{}'
						];
						Class fields: [
							dentalPanel: '{}',
							logger: '{}'
						].""",
				this.getTitle(), this.getDefaultCloseOperation(), this.getMinimumSize(), this.isVisible(), dentalPanel, log);
	}

	/**
	 * The entry point to the application.
	 * Sets the system look and feel and launches the graphical interface in the Swing event thread (EDT).
	 *
	 * @param args command line arguments (not used).
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

}
