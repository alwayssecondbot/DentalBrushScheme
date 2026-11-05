package ru.yanes;

import java.awt.*;

import javax.swing.*;

import lombok.Getter;
import lombok.Setter;
import ru.yanes.Panels.BrushPanel;
import ru.yanes.Panels.DentalPanel;
import ru.yanes.Panels.ToolPanel;
import ru.yanes.models.Brush;
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
 * The app uses the {@link Mouth} model to store the state of teeth and spaces.
 *
 * @author Skorokhodov Ilia
 * @version 0.1b
 * @since 2026-08
 */
public class DentalScheme extends JFrame {
	/** Logger engine for {@link DentalScheme}.*/
	private static final Logger log = LoggerFactory.getLogger(DentalScheme.class);
	/** Minimal width and height of app window ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height). */
	private final int[] windowMinSize = new int[]{1600, 1300};
	/** Minimal width and height of {@code CommentPanel} ({@code windowMinSize[0]} — width, {@code windowMinSize[1]} — height).
	 * @deprecated The {@code CommentPanel} is disabled, instead the comment is embedded in the {@link DentalPanel}
	 */
	private final int[] commentPanelPreferSize = new int[]{400, 0};
	/**Font family name which is used by default in app.*/
	private String basicFont;
	/**Temporary field with counter for font families*/
	private int count = 0;
	/**Temporary field with list of font family names*/
	private final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
	/**Default background color for app. Currently - light green.*/
	private final Color defaultBackground = new Color(230, 240, 230);
	/**Panel where displays the available brush types and allows you to select one of them.*/
	private final BrushPanel brushPanel;
	/** Minimal width and height of {@link BrushPanel} ({@code brushPanelPreferSize[0]} — width, {@code brushPanelPreferSize[1]} — height). */
	private final int[] brushPanelPreferSize = new int[]{400, 0};
	/** The currently selected brush. If no brush is selected, the value is {@code null}. */
	@Setter
	@Getter
	private Brush selectedBrush;
	/**Panel with teeth, spaces and comment area*/
	private final DentalPanel dentalPanel;
	/**Panel with tool buttons*/
	private final ToolPanel toolPanel;
	/** Minimal width and height of {@link ToolPanel} ({@code toolPanelPreferSize[0]} — width, {@code toolPanelPreferSize[1]} — height). */
	private final int[] toolPanelPreferSize = new int[]{0, 30};



	/**
	 * Creates the main application window and initializes the mouth model {@link Mouth},
	 * configures window settings and adds all panels.
	 */
	public DentalScheme() {

		basicFont = fonts[count];
		System.out.println("Font Name: " + basicFont);

		this.setTitle("\ud83e\uddb7 Дентальная схема");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(windowMinSize[0], windowMinSize[1]));

		dentalPanel = new DentalPanel(this, defaultBackground, basicFont);
		dentalPanel.setName("dentalPanel");

		brushPanel = new BrushPanel(this, defaultBackground, brushPanelPreferSize, basicFont);
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

}
