package net.imglib2.trainable_segmention.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.scijava.Context;

import net.imglib2.trainable_segmention.gui.FeatureSettingsUI.GlobalsPanel;

public class FiltersList extends JList< FiltersListRow > {

	private static final long serialVersionUID = 1L;

	private int selectedIndex;

	public FiltersList( Context context, GlobalsPanel globalsPanel, FiltersListModel model ) {
		super( model );

		setCellRenderer( new CheckBoxCellRenderer() );

		addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed( MouseEvent e ) {
				selectedIndex = locationToIndex( e.getPoint() );
				if ( selectedIndex < 0 )
					return;

				Point p = e.getPoint().getLocation();
				if ( selectedIndex > 0 ) {
					Rectangle listCellBounds = getCellBounds( 0, selectedIndex - 1 );
					p.y -= listCellBounds.height;
				}

				FiltersListRow panel = getModel().getElementAt( selectedIndex );
				JCheckBox cb = panel.getCheckBox();
				JLabel ib = panel.getInfoButton();
				JLabel db = panel.getDuplicateButton();
				JLabel rb = panel.getRemoveButton();
				JLabel eb = panel.getEditButton();
				if ( isOverComponent( cb, p ) ) {
					cb.setSelected( !cb.isSelected() );
					validate();
					repaint();
				} else if ( isOverComponent( eb, p ) ) {
					panel.editParameters(context, globalsPanel.get());
					
				} else if ( isOverComponent( db, p ) ) {
					duplicateFilter();
					
				} else if ( isOverComponent( rb, p ) ) {
					removeFilter();
					
				} else if ( isOverComponent( ib, p ) ) {
					panel.showInfoDialog();

				}
			}

		} );

		setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	}

	private void duplicateFilter() {
		( ( FiltersListModel ) getModel() ).add( selectedIndex, new FiltersListRow(getModel().getElementAt( selectedIndex ).getFeatureSetting()));
		super.revalidate();
		super.repaint();
	}

	private void removeFilter() {
		( ( FiltersListModel ) getModel() ).remove( selectedIndex );
		super.revalidate();
		super.repaint();
	}

	private boolean isOverComponent( Component c, Point p ) {
		Point pp = c.getParent().getLocation();
		Rectangle r = c.getBounds();
		if (pp.x > 0) {
			r.setBounds( c.getX() + pp.x, c.getY(), c.getWidth(), c.getHeight());
		}
		return r.contains( p );
	}

	class CheckBoxCellRenderer implements ListCellRenderer< Object > {

		@Override
		public Component getListCellRendererComponent(
				JList< ? > list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus ) {

			FiltersListRow row = ( FiltersListRow ) value;

			JCheckBox checkbox = row.getCheckBox();
			checkbox.setBackground( isSelected ? list.getSelectionBackground() : list.getBackground() );
			checkbox.setForeground( isSelected ? list.getSelectionForeground() : list.getForeground() );
			checkbox.setEnabled( list.isEnabled() );
			checkbox.setFont( list.getFont() );
			checkbox.setFocusPainted( false );
			checkbox.setBorderPainted( true );
			checkbox.setBorder( isSelected ? UIManager.getBorder( "List.focusCellHighlightBorder" ) : new EmptyBorder( 1, 1, 1, 1 ) );
			return row;
		}
	}
}
