package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

import org.scijava.Context;


import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSetting;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

/**
 * Sub component of the AccordionPanel
 */
public class FiltersListSection extends AccordionSection {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon EXPANDED_ICON = IconResources.getIcon( "arrow_down_48px.png" );
	private static final ImageIcon COLLAPSED_ICON = IconResources.getIcon( "arrow_right_48px.png" );

	private List<FeatureSetting> featureSettings;
	private IconPanel iconPanel;
	private JLabel titleComponent;
	private JPanel expandablePanel;

	public enum AccordionControlIcons {

		EXPANDED( EXPANDED_ICON ), COLLAPSED( COLLAPSED_ICON );

		private ImageIcon icon;

		private AccordionControlIcons( ImageIcon icon ) {
			this.icon = icon;
		}

		public Icon getIcon() {
			return this.icon;
		}
	}

	/**
	 */
	public FiltersListSection(String title, Context context, GlobalSettings gs, List<FeatureSetting> featureSettings, boolean isExpanded) {
		this.featureSettings = featureSettings;
		setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
		setBackground(Color.WHITE);

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout( new BorderLayout() );
		titlePanel.setBackground(Color.WHITE);
		titlePanel.setPreferredSize( new Dimension( this.getPreferredSize().width, this.getPreferredSize().height ) );

		iconPanel = new IconPanel();
		titlePanel.add( iconPanel, BorderLayout.WEST );
		add( titlePanel);

		titleComponent = new JLabel( title );
		Font f = titleComponent.getFont();
		titleComponent.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
		titleComponent.setBorder( new CompoundBorder( BorderFactory.createEmptyBorder( 2, 8, 2, 2 ), titleComponent.getBorder() ) );
		titlePanel.add( titleComponent );

		createExpandablePanel(context, gs);
		add( expandablePanel);
		
		if ( isExpanded ) {
			iconPanel.doClick();
		}
	}
	
	private void createExpandablePanel(Context context, GlobalSettings gs) {
		expandablePanel = new JPanel();
		expandablePanel.setBackground( Color.WHITE );
		expandablePanel.setLayout( new BoxLayout(expandablePanel, BoxLayout.Y_AXIS) );
		for (FeatureSetting fs: featureSettings) {
			if (fs.parameters().isEmpty())
			{
				expandablePanel.add( new NonParametrizedRow(fs) );
			} else {
				expandablePanel.add( new ParametrizedRow(context, gs, fs) );
			}
		}
	}

	@Override
	public Dimension getMinimumSize() {
		if ( collapsed ) {
			return new Dimension( MIN_COMPONENT_WIDTH, MIN_COMPONENT_HEIGHT );
		} else {
			Dimension d = super.getMinimumSize();
			return new Dimension( MIN_COMPONENT_WIDTH, d.height );
		}
	}

	@Override
	public Dimension getPreferredSize() {
		if ( collapsed ) {
			return new Dimension( MIN_COMPONENT_WIDTH, MIN_COMPONENT_HEIGHT );
		} else {
			Dimension d = super.getPreferredSize();
			return new Dimension( MIN_COMPONENT_WIDTH, d.height );
		}
	}

	@Override
	protected int preferredCollapsedHeight() {
		return 40;
	}

	@Override
	protected int preferredExpandedHeight() {
		Dimension d = super.getPreferredSize();
		return d.height;
	}

	class IconPanel extends JPanel {

		private static final long serialVersionUID = -1126501236314927869L;
		private JButton iconButton;
		private boolean isExpanded = false;

		public IconPanel() {
			setLayout( new BorderLayout() );
			setBackground(Color.WHITE);
			iconButton = new JButton( AccordionControlIcons.COLLAPSED.getIcon());
			iconButton.setFocusPainted(false);
	        iconButton.setMargin(new Insets(0, 0, 0, 0));
	        iconButton.setContentAreaFilled(false);
	        iconButton.setBorderPainted(false);
	        iconButton.setOpaque(false);
			iconButton.addActionListener( new ActionListener() {

				@Override
				public void actionPerformed( ActionEvent e ) {
					if ( isExpanded ) {
						iconButton.setIcon( AccordionControlIcons.COLLAPSED.getIcon() );
						collapse();
						isExpanded = false;
					} else {
						iconButton.setIcon( AccordionControlIcons.EXPANDED.getIcon() );
						expand();
						isExpanded = true;
					}
				}
			} );
			add( iconButton, BorderLayout.CENTER );
		}
		
		public void doClick()
		{
			iconButton.doClick();
		}
		
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

	@Override
	public JComponent getExpandableComponent() {
		return expandablePanel;
	}
}
