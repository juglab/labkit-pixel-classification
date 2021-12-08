/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.pixel_classification.gui;

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

	private static final BufferedImage backupImage = new BufferedImage(24, 24,
		BufferedImage.TYPE_INT_ARGB);

	private static final ImageIcon backupIcon = new ImageIcon(backupImage);

	public static URL getResource(String name) {
		return instance.getResourceImpl(name);
	}

	public static ImageIcon getIcon(String name) {
		return instance.getIconImpl(name);
	}

	public static BufferedImage getImage(String name) {
		return instance.getImageImpl(name);
	}

	private BufferedImage getImageImpl(String name) {
		try {
			return ImageIO.read(getResource(name));
		}
		catch (IOException e) {
			// Fail gracefully
			return backupImage;
		}
	}

	private URL getResourceImpl(String name) {
		return ClassLoader.getSystemResource(name);
	}

	private ImageIcon getIconImpl(String name) {
		ImageIcon icon = map.get(name);
		if (icon == null) {
			try {
				icon = new ImageIcon(getResourceImpl(name));
			}
			catch (NullPointerException npe) {
				// Fail gracefully
				icon = backupIcon;
			}
			map.put(name, icon);
		}
		return icon;
	}
}
