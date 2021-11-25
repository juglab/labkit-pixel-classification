
package sc.fiji.labkit.pixel_classification.gui;

import java.util.List;

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;

public interface SelectableRow {

	public List<FeatureSetting> getSelectedFeatureSettings();

	void setGlobalSettings(GlobalSettings featureInfo);
}
