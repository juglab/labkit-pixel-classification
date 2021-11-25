
package sc.fiji.labkit.pixel_classification.gui;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;

public class AvailableFeatures {

	private AvailableFeatures() {
		// prevent from instantiation
	}

	public static List<FeatureInfo> getFeatures(
		Context context)
	{
		List<FeatureInfo> list = new ArrayList<>();
		List<PluginInfo<FeatureOp>> pluginInfos = context.service(PluginService.class).getPluginsOfType(
			FeatureOp.class);
		for (PluginInfo<FeatureOp> pluginInfo : pluginInfos) {
			try {
				list.add(new FeatureInfo(pluginInfo));
			}
			catch (InstantiableException e) {
				// ignore
			}
		}
		return list;
	}
}
