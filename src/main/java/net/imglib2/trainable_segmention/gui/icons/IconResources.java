package net.imglib2.trainable_segmention.gui.icons;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconResources extends HashMap< String, ImageIcon > {

	private static IconResources instance = new IconResources();

	private static final long serialVersionUID = 1L;

	private static BufferedImage backupImage = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_ARGB );
	
	private static ImageIcon backupIcon = new ImageIcon( backupImage );

	public static URL getResource( String name ) {
		return instance.getResourceImpl( name );
	}

	public static ImageIcon getIcon( String name ) {
		return instance.getIconImpl( name );
	}

	public static BufferedImage getImage( String name ) {
		return instance.getImageImpl( name );
	}

	private BufferedImage getImageImpl( String name ) {
		try {
			return ImageIO.read( getResource( name ) );
		} catch ( IOException e ) {
			//Fail gracefully
			return backupImage;
		}
	}

	private URL getResourceImpl( String name ) {
		return getClass().getResource( name );
	}

	private ImageIcon getIconImpl( String name ) {
		ImageIcon icon = get( name );
		if ( icon == null ) {
			try {
				icon = new ImageIcon( getResourceImpl( name ) );
			} catch ( NullPointerException npe ) {
				//Fail gracefully
				icon = backupIcon;
			}
			put( name, icon );
		}
		return icon;
	}
}
