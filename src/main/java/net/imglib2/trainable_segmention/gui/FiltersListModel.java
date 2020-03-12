package net.imglib2.trainable_segmention.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;

public class FiltersListModel extends AbstractListModel<FiltersListRow> {

	private static final long serialVersionUID = 1L;
	private final List<FiltersListRow> rows;
	private final List<FeatureSetting> features;

	public FiltersListModel() {
		this.rows = new ArrayList<>();
		this.features = new ArrayList<>();
	}

	@Override
	public int getSize() {
		return rows.size();
	}

	@Override
	public FiltersListRow getElementAt(int index) {
		return rows.get(index);
	}

	public void add(FiltersListRow row) {
		features.add( row.getFeatureSetting() );
		rows.add(row);
		update();
	}

	public void remove(int index) {
		features.remove(index);
		rows.remove(index);
		update();
	}

	public void update() {
		fireContentsChanged(rows, 0, rows.size());
	}
	
	public List<FeatureSetting> getSelectedFeatureSettings() {
		List<FeatureSetting> selectedFeatures = new ArrayList<>();
		for (FiltersListRow item: rows) {
			if (item.isSelected())
				selectedFeatures.add( item.getFeatureSetting());
		}
		return selectedFeatures;
	}
	
	public List<FeatureSetting> getFeatureSettings() {
		return features;
	}

	public void removeAll(List<FeatureSetting> invalids) {
		for (FeatureSetting invalid: invalids) {
			int index = features.indexOf(invalid);
			if (index > -1) {
				features.remove(index);
				rows.remove(index);
			}
		}
		update();
	}
}
