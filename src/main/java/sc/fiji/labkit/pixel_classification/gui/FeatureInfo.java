
package sc.fiji.labkit.pixel_classification.gui;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import java.lang.annotation.Annotation;

public class FeatureInfo {

	private final String label;
	private final Class<? extends FeatureOp> clazz;
	private final boolean hasParameters;

	public FeatureInfo(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		this.label = getLabel(pluginInfo);
		this.clazz = pluginInfo.loadClass();
		this.hasParameters = !new FeatureSetting(clazz).parameters().isEmpty();
	}

	public String getName() {
		return label;
	}

	public Class<? extends FeatureOp> pluginClass() {
		return clazz;
	}

	public boolean hasParameters() {
		return hasParameters;
	}

	public boolean isDeprecated() {
		return isDeprecated(clazz);
	}

	private static boolean isDeprecated(Class<? extends FeatureOp> aClass) {
		for (Annotation annotation : aClass.getAnnotations())
			if (Deprecated.class.equals(annotation.annotationType()))
				return true;
		return false;
	}

	private static String getLabel(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		String label = pluginInfo.getLabel();
		return label.isEmpty() ? pluginInfo.loadClass().getSimpleName() : label;
	}
}
