/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import org.scijava.Context;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;

/**
 * Sub component of the AccordionPanel
 */
public class FiltersListSection extends AccordionSection {

	private List<FeatureInfo> featureInfos;
	private final JPanel titlePanel;
	private final JPanel expandablePanel;

	/**
	 */
	public FiltersListSection(String title, Context context, FeatureSettings featureSettings,
		List<FeatureInfo> featureInfos, boolean isExpanded)
	{
		super(isExpanded);
		this.featureInfos = featureInfos;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		titlePanel = createTitlePanel(title);
		add(titlePanel, BorderLayout.NORTH);
		expandablePanel = createExpandablePanel(context, featureSettings);
		add(expandablePanel, BorderLayout.CENTER);
	}

	private JPanel createTitlePanel(String title) {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BorderLayout());
		titlePanel.add(getIconButton(), BorderLayout.WEST);
		titlePanel.add(createTitleComponent(title));
		return titlePanel;
	}

	private JLabel createTitleComponent(String title) {
		JLabel titleComponent = new JLabel(title);
		Font f = titleComponent.getFont();
		titleComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		titleComponent.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2),
			titleComponent.getBorder()));
		return titleComponent;
	}

	private JPanel createExpandablePanel(Context context, FeatureSettings featureSettings) {
		JPanel expandablePanel = new JPanel();
		expandablePanel.setLayout(new BoxLayout(expandablePanel, BoxLayout.Y_AXIS));
		for (FeatureInfo featureInfo : featureInfos) {
			JPanel row = (featureInfo.hasParameters()) ? new ParametrizedRow(context, featureInfo,
				featureSettings) : new NonParametrizedRow(featureInfo, featureSettings);
			expandablePanel.add(row);
		}
		return expandablePanel;
	}

	@Override
	protected int preferredCollapsedHeight() {
		return titlePanel.getPreferredSize().height;
	}

	@Override
	protected int preferredExpandedHeight() {
		return titlePanel.getPreferredSize().height + expandablePanel.getPreferredSize().height;
	}

	public List<FeatureSetting> getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		Component[] children = expandablePanel.getComponents();
		for (Component child : children) {
			if (child instanceof SelectableRow)
				selected.addAll(((SelectableRow) child).getSelectedFeatureSettings());
		}
		return selected;
	}

	public void setGlobalSettings(GlobalSettings globalSettings) {
		Component[] children = expandablePanel.getComponents();
		for (Component child : children) {
			if (child instanceof SelectableRow)
				((SelectableRow) child).setGlobalSettings(globalSettings);
		}
	}
}
