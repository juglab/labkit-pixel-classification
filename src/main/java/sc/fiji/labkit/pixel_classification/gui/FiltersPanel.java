/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.dog2.DifferenceOfGaussiansFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gauss.GaussianBlurFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.gradient.GaussianGradientMagnitudeFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.hessian.HessianEigenvaluesFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.identity.IdentityFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.laplacian.LaplacianOfGaussianFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MaxFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MeanFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.MinFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.stats.VarianceFeature;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.structure.StructureTensorEigenvaluesFeature;
import org.scijava.Context;

import sc.fiji.labkit.pixel_classification.pixel_feature.filter.FeatureOp;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSetting;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;

public class FiltersPanel extends JPanel {

	private static final List<Class<?>> BASIC_FILTERS = Arrays.asList(
		IdentityFeature.class,
		GaussianBlurFeature.class,
		DifferenceOfGaussiansFeature.class,
		GaussianGradientMagnitudeFeature.class,
		LaplacianOfGaussianFeature.class,
		HessianEigenvaluesFeature.class,
		StructureTensorEigenvaluesFeature.class,
		MinFeature.class,
		MaxFeature.class,
		MeanFeature.class,
		VarianceFeature.class);

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
		accordion.addSection(newSection(fs, "Customizable Filters", advancedFilters, false));
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
		basicFilters.sort(Comparator.comparing(f -> BASIC_FILTERS.indexOf(f.pluginClass())));
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
