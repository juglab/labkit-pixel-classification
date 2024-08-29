/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
