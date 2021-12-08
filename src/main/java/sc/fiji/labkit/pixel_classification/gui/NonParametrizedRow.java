/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;

public class NonParametrizedRow extends JPanel implements SelectableRow {

	private static final ImageIcon INFO_ICON = IconResources.getIcon("info_icon_16px.png");

	private FeatureInfo featureInfo;

	private JCheckBox checkbox;

	public NonParametrizedRow(FeatureInfo featureInfo, FeatureSettings featureSettings) {
		this.featureInfo = featureInfo;
		initUI(isSelected(featureInfo, featureSettings));
		setGlobalSettings(featureSettings.globals());
	}

	private static boolean isSelected(FeatureInfo featureInfo, FeatureSettings featureSettings) {
		return featureSettings.features().stream().anyMatch(f -> featureInfo.pluginClass().equals(f
			.pluginClass()));
	}

	private void initUI(boolean selected) {

		setLayout(new BorderLayout());
		add(Box.createHorizontalStrut(30), BorderLayout.WEST);
		checkbox = new JCheckBox(featureInfo.getName());
		checkbox.setSelected(selected);
		add(checkbox, BorderLayout.CENTER);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		btnPanel.add(createInfoButton());
		//add(btnPanel, BorderLayout.LINE_END);
		this.setPreferredSize(new Dimension(getPreferredSize().width, btnPanel.getPreferredSize().height));
	}

	private JButton createInfoButton() {
		JButton infoButton = new JButton(INFO_ICON);
		infoButton.setFocusPainted(false);
		infoButton.setMargin(new Insets(0, 0, 0, 0));
		infoButton.setContentAreaFilled(false);
		infoButton.setBorderPainted(false);
		infoButton.setOpaque(false);
		infoButton.setToolTipText("Filter information");
		infoButton.addActionListener(this::showInfoDialog);
		return infoButton;
	}

	private void showInfoDialog(ActionEvent e) {
		InfoDialog docoDiag = new InfoDialog(this, "example",
			"If you use this filter you will do great things");
		docoDiag.setVisible(true);
	}

	@Override
	public List<FeatureSetting> getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		if (checkbox.isSelected())
			selected.add(new FeatureSetting(featureInfo.pluginClass()));
		return selected;
	}

	@Override
	public void setGlobalSettings(GlobalSettings globalSettings) {
		try {
			boolean isValid = featureInfo.pluginClass().newInstance().checkGlobalSettings(globalSettings);
			enableRecursively(this, isValid);
		}
		catch (InstantiationException | IllegalAccessException ignored) {}
	}

	private static void enableRecursively(Component component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof JPanel) {
			JPanel panel = (JPanel) component;
			for (Component child : panel.getComponents())
				enableRecursively(child, enabled);
		}
	}
}
