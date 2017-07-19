package net.imglib2.algorithm.features.gui;

import net.imagej.ops.*;
import net.imagej.ops.special.computer.AbstractUnaryComputerOp;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.algorithm.features.FeatureGroup;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.type.numeric.integer.IntType;
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

	private final JFrame frame = new JFrame();

	private final Semaphore lock = new Semaphore(0);

	private final ListModel model = new ListModel();

	private final JList<Holder> list = new JList<>();

	private final Context context = new Context();

	private final FeatureSettingsDialog featureSettingsDialog = new FeatureSettingsDialog(context);

	public FeatureSettingsGui() {
		initGui();
	}

	private void initGui() {
		list.setModel(model);
		frame.setLayout(new MigLayout("", "[grow]","[grow][]"));
		frame.add(new JScrollPane(list), "grow, wrap");
		frame.add(addButton("add", this::addPressed), "split 3");
		frame.add(addButton("remove", this::removePressed));
		frame.add(addButton("edit", this::editPressed));
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				lock.release();
			}
		});
		frame.setSize(100,100);
		frame.setVisible(true);
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

	private void addPressed() {
		List<PluginInfo<FeatureOp>> pi = context.service(PluginService.class).getPluginsOfType(FeatureOp.class);
		List<Class<? extends FeatureOp>> choices = pi.stream().map(x -> x.getPluginClass()).collect(Collectors.toList());

		Class<? extends Op> choosen = (Class<? extends Op>) JOptionPane.showInputDialog(
				frame, "Select new feature", "Add Feature",
				JOptionPane.PLAIN_MESSAGE, null, choices.toArray(), choices.get(0)
		);
		model.add(new Holder(newInstance(choosen)));
	}

	private <T> T newInstance(Class<T> tClass) {
		try {
			return tClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private JButton addButton(String add, Runnable runnable) {
		JButton button = new JButton(add);
		button.addActionListener(e -> runnable.run());
		return button;
	}

	public FeatureGroup show() {
		frame.setVisible(true);
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String... args) {
		new FeatureSettingsGui().show();
		System.out.print("finished");
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

	interface FeatureOp extends SciJavaPlugin, Op, UnaryComputerOp<IntType, IntType> {

	}

	@Plugin(type = FeatureOp.class)
	public static class Test extends AbstractUnaryComputerOp<IntType, IntType> implements FeatureOp {

		static String NAME = "TestOp";

		@Parameter
		private String input = "Hello World!";

		@Override
		public void compute(IntType input, IntType output) {
		}
	}
}
