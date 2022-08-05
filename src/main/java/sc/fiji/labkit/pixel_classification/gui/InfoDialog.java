/*-
 * #%L
 * The implementation of the pixel classification algorithm, that is used the Labkit image segmentation plugin for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import net.miginfocom.swing.MigLayout;

public class InfoDialog extends JDialog {

	private final String OS = System.getProperty("os.name").toLowerCase();
	private JPanel panel;

	public InfoDialog(Container parent, String name, String documentation) {
		setTitle(name);
		createUI(documentation);
		setModal(true);
		setSize(panel.getPreferredSize());
		setLocationRelativeTo(parent);
	}

	private void createUI(String documentation) {
		panel = new JPanel();
		panel.setLayout(new MigLayout("", "[left]"));
		JEditorPane docoPane = new JEditorPane();
		docoPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		docoPane.setEditorKit(new HTMLEditorKit());
		docoPane.setText(documentation);
		docoPane.setEditable(false);
		docoPane.setPreferredSize(new Dimension(200, 200));
		docoPane.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					try {
						openURLInBrowser(hle.getURL().toString());
					}
					catch (Exception e) {
						// Logger
					}
				}
			}
		});
		panel.add(new JScrollPane(docoPane), "gaptop10, wrap");
		JButton doneButton = new JButton("Done");
		panel.add(doneButton, "align right, gapbottom5, split 2");
		doneButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(panel);
	}

	private void openURLInBrowser(String url) throws IOException, InterruptedException {
		if (OS.indexOf("win") >= 0) {
			Runtime.getRuntime().exec((new StringBuilder()).append(
				"rundll32 url.dll, FileProtocolHandler ").append(url).toString());
		}
		else if (OS.indexOf("mac") >= 0) {
			String[] args = { "osascript", "-e", "open location \"" + url + "\"" };
			Runtime.getRuntime().exec(args);
		}
		else {
			String browsers[] = { "firefox", "opera", "konqueror", "epiphany", "chrome" };
			String browser = null;
			for (int i = 0; i < browsers.length && browser == null; i++) {
				if (Runtime.getRuntime().exec(new String[] { "which", browsers[i] }).waitFor() == 0)
					browser = browsers[i];
			}
			if (browser == null)
				throw new FileNotFoundException("Could not find web browser");
			Runtime.getRuntime().exec(new String[] { browser, url });
		}
	}

}
