package net.imglib2.algorithm.features.gui;

import net.imagej.ops.OpService;
import net.imglib2.algorithm.features.FeatureSettings;
import net.imglib2.algorithm.features.GlobalSettings;
import net.imglib2.algorithm.features.GroupedFeatures;
import org.scijava.Context;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsGuiTest {

	public static void main(String... args) throws InterruptedException {
		Context context = new Context(OpService.class);
		GlobalSettings settings = GlobalSettings.defaultSettings();
		FeatureSettings fs = new FeatureSettings(settings, GroupedFeatures.gauss());
		FeatureSettingsGui gui = new FeatureSettingsGui(context, fs);
		showFrame(gui.getComponent());
		System.out.println(gui.get().toJson());
	}

	private static void showFrame(JComponent component) throws InterruptedException {
		JFrame frame = new JFrame("Change Feature Settings Demo");
		frame.add(component);
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		while(frame.isVisible()) {
			Thread.sleep(100);
		}
	}
}
