/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

import sc.fiji.labkit.pixel_classification.pixel_feature.settings.FeatureSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.settings.GlobalSettings;
import sc.fiji.labkit.pixel_classification.pixel_feature.filter.GroupedFeatures;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import org.scijava.Context;

import javax.swing.*;

/**
 * @author Matthias Arzt
 */
public class FeatureSettingsUITest {

	public static void main(String... args) throws InterruptedException {
		Context context = SingletonContext.getInstance();
		GlobalSettings settings = GlobalSettings.default2d().build();
		FeatureSettings fs = new FeatureSettings(settings, GroupedFeatures.gauss());
		FeatureSettingsUI gui = new FeatureSettingsUI(context, fs);
		showFrame(gui);
		System.out.println(gui.get().toJson());
	}

	private static void showFrame(JComponent component) throws InterruptedException {
		JFrame frame = new JFrame("Change Feature Settings Demo");
		frame.add(component);
		frame.setSize(300, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
		while (frame.isVisible()) {
			Thread.sleep(100);
		}
	}
}
