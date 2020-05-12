
package net.imglib2.trainable_segmention.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.imglib2.trainable_segmention.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gauss.GaussianBlurFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.identity.IdendityFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.stats.StatisticsFeature;
import net.imglib2.trainable_segmention.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

public class FiltersPanel extends JPanel {

	private static final Set<Class<?>> BASIC_FILTERS = new HashSet<>(Arrays.asList(
		IdendityFeature.class,
		GaussianBlurFeature.class,
		DifferenceOfGaussiansFeature.class,
		GaussianGradientMagnitudeFeature.class,
		LaplacianOfGaussianFeature.class,
		HessianEigenvaluesFeature.class,
		StructureTensorEigenvaluesFeature.class,
		StatisticsFeature.class));

	private List<FeatureInfo> deprecatedFilters;
	private List<FeatureInfo> basicFilters;
	private List<FeatureInfo> advancedFilters;
	private List<FiltersListSection> sections;
	private Context context;

	public FiltersPanel(Context context, FeatureSettings fs) {
		this.context = context;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initUI(fs);
	}

	private void initUI(FeatureSettings fs) {
		populateFeatureSettingArrays();
		sections = new ArrayList<>();
		AccordionPanel accordion = new AccordionPanel();
		accordion.addSection(newSection(fs, "Basic Filters", basicFilters, true));
		accordion.addSection(newSection(fs, "Advanced Filters", advancedFilters, false));
		accordion.addSection(newSection(fs, "Deprecated Filters", deprecatedFilters, false));
		add(accordion);
	}

	private FiltersListSection newSection(FeatureSettings fs, String title,
		List<FeatureInfo> basicFilters, boolean isExpanded)
	{
		FiltersListSection basic = new FiltersListSection(title, context, fs, basicFilters, isExpanded);
		sections.add(basic);
		return basic;
	}

	private void populateFeatureSettingArrays() {
		List<FeatureInfo> featureInfos = AvailableFeatures.getFeatures(context);
		basicFilters = new ArrayList<>();
		advancedFilters = new ArrayList<>();
		deprecatedFilters = new ArrayList<>();
		featureInfos.sort(Comparator.comparing(FeatureInfo::getName));
		for (FeatureInfo featureInfo : featureInfos) {
			if (featureInfo.isDeprecated())
				deprecatedFilters.add(featureInfo);
			else if (isBasic(featureInfo.pluginClass()))
				basicFilters.add(featureInfo);
			else
				advancedFilters.add(featureInfo);
		}
	}

	private boolean isBasic(Class<? extends FeatureOp> clazz) {
		return BASIC_FILTERS.contains(clazz);
	}

	public List<FeatureSetting> getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		for (FiltersListSection section : sections)
			selected.addAll(section.getSelectedFeatureSettings());
		return selected;
	}

	public void setGlobalSetting(GlobalSettings globalSettings) {
		sections.forEach(section -> section.setGlobalSettings(globalSettings));
	}
}
