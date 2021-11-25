
package net.imglib2.trainable_segmentation.gui;

import java.util.List;

import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmentation.pixel_feature.settings.GlobalSettings;

public interface SelectableRow {

	public List<FeatureSetting> getSelectedFeatureSettings();

	void setGlobalSettings(GlobalSettings featureInfo);
}
