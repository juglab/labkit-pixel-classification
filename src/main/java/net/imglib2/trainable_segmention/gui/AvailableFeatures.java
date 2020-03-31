
package net.imglib2.trainable_segmention.gui;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
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

	public static List<FeatureInfo> getValidFeatures(
		Context context, GlobalSettings globals)
	{
		List<FeatureInfo> list = new ArrayList<>();
		List<PluginInfo<FeatureOp>> pluginInfos = context.service(PluginService.class).getPluginsOfType(
			FeatureOp.class);
		for (PluginInfo<FeatureOp> pluginInfo : pluginInfos) {
			try {
				if (!isValid(pluginInfo, globals))
					continue;
				list.add(new FeatureInfo(pluginInfo));
			}
			catch (InstantiableException e) {
				// ignore
			}
		}
		return list;
	}


	private static boolean isValid(PluginInfo<FeatureOp> pluginInfo, GlobalSettings globals)
			throws InstantiableException
	{
		FeatureOp op = pluginInfo.createInstance();
		return op.checkGlobalSettings(globals);
	}
}
