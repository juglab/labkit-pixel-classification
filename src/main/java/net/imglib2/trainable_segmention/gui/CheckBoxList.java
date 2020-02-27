package net.imglib2.trainable_segmention.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.filter.FeatureOp;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.util.ValuePair;

public class CheckBoxList extends JList<JCheckBox> {

	private static final long serialVersionUID = 1L;

	public CheckBoxList(ListModel<JCheckBox> model) {
		super(model);
		setCellRenderer( new CheckBoxCellRenderer() );

		addMouseListener( new MouseAdapter() {

			@Override
			public void mousePressed( MouseEvent e ) {
				int index = locationToIndex( e.getPoint() );

				if ( index != -1 ) {
					JCheckBox checkbox = getModel().getElementAt( index );
					checkbox.setSelected( !checkbox.isSelected() );
					repaint();
				}
			}
		} );

		setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	}

	protected class CheckBoxCellRenderer implements ListCellRenderer< Object > {

		public Component getListCellRendererComponent(
				JList<?> list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus ) {
			JCheckBox checkbox = ( JCheckBox ) value;
			checkbox.setBackground( isSelected ? getSelectionBackground() : getBackground() );
			checkbox.setForeground( isSelected ? getSelectionForeground() : getForeground() );
			checkbox.setEnabled( isEnabled() );
			checkbox.setFont( getFont() );
			checkbox.setFocusPainted( false );
			checkbox.setBorderPainted( true );
			checkbox.setBorder( isSelected ? UIManager.getBorder( "List.focusCellHighlightBorder" ) : new EmptyBorder( 1, 1, 1, 1 ) );
			return checkbox;
		}
	}
	
	public static void main( String... args ) {
		Context context = new Context();
		final JFrame frame = new JFrame();
		DefaultListModel<JCheckBox> listModel = new DefaultListModel<>();
		CheckBoxList list = new CheckBoxList(listModel);
		List<ValuePair<Class<? extends FeatureOp>, String>> features = AvailableFeatures
				.getValidFeatures(context, GlobalSettings.default2d().build());
			features.sort(Comparator.comparing(ValuePair::getB));
			features.forEach(featureClassAndLabel -> {
				listModel.addElement(new JCheckBox(featureClassAndLabel.getB())); 
			});
		frame.getContentPane().add( list );
		frame.setSize( 400, 400 );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

}
