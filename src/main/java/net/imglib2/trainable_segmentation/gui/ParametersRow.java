
package net.imglib2.trainable_segmentation.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.StringJoiner;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.scijava.Context;

import net.imglib2.trainable_segmentation.pixel_feature.settings.FeatureSetting;
import org.scijava.module.Module;
import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.ui.swing.widget.SwingInputHarvester;

public class ParametersRow extends JPanel {

	private static final ImageIcon DOT_ICON = IconResources.getIcon("dot_icon_16px.png");
	private static final ImageIcon RM_ICON = IconResources.getIcon("minus_icon_16px.png");
	private static final ImageIcon PARAMS_ICON = IconResources.getIcon("params_icon_16px.png");

	private Context context;
	private FeatureSetting featureSetting;

	private JLabel paramsLabel;

	public ParametersRow(Context context, FeatureSetting featureSetting) {
		this.context = context;
		this.featureSetting = featureSetting;
		setLayout(new BorderLayout());
		initUI();
	}

	private void initUI() {

		JPanel cbPanel = new JPanel();
		cbPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		cbPanel.add(Box.createHorizontalStrut(50));

		paramsLabel = new JLabel(paramsString(), DOT_ICON, SwingConstants.LEFT);
		cbPanel.add(paramsLabel, BorderLayout.WEST);
		add(cbPanel, BorderLayout.WEST);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton rmButton = new JButton(RM_ICON);
		rmButton.setFocusPainted(false);
		rmButton.setMargin(new Insets(0, 0, 0, 0));
		rmButton.setContentAreaFilled(false);
		rmButton.setBorderPainted(false);
		rmButton.setOpaque(false);
		rmButton.setToolTipText("Remove filter");
		rmButton.addActionListener(this::remove);
		btnPanel.add(rmButton);

		JButton editButton = new JButton(PARAMS_ICON);
		editButton.setFocusPainted(false);
		editButton.setMargin(new Insets(0, 0, 0, 0));
		editButton.setContentAreaFilled(false);
		editButton.setBorderPainted(false);
		editButton.setOpaque(false);
		editButton.setToolTipText("Edit filter parameters");
		editButton.addActionListener(this::editParameters);
		btnPanel.add(editButton);
		add(btnPanel, BorderLayout.EAST);
	}

	private void editParameters(ActionEvent e) {
		featureSetting = showParametersDialog(context, featureSetting);
		update();
		validate();
		repaint();
	}

	private void update() {
		paramsLabel.setText(paramsString());
		paramsLabel.repaint();
	}

	private void remove(ActionEvent e) {
		getParent().remove(this);
	}

	private String paramsString() {
		StringJoiner joiner = new StringJoiner(",");
		for (String parameter : featureSetting.parameters())
			joiner.add(parameter + "=" + featureSetting.getParameter(parameter));
		String s = joiner.toString();
		s = s.replace("sigma", "\u03c3");
		s = s.replace("psi", "\u03c8");
		s = s.replace("gamma", "\u03b3");
		return "<html>" + s + "</html>";
	}

	public FeatureSetting getFeatureSetting() {
		return featureSetting;
	}

	public static FeatureSetting showParametersDialog(Context context, FeatureSetting op) {
		SwingInputHarvester harvester = new SwingInputHarvester();
		context.inject(harvester);
		try {
			Module module = op.asModule(null);
			harvester.harvest(module);
			return FeatureSetting.fromModule(module);
		}
		catch (ModuleCanceledException e) {
			return op;
		}
		catch (ModuleException e) {
			throw new RuntimeException(e);
		}
	}

}
