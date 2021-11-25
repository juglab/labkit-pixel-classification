
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
