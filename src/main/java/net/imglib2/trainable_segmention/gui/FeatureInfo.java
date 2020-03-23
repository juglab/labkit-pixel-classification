package net.imglib2.trainable_segmention.gui;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import java.lang.annotation.Annotation;

public class FeatureInfo {

	private final String label;
	private final Class<? extends FeatureOp> clazz;

	public FeatureInfo(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		this.label = getLabel(pluginInfo);
		this.clazz = pluginInfo.loadClass();
	}

	public String label() {
		return label;
	}

	public Class<? extends FeatureOp> clazz() {
		return clazz;
	}

	public boolean isDeprecated() {
		return isDeprecated(clazz);
	}

	private static boolean isDeprecated(Class<? extends FeatureOp> aClass) {
		for(Annotation annotation : aClass.getAnnotations())
			if(Deprecated.class.equals(annotation.annotationType()))
				return true;
		return false;
	}
	private static String getLabel(PluginInfo<FeatureOp> pluginInfo) throws InstantiableException {
		String label = pluginInfo.getLabel();
		return label.isEmpty() ? pluginInfo.loadClass().getSimpleName() : label;
	}
}
