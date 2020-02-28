package net.imglib2.trainable_segmention.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class FiltersList extends JList< FiltersListRow > {

	private static final long serialVersionUID = 1L;
	private static final ImageIcon DUP_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "plus_icon_16px.png" ) );
	private static final ImageIcon RM_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "minus_icon_16px.png" ) );
	private static final ImageIcon PARAMS_ICON = new ImageIcon( FiltersList.class.getClassLoader().getResource( "params_icon_16px.png" ) );

	private JPopupMenu popupMenu;

	public FiltersList( ListModel< FiltersListRow > model ) {
		super( model );
		setCellRenderer( new CheckBoxCellRenderer() );

		addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed( MouseEvent e ) {
				/*
				 * int index = locationToIndex( e.getPoint() );
				 * int height = 0;
				 * 
				 * if ( index != -1 ) {
				 * if ( index > 0 ) {
				 * Rectangle listCellBounds = getCellBounds( 0, index - 1 );
				 * height = listCellBounds.height;
				 * }
				 * Point p = e.getPoint().getLocation();
				 * p.y -= height;
				 * 
				 * FiltersListRow panel = getModel().getElementAt( index );
				 * JCheckBox cb = panel.getCheckBox();
				 * if ( isOverComponent( cb, p ) ) {
				 * cb.setSelected( !cb.isSelected() );
				 * validate();
				 * repaint();
				 * return;
				 * }
				 * 
				 * if ( isOverComponent( panel.getInfoLabel(), p ) ) {
				 * InfoDialog docoDiag = new InfoDialog( panel, "example",
				 * "If you use this filter you will do great things" );
				 * docoDiag.setVisible( true );
				 * return;
				 * }
				 * 
				 * if ( isOverComponent( panel.getNameLabel(), p ) ) {
				 * panel.getNameLabel();
				 * if ( e.getButton() == MouseEvent.BUTTON3 || e.getButton() ==
				 * MouseEvent.BUTTON2 ) {
				 * if ( !popupMenu.isVisible() )
				 * popupMenu.show( panel, p.x, p.y );
				 * else
				 * popupMenu.setVisible( false );
				 * }
				 * }
				 * }
				 */
			}

			@Override
			public void mouseClicked( MouseEvent e ) {
				int index = locationToIndex( e.getPoint() );
				int height = 0;

				if ( index != -1 ) {
					if ( index > 0 ) {
						Rectangle listCellBounds = getCellBounds( 0, index - 1 );
						height = listCellBounds.height;
					}
					Point p = e.getPoint().getLocation();
					p.y -= height;

					FiltersListRow panel = getModel().getElementAt( index );
					JCheckBox cb = panel.getCheckBox();
					if ( isOverComponent( cb, p ) ) {
						cb.setSelected( !cb.isSelected() );
						validate();
						repaint();
						return;
					}

					if ( isOverComponent( panel.getInfoLabel(), p ) ) {
						InfoDialog docoDiag = new InfoDialog( panel, "example", "If you use this filter you will do great things" );
						docoDiag.setVisible( true );
						return;
					}

					JLabel nameLabel = panel.getNameLabel();
					if ( isOverComponent( nameLabel, p ) ) {
						if ( e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2 ) {
							if ( !popupMenu.isVisible() )
								popupMenu.show( nameLabel, p.x, p.y );
							else
								popupMenu.setVisible( false );
						}
					}
				}

			}

		} );

		setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		createPopupMenu();
	}

	private void createPopupMenu() {
		popupMenu = new JPopupMenu();

		JMenuItem item1 = new JMenuItem( "Edit Parameters", PARAMS_ICON );
		item1.setHorizontalTextPosition( SwingConstants.RIGHT );
		item1.addActionListener( new ActionListener() {

			@Override
			public void actionPerformed( ActionEvent e ) {}

		} );
		popupMenu.add( item1 );

		JMenuItem item2 = new JMenuItem( "Duplicate", DUP_ICON );
		item2.setHorizontalTextPosition( SwingConstants.RIGHT );
		item2.addActionListener( this::duplicateFilter );
		popupMenu.add( item2 );

		JMenuItem item3 = new JMenuItem( "Remove", RM_ICON );
		item3.setHorizontalTextPosition( SwingConstants.RIGHT );
		item3.addActionListener( this::removeFilter );
		popupMenu.add( item3 );
	}

	private void duplicateFilter( ActionEvent e ) {

	}

	private void removeFilter( ActionEvent e ) {

	}

	private boolean isOverComponent( Component c, Point p ) {
		Rectangle r = c.getBounds();
		if ( r.contains( p ) )
			return true;
		else
			return false;
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
