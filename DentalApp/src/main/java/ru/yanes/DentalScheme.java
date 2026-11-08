package ru.yanes;

import java.awt.*;

import javax.swing.*;

import ru.yanes.state.BrushSelection;
import ru.yanes.Panels.BrushPanel;
import ru.yanes.Panels.DentalPanel;
import ru.yanes.Panels.ToolPanel;
import ru.yanes.models.Mouth;

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
	 * Minimal width and height of app window ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height).
	 */
	private final int[] windowMinSize = new int[]{1600, 1300};
	/**
	 * Preferred width and height of {@code CommentPanel} ({@code commentPanelPreferSize[0]} — width, {@code commentPanelPreferSize[1]} — height).
	 *
	 * @deprecated The {@code CommentPanel} is disabled, instead the comment is embedded in the {@link DentalPanel}.
	 */
	@Deprecated
	private final int[] commentPanelPreferSize = new int[]{400, 0};
	/**
	 * Font family name which is used by default in app.
	 */
	private String basicFont;
	/**
	 * Temporary field with the index of the font family in {@link #fonts} which is used as {@link #basicFont}.
	 */
	private int count = 0;
	/**
	 * Temporary field with the list of font family names available in the system.
	 */
	private final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	/**
	 * Default background color for app. Currently - light green.
	 */
	private final Color defaultBackground = new Color(230, 240, 230);
	/**
	 * Panel which displays the available brush types and allows you to select one of them.
	 */
	private final BrushPanel brushPanel;
	/**
	 * Preferred width and height of {@link BrushPanel} ({@code brushPanelPreferSize[0]} — width, {@code brushPanelPreferSize[1]} — height).
	 */
	private final int[] brushPanelPreferSize = new int[]{400, 0};
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
	 * Preferred width and height of {@link ToolPanel} ({@code toolPanelPreferSize[0]} — width, {@code toolPanelPreferSize[1]} — height).
	 */
	private final int[] toolPanelPreferSize = new int[]{0, 30};


	/**
	 * Creates the main application window: initializes the shared state ({@link Mouth} and {@link BrushSelection}),
	 * configures window settings, creates all panels, passes the shared state to them and shows the window.
	 */
	public DentalScheme() {

		basicFont = fonts[count];
		System.out.println("Font Name: " + basicFont);

		this.setTitle("\ud83e\uddb7 Дентальная схема");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(windowMinSize[0], windowMinSize[1]));

		this.brushSelection = new BrushSelection();
		this.mouth = new Mouth();
		dentalPanel = new DentalPanel(brushSelection, mouth, defaultBackground, basicFont);
		dentalPanel.setName("dentalPanel");

		brushPanel = new BrushPanel(brushSelection, defaultBackground, brushPanelPreferSize, basicFont);
		brushPanel.setName("brushPanel");

		this.add(brushPanel, BorderLayout.EAST);

//		CommentPanel commentPanel = new CommentPanel();
//		commentPanel.setName("commentPanel");
//		this.add(commentPanel, BorderLayout.WEST);

		toolPanel = new ToolPanel(dentalPanel, defaultBackground, toolPanelPreferSize, basicFont);
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
							logger: '{}',
							windowMinSize: '{}',
							brushPanelPreferSize: '{}',
							commentPanelPreferSize: '{}',
							toolPanelPreferSize: '{}',
							basicFont: '{}',
							defaultBackground: '{}'
						].""",
				this.getTitle(), this.getDefaultCloseOperation(), this.getMinimumSize(), this.isVisible(), dentalPanel, log,
				this.windowMinSize, this.brushPanelPreferSize, this.commentPanelPreferSize, this.toolPanelPreferSize,
				this.basicFont, this.defaultBackground);
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
