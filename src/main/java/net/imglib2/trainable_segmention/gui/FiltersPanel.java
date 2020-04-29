package net.imglib2.trainable_segmention.gui;

import java.awt.Component;
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

	private List<FeatureSetting> deprecatedFilters;
	private List<FeatureSetting> basicFilters;
	private List<FeatureSetting> advancedFilters;
	private List<FiltersListSection> sections;
	private GlobalSettings globalSettings;
	private Context context;

	public FiltersPanel(Context context, FeatureSettings fs) {
		this.context = context;
		this.globalSettings = fs.globals();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initUI();
	}

	private void initUI() {
		populateFeatureSettingArrays();
		sections = new ArrayList<>();
		AccordionPanel accordion = new AccordionPanel();
		FiltersListSection basic = new FiltersListSection("Basic Filters", context, globalSettings, basicFilters, true);
		sections.add(basic);
		accordion.addSection(basic);
		FiltersListSection advanced = new FiltersListSection("Advanced Filters", context, globalSettings, this.advancedFilters, false);
		sections.add(advanced);
		accordion.addSection(advanced);
		FiltersListSection deprecated = new FiltersListSection("Deprecated Filters", context, globalSettings, this.deprecatedFilters, false);
		sections.add(deprecated);
		accordion.addSection(deprecated);
		add(accordion);
	}

	private void populateFeatureSettingArrays() {
		List<FeatureInfo> featureInfos = AvailableFeatures.getFeatures(context);
		basicFilters = new ArrayList<>();
		advancedFilters = new ArrayList<>();
		deprecatedFilters = new ArrayList<>();
		featureInfos.sort(Comparator.comparing(FeatureInfo::label));
		for (FeatureInfo featureInfo : featureInfos) {
			FeatureSetting fs = FeatureSetting.fromClass(featureInfo.clazz());
			if (featureInfo.isDeprecated())
				deprecatedFilters.add(fs);
			else if (isBasic(featureInfo.clazz()))
				basicFilters.add(fs);
			else
				advancedFilters.add(fs);
		}
	}

	private boolean isBasic(Class<? extends FeatureOp> clazz) {
		return BASIC_FILTERS.contains(clazz);
	}

	public List<FeatureSetting> getSelectedFeatureSettings() {
		List<FeatureSetting> selected = new ArrayList<>();
		Component[] children = getComponents();
		for (Component child : children) {
			if (child instanceof FiltersListSection)
				selected.addAll(((FiltersListSection) child).getSelectedFeatureSettings());
		}
		return selected;
	}

	public void setGlobalSetting(GlobalSettings globalSettings) {
		this.globalSettings = globalSettings;
		sections.forEach(section -> section.setGlobalSettings(this.globalSettings));
	}
}
