
package sc.fiji.labkit.pixel_classification.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class AccordionSection extends JPanel {

	private static final ImageIcon EXPANDED_ICON = IconResources.getIcon("arrow_down_48px.png");
	private static final ImageIcon COLLAPSED_ICON = IconResources.getIcon("arrow_right_48px.png");
	private static final int MIN_COMPONENT_WIDTH = 200;
	private static final int PREFERRED_COMPONENT_WIDTH = 500;
	private final JButton iconButton;
	private boolean expanded = true;

	public AccordionSection(boolean isExpanded) {
		this.expanded = isExpanded;
		this.iconButton = createArrowButten();
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(MIN_COMPONENT_WIDTH, currentHeight());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(PREFERRED_COMPONENT_WIDTH, currentHeight());
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, currentHeight());
	}

	private int currentHeight() {
		return expanded ? preferredExpandedHeight() : preferredCollapsedHeight();
	}

	protected JButton getIconButton() {
		return iconButton;
	}

	protected abstract int preferredCollapsedHeight();

	protected abstract int preferredExpandedHeight();

	private JButton createArrowButten() {
		JButton iconButton = new JButton(currentIcon());
		iconButton.setFocusPainted(false);
		iconButton.setMargin(new Insets(0, 0, 0, 0));
		iconButton.setContentAreaFilled(false);
		iconButton.setBorderPainted(false);
		iconButton.setOpaque(false);
		iconButton.addActionListener(this::toggle);
		add(iconButton, BorderLayout.CENTER);
		return iconButton;
	}

	private void toggle(ActionEvent e) {
		expanded = !expanded;
		iconButton.setIcon(currentIcon());
		revalidate();
		repaint();
	}

	private ImageIcon currentIcon() {
		return expanded ? EXPANDED_ICON : COLLAPSED_ICON;
	}
}
