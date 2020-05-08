
package net.imglib2.trainable_segmention.gui;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsUITest {

	public static void main(String... args) throws InterruptedException {
		Context context = SingletonContext.getInstance();
		GlobalSettings settings = GlobalSettings.default2d().build();
		FeatureSettings fs = new FeatureSettings(settings, GroupedFeatures.gauss());
		FeatureSettingsUI gui = new FeatureSettingsUI(context, fs);
		showFrame(gui);
		System.out.println(gui.get().toJson());
	}

	private static void showFrame(JComponent component) throws InterruptedException {
		JFrame frame = new JFrame("Change Feature Settings Demo");
		frame.add(component);
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		while (frame.isVisible()) {
			Thread.sleep(100);
		}
	}
}
