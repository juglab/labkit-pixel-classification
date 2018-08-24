package net.imglib2.trainable_segmention.gui;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.imglib2.trainable_segmention.pixel_feature.settings.ChannelSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.miginfocom.swing.MigLayout;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.widget.InputHarvester;

/**
 * GUI to select a list of features and their settings.
 *
 * @author Matthias Arzt
 */
public class FeatureSettingsGui {

	private final JPanel content = new JPanel();

	private final ListModel model;

	private final JList<Holder> list = new JList<>();

	private final Context context;

	private final FeatureSettingsDialog featureSettingsDialog;

	private GlobalsPanel globalsPanel;

	public FeatureSettingsGui(Context context, FeatureSettings fs) {
		this.context = context;
		featureSettingsDialog = new FeatureSettingsDialog(this.context);
		List<Holder> init = fs.features().stream().map(f -> new Holder(f)).collect(Collectors.toList());
		model = new ListModel(init);
		initGui(fs.globals());
	}

	public JComponent getComponent() {
		return content;
	}

	public FeatureSettings get() {
		return new FeatureSettings(globalsPanel.get(), model.features());
	}

	private void initGui(GlobalSettings globals) {
		list.setModel(model);
		content.setLayout(new MigLayout("insets 0", "[grow]","[][grow][]"));
		globalsPanel = new GlobalsPanel(globals);
		content.add(globalsPanel, "wrap");
		content.add(new JScrollPane(list), "split 2, grow");
		JPopupMenu menu = initMenu(globals);
		JButton addButton = new JButton("add");
		addButton.addActionListener(a ->
			menu.show(addButton, 0, addButton.getHeight()));
		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new MigLayout("insets 0", "[]","[][][][grow]"));
		sidePanel.add(addButton, "grow, wrap");
		sidePanel.add(addButton("remove", this::removePressed), "grow, wrap");
		sidePanel.add(addButton("edit", this::editPressed), "grow, wrap");
		content.add(sidePanel, "top, wrap");
	}

	private static class GlobalsPanel extends JPanel {

		private final ChannelSetting channelSetting;

		private final int numDimensions;

		private final JFormattedTextField sigmasField;

		private final JFormattedTextField thicknessField;

		public GlobalsPanel(GlobalSettings globalSettings) {
			channelSetting = globalSettings.channelSetting();
			numDimensions = globalSettings.numDimensions();
			setLayout(new MigLayout("insets 0", "[]20pt[100pt]", "[][]"));
			add(new JLabel("Radii:"));
			sigmasField = new JFormattedTextField(new ListOfDoubleFormatter());
			sigmasField.setValue(globalSettings.radii());
			add(sigmasField, "grow, wrap");
			add(new JLabel("Membrane Thickness:"));
			thicknessField = new JFormattedTextField(globalSettings.membraneThickness());
			add(thicknessField, "grow, wrap");
		}

		GlobalSettings get() {
			return GlobalSettings.default2d()
					.channels(channelSetting)
					.dimensions(numDimensions)
					.radii((List<Double>) sigmasField.getValue())
					.membraneThickness((Double) thicknessField.getValue())
					.build();
		}
	}

	private JPopupMenu initMenu(GlobalSettings globals) {
		JPopupMenu menu = new JPopupMenu();
		Map<String, Class<? extends FeatureOp>> features = AvailableFeatures.getMap(context, globals);
		features.forEach((label, featureClass) -> {
			JMenuItem item = new JMenuItem(label);
			item.addActionListener(l -> addPressed(featureClass));
			menu.add(item);
		});
		return menu;
	}

	private void removePressed() {
		int index = list.getSelectedIndex();
		if(index < 0)
			return;
		model.remove(index);
		model.update();
	}

	private void editPressed() {
		int index = list.getSelectedIndex();
		if(index < 0)
			return;
		model.getElementAt(index).edit();
		model.update();
	}

	private void addPressed(Class<? extends FeatureOp> featureClass) {
		model.add(new Holder(featureClass));
	}

	private JButton addButton(String add, Runnable runnable) {
		JButton button = new JButton(add);
		button.addActionListener(e -> runnable.run());
		return button;
	}

	public static Optional<FeatureSettings> show(Context context, FeatureSettings fg) {
		FeatureSettingsGui featureSettingsGui = new FeatureSettingsGui(context, fg);
		return featureSettingsGui.showInternal();
	}

	private Optional<FeatureSettings> showInternal() {
		boolean ok = showResizeableOkCancelDialog("Select Pixel Features", content);
		if(ok) {
			FeatureSettings features = get();
			return Optional.of(features);
		} else
			return Optional.empty();
	}

	private static boolean showResizeableOkCancelDialog(String title, JPanel content) {
		JDialog dialog = new JDialog((Frame) null, title, true);
		JOptionPane optionPane = new JOptionPane(content, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog.setContentPane(optionPane);
		dialog.setResizable(true);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		optionPane.addPropertyChangeListener( e -> {
				String prop = e.getPropertyName();
				if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop)))
					dialog.dispose();
		});
		dialog.pack();
		dialog.setVisible(true);
		return optionPane.getValue().equals(JOptionPane.OK_OPTION);
	}

	public static void main(String... args) {
		Context context = new Context();
		System.out.println(FeatureSettingsGui.show(context, new FeatureSettings(GlobalSettings.default2d().build())));
		System.out.println("finished");
	}

	private class Holder {

		private FeatureSetting value;

		public Holder(Class<? extends FeatureOp> featureClass) {
			this.value = FeatureSetting.fromClass(featureClass);
		}

		public Holder(FeatureSetting f) {
			this.value = f;
		}

		void edit() {
			value = featureSettingsDialog.show(value, globalsPanel.get());
		}

		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(", ");
			for(String parameter : value.parameters())
				joiner.add(parameter + " = " + value.getParameter(parameter));
			return value.getName() + " " + joiner;
		}

		public FeatureSetting get() {
			return value;
		}
	}

	static class ListModel extends AbstractListModel<Holder> {

		private final List<Holder> items;

		public ListModel(List<Holder> items) {
			this.items = new ArrayList(items);
		}

		@Override
		public int getSize() {
			return items.size();
		}

		@Override
		public Holder getElementAt(int index) {
			return items.get(index);
		}

		public void add(Holder holder) {
			items.add(holder);
			update();
		}

		public void remove(int index) {
			items.remove(index);
			update();
		}

		public void update() {
			fireContentsChanged(items, 0, items.size());
		}

		public List<FeatureSetting> features() {
			return items.stream().map(h -> h.get()).collect(Collectors.toList());
		}
	}

	static class FeatureSettingsDialog extends AbstractContextual {

		private final InputHarvester harvester;

		FeatureSettingsDialog(Context context) {
			harvester = new SwingInputHarvester();
			context.inject(harvester);
		}

		FeatureSetting show(FeatureSetting op, GlobalSettings globalSetting) {
			try {
				Module module = op.asModule(globalSetting);
				harvester.harvest(module);
				return FeatureSetting.fromModule(module);
			} catch (ModuleCanceledException e) {
				return op;
			} catch (ModuleException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static class ListOfDoubleFormatter extends JFormattedTextField.AbstractFormatter {

		@Override
		public Object stringToValue(String text) throws ParseException {
			return Stream.of(text.split(";")).map(Double::new).collect(Collectors.toList());
		}

		@Override
		public String valueToString(Object value) throws ParseException {
			if(value == null)
				return "";
			@SuppressWarnings("unchecked")
			List<Double> list = (List<Double>) value;
			StringJoiner joiner = new StringJoiner("; ");
			list.stream().map(Object::toString).forEach(joiner::add);
			return joiner.toString();
		}
	}
}
