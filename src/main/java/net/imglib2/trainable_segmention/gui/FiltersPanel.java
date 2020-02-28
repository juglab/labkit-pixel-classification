package net.imglib2.trainable_segmention.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.scijava.Context;

import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;

/**
 */
public class FiltersPanel extends JPanel {

	private static final long serialVersionUID = 1L;


	public FiltersPanel(Context context, GlobalSettings globals) {
		setLayout( new BorderLayout() );
		init(context, globals);
	}

	public void init(Context context, GlobalSettings globals) {
		FiltersListPanel accordionPanel = new FiltersListPanel(context, globals);
		JScrollPane scrollPane = new JScrollPane( accordionPanel);
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add( scrollPane, BorderLayout.CENTER );
	}

	public static void main( String... args ) {
		Context context = new Context();
		final JFrame frame = new JFrame();
		FiltersPanel panel = new FiltersPanel(context, GlobalSettings.default2d().build());
		frame.getContentPane().add( panel );
		frame.setSize( 600, 800 );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );

	}

}
