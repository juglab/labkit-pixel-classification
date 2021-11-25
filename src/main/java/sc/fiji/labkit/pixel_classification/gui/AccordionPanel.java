
package sc.fiji.labkit.pixel_classification.gui;

import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;

/**
 * UI Component that contains expandable (accordion) sections
 * 
 * @author turekg
 */
public class AccordionPanel extends JPanel {

	public AccordionPanel() {
		setLayout(new MigLayout("", "[grow]", ""));
		setFocusable(false);
	}

	public void addSection(AccordionSection newSection) {
		add(newSection, "grow, wrap");
	}
}
