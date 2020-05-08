package net.imglib2.trainable_segmention.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconResources {

	private final Map<String, ImageIcon> map = new HashMap<>();

	private static final IconResources instance = new IconResources();

	private static final BufferedImage backupImage = new BufferedImage( 24, 24, BufferedImage.TYPE_INT_ARGB );
	
	private static final ImageIcon backupIcon = new ImageIcon( backupImage );

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
		return ClassLoader.getSystemResource( name );
	}

	private ImageIcon getIconImpl( String name ) {
		ImageIcon icon = map.get( name );
		if ( icon == null ) {
			try {
				icon = new ImageIcon( getResourceImpl( name ) );
			} catch ( NullPointerException npe ) {
				//Fail gracefully
				icon = backupIcon;
			}
			map.put( name, icon );
		}
		return icon;
	}
}
