package net.imglib2.algorithm.features.gui;

import net.imagej.ops.OpService;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.GlobalSettings;
import net.imglib2.algorithm.features.GroupedFeatures;
import net.imglib2.algorithm.features.gson.FeaturesGson;
import org.scijava.Context;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsGuiTest {

	public static void main(String... args) throws InterruptedException {
		OpService ops = new Context(OpService.class).service(OpService.class);
		GlobalSettings settings = GlobalSettings.defaultSettings();
		FeatureGroup fg = Features.grayGroup(new GroupedFeatures(ops, settings).gauss());
		FeatureSettingsGui gui = new FeatureSettingsGui(ops, fg);
		showFrame(gui.getComponent());
		System.out.println(FeaturesGson.toJson(fg));
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
