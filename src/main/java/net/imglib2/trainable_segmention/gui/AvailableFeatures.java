
package net.imglib2.trainable_segmention.gui;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AvailableFeatures {

	Context context;

	AvailableFeatures(Context context) {
		this.context = context;
	}

	public static Map<String, Class<? extends FeatureOp>> getMap(Context context,
		GlobalSettings globals)
	{
		Map<String, Class<? extends FeatureOp>> map = new TreeMap<>();
		List<PluginInfo<FeatureOp>> pi = context.service(PluginService.class).getPluginsOfType(
			FeatureOp.class);
		for (PluginInfo<FeatureOp> pluginInfo : pi) {
			try {
				if (!isValid(pluginInfo, globals))
					continue;
				map.put(getLabel(pluginInfo), pluginInfo.loadClass());
			}
			catch (InstantiableException e) {
				// ignore
			}
		}
		return map;
	}

	private static boolean isValid(PluginInfo<FeatureOp> pluginInfo, GlobalSettings globals)
		throws InstantiableException
	{
		FeatureOp op = pluginInfo.createInstance();
		return op.checkGlobalSettings(globals);
	}

	private static String getLabel(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		String label = pluginInfo.getLabel();
		return label.isEmpty() ? pluginInfo.loadClass().getSimpleName() : label;
	}

}
