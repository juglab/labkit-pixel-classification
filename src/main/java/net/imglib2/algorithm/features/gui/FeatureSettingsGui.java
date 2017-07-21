package net.imglib2.algorithm.features.gui;

import net.imagej.ops.*;
import net.imglib2.algorithm.features.Feature;
import net.imglib2.algorithm.features.FeatureGroup;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.algorithm.features.ops.FeatureOp;
import net.miginfocom.swing.MigLayout;
import org.scijava.AbstractContextual;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.*;
import org.scijava.ui.swing.widget.SwingInputHarvester;
import org.scijava.widget.AbstractInputHarvester;

/**
 * GUI to select a list of features and their settings.
 *
 * @author Matthias Arzt
 */
public class FeatureSettingsGui {

	private final JPanel content = new JPanel();

	private final ListModel model = new ListModel();

	private final JList<Holder> list = new JList<>();

	private final Context context = new Context();

	private final FeatureSettingsDialog featureSettingsDialog = new FeatureSettingsDialog(context);

	public FeatureSettingsGui() {
		initGui();
	}

	private void initGui() {
		list.setModel(model);
		content.setLayout(new MigLayout("insets 0", "[grow]","[grow][]"));
		content.add(new JScrollPane(list), "split 2, grow");
		JPopupMenu menu = initMenu();
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

	private JPopupMenu initMenu() {
		JPopupMenu menu = new JPopupMenu();
		List<PluginInfo<FeatureOp>> pi = context.service(PluginService.class).getPluginsOfType(FeatureOp.class);
		List<Class<? extends FeatureOp>> choices = pi.stream().map(PluginInfo::getPluginClass).collect(Collectors.toList());
		for(Class<? extends FeatureOp> choice : choices) {
			JMenuItem item = new JMenuItem(choice.getSimpleName());
			item.addActionListener(l -> addPressed(choice));
			menu.add(item);
		}
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
		model.add(new Holder(newInstance(featureClass)));
	}

	private <T extends FeatureOp> T newInstance(Class<T> tClass) {
		try {
			T t = tClass.newInstance();
			t.initialize();
			return t;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private JButton addButton(String add, Runnable runnable) {
		JButton button = new JButton(add);
		button.addActionListener(e -> runnable.run());
		return button;
	}

	public static Optional<FeatureGroup> show() {
		FeatureSettingsGui featureSettingsGui = new FeatureSettingsGui();
		return featureSettingsGui.showInternal();
	}

	private Optional<FeatureGroup> showInternal() {
		boolean ok = showResizeableOkCancelDialog("Select Pixel Features", content);
		if(ok)
			return Optional.of(model.features());
		else
			return Optional.empty();
	}

	public static boolean showResizeableOkCancelDialog(String title, JPanel content) {
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
		System.out.println(FeatureSettingsGui.show());
		System.out.println("finished");
	}

	private static final List<String> exclude = Arrays.asList("out", "in");

	class Holder {

		private Op value;

		Holder(Op value) {
			this.value = value;
		}

		void edit() {
			featureSettingsDialog.show(value);
		}

		@Override
		public String toString() {
			StringJoiner joiner = new StringJoiner(", ");
			OpInfo opInfo = new OpInfo(value.getClass());
			CommandInfo cInfo = opInfo.cInfo();
			Module module = cInfo.createModule(value);
			for(ModuleItem<?> input : cInfo.inputs())
				if(!exclude.contains(input.getName()))
				joiner.add(input.getName() + " = " + input.getValue(module));
			return value.getClass().getSimpleName() + " " + joiner;
		}

		public FeatureOp get() {
			return (FeatureOp) value;
		}
	}

	static class ListModel extends AbstractListModel<Holder> {

		List<Holder> items = new ArrayList();

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

		public FeatureGroup features() {
			return Features.group( items.stream().map(Holder::get).collect(Collectors.toList()) );
		}
	}


	static class FeatureSettingsDialog extends AbstractContextual {

		private final PluginService pluginService;

		private final AbstractInputHarvester harvester;

		FeatureSettingsDialog(Context context) {
			pluginService = context.service(PluginService.class);
			harvester = getHarvester();
		}

		private AbstractInputHarvester getHarvester() {
			List<AbstractInputHarvester> harvester = RevampUtils.filterForClass(AbstractInputHarvester.class,
					pluginService.createInstancesOfType(PreprocessorPlugin.class));
			List<SwingInputHarvester> swing = RevampUtils.filterForClass(SwingInputHarvester.class, harvester);
			return swing.isEmpty() ? harvester.get(0) : swing.get(0);
		}

		void show(Op op) {
			try {
				Module module = new OpInfo(op.getClass()).cInfo().createModule(op);
				module.resolveInput("in");
				module.resolveInput("out");
				harvester.harvest(module);
			} catch (ModuleException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
