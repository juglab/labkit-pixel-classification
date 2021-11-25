
package sc.fiji.labkit.pixel_classification.gui;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
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
