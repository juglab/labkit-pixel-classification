package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;

/**
 * Sub component of the AccordionPanel
 */
public class FiltersListSection extends AccordionSection< FiltersListSection > {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon EXPANDED_ICON = new ImageIcon( FiltersListSection.class.getClassLoader().getResource( "arrow_down_48px.png" ) );
	private static final ImageIcon COLLAPSED_ICON = new ImageIcon( FiltersListSection.class.getClassLoader().getResource( "arrow_right_48px.png" ) );

	private FiltersList filtersList;
	private IconPanel iconPanel;
	private JLabel titleComponent;

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
	public FiltersListSection( AccordionPanel< FiltersListSection > owner, String title, FiltersList filtersList, boolean isExpanded ) {
		this.owner = owner;
		this.filtersList = filtersList;
		setLayout( new BorderLayout() );

		JPanel titlePanel = new JPanel();
		titlePanel.setLayout( new BorderLayout() );
		titlePanel.setPreferredSize( new Dimension( this.getPreferredSize().width, this.getPreferredSize().height ) );

		iconPanel = new IconPanel( isExpanded );
		titlePanel.add( iconPanel, BorderLayout.WEST );
		add( titlePanel, BorderLayout.NORTH );

		titleComponent = new JLabel( title );
		titleComponent.setBorder( new CompoundBorder( BorderFactory.createEmptyBorder( 2, 8, 2, 2 ), titleComponent.getBorder() ) );
		titlePanel.add( titleComponent );

		add( filtersList, BorderLayout.CENTER );
		if ( !isExpanded )
			this.collapse();
		
		revalidate();
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
		private JLabel iconLabel;
		private boolean isExpanded;

		public IconPanel( boolean expanded ) {
			isExpanded = expanded;
			setLayout( new BorderLayout() );
			iconLabel = new JLabel( isExpanded ? AccordionControlIcons.EXPANDED.getIcon() : AccordionControlIcons.COLLAPSED.getIcon() );
			iconLabel.addMouseListener( new MouseAdapter() {

				@Override
				public void mouseReleased( MouseEvent e ) {
					if ( isExpanded ) {
						iconLabel.setIcon( AccordionControlIcons.COLLAPSED.getIcon() );
						collapse();
						repaint();
						isExpanded = false;
					} else {
						iconLabel.setIcon( AccordionControlIcons.EXPANDED.getIcon() );
						expand();
						repaint();
						isExpanded = true;
					}
				}
			} );
			add( iconLabel, BorderLayout.CENTER );
		}
	}

	@Override
	JComponent getExpandableComponent() {
		return filtersList;
	}
}
