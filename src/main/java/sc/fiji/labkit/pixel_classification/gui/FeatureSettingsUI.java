
package sc.fiji.labkit.pixel_classification.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import sc.fiji.labkit.pixel_classification.gson.GsonUtils;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.SingleFeatures;
import org.scijava.Context;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.ChannelSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import net.miginfocom.swing.MigLayout;

/**
 * UI to select a list of features and their settings.
 *
 * @author turekg
 */
public class FeatureSettingsUI extends JPanel {

	private GlobalsPanel globalsPanel;

	private FiltersPanel filtersPanel;

	public FeatureSettingsUI(Context context, FeatureSettings featureSettings) {
		setLayout(new MigLayout("insets 0", "[grow]", "[][grow]"));
		globalsPanel = new GlobalsPanel(featureSettings.globals());
		add(globalsPanel, "wrap");
		filtersPanel = new FiltersPanel(context, featureSettings);
		add(new JScrollPane(filtersPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), "split 2, grow");
	}

	public FeatureSettings get() {
		return new FeatureSettings(globalsPanel.get(), filtersPanel.getSelectedFeatureSettings());
	}

	public static Optional<FeatureSettings> show(Context context, FeatureSettings featureSettings) {
		FeatureSettingsUI ui = new FeatureSettingsUI(context, featureSettings);
		boolean ok = showResizeableOkCancelDialog("Select Pixel Features", ui);
		if (ok) {
			FeatureSettings features = ui.get();
			return Optional.of(features);
		}
		return Optional.empty();
	}

	private static boolean showResizeableOkCancelDialog(String title, JPanel content) {
		JDialog dialog = new JDialog((Frame) null, title, true);
		JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE,
			JOptionPane.OK_CANCEL_OPTION);
		dialog.setContentPane(optionPane);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setPreferredSize(new Dimension(800, 650));
		optionPane.addPropertyChangeListener(e -> {
			String prop = e.getPropertyName();
			if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(
				prop)))
				dialog.dispose();
		});
		dialog.pack();
		dialog.setVisible(true);
		return optionPane.getValue().equals(JOptionPane.OK_OPTION);
	}

	public static void main(String... args) {
		FeatureSettings featureSettings = new FeatureSettings(GlobalSettings.default2d().build(),
			SingleFeatures.identity(),
			SingleFeatures.differenceOfGaussians(3, 4.5),
			GroupedFeatures.hessian());
		Optional<FeatureSettings> result = FeatureSettingsUI.show(new Context(), featureSettings);
		System.out.println(result.map(f -> GsonUtils.toString(f.toJson())));
	}

	public class GlobalsPanel extends JPanel {

		private final ChannelSetting channelSetting;
		private final JComboBox<String> dimensionsField;
		private final JFormattedTextField sigmasField;

		public GlobalsPanel(GlobalSettings globalSettings) {
			channelSetting = globalSettings.channelSetting();
			setLayout(new MigLayout("insets 0", "[]20pt[200pt]", "[][][]"));
			add(new JLabel("Dimensions:"));
			dimensionsField = new JComboBox<>(new String[] { "2D", "3D" });
			dimensionsField.setSelectedItem(globalSettings.numDimensions() + "D");
			dimensionsField.addActionListener(this::dimensionsChanged);
			add(dimensionsField, "wrap");
			add(new JLabel("List of sigmas:"));
			sigmasField = new JFormattedTextField(new ListOfDoubleFormatter());
			sigmasField.setColumns(50);
			sigmasField.setValue(globalSettings.sigmas());
			add(sigmasField, "grow, wrap");
		}

		@SuppressWarnings("unchecked")
		GlobalSettings get() {
			return GlobalSettings.default2d().channels(channelSetting).dimensions(dimensionsField
				.getSelectedIndex() + 2).sigmas((List<Double>) sigmasField.getValue()).build();
		}

		private void dimensionsChanged(ActionEvent e) {
			filtersPanel.setGlobalSetting(get());
		}
	}

	private static class ListOfDoubleFormatter extends JFormattedTextField.AbstractFormatter {

		@Override
		public Object stringToValue(String text) throws ParseException {
			return Stream.of(text.split(";")).map(Double::new).collect(Collectors.toList());
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if (value == null)
				return "";
			@SuppressWarnings("unchecked")
			List<Double> list = (List<Double>) value;
			StringJoiner joiner = new StringJoiner("; ");
			list.stream().map(Object::toString).forEach(joiner::add);
			return joiner.toString();
		}
	}
}
