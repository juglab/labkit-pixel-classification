package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;

import org.scijava.Context;


import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

/**
 * Sub component of the AccordionPanel
 */
public class FiltersListSection extends AccordionSection {

	private List<FeatureSetting> featureSettings;
	private final JPanel titlePanel;
	private final JPanel expandablePanel;

	/**
	 */
	public FiltersListSection(String title, Context context, GlobalSettings gs, List<FeatureSetting> featureSettings, boolean isExpanded) {
		super(isExpanded);
		this.featureSettings = featureSettings;
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );

		titlePanel = createTitlePanel(title);
		add(titlePanel, BorderLayout.NORTH );
		expandablePanel = createExpandablePanel(context, gs);
		add( expandablePanel, BorderLayout.CENTER );
	}

	private JPanel createTitlePanel(String title) {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new BorderLayout());
		titlePanel.add(getIconButton(), BorderLayout.WEST);
		titlePanel.add(createTitleComponent(title));
		return titlePanel;
	}

	private JLabel createTitleComponent(String title) {
		JLabel titleComponent = new JLabel(title);
		Font f = titleComponent.getFont();
		titleComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		titleComponent.setBorder( new CompoundBorder( BorderFactory.createEmptyBorder( 2, 8, 2, 2 ), titleComponent.getBorder() ) );
		return titleComponent;
	}

	private JPanel createExpandablePanel(Context context, GlobalSettings gs) {
		JPanel expandablePanel = new JPanel();
		expandablePanel.setLayout( new BoxLayout(expandablePanel, BoxLayout.Y_AXIS) );
		for (FeatureSetting fs: featureSettings) {
			if (fs.parameters().isEmpty())
			{
				expandablePanel.add( new NonParametrizedRow(gs, fs) );
			} else {
				expandablePanel.add( new ParametrizedRow(context, gs, fs) );
			}
		}
		return expandablePanel;
	}

	@Override
	protected int preferredCollapsedHeight() {
		return titlePanel.getPreferredSize().height;
	}

	@Override
	protected int preferredExpandedHeight() {
		return titlePanel.getPreferredSize().height + expandablePanel.getPreferredSize().height;
	}

	public List<FeatureSetting> getSelectedFeatureSettings()
	{
		List<FeatureSetting> selected = new ArrayList<>();
		Component[] children = expandablePanel.getComponents();
		for (Component child : children) {
			if (child instanceof SelectableRow)
				selected.addAll( ((SelectableRow)child).getSelectedFeatureSettings());
		}
		return selected;
	}

	public void setGlobalSettings(GlobalSettings globalSettings) {
		Component[] children = expandablePanel.getComponents();
		for (Component child : children) {
			if (child instanceof SelectableRow)
				((SelectableRow)child).setGlobalSettings(globalSettings);
		}
	}
}
