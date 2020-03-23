package net.imglib2.trainable_segmention.gui;

import java.awt.Dimension;

public class AccordionAnimation extends Animation {

	private AccordionSection accordionSection;

	public AccordionAnimation(AccordionSection accordionSection, int durationMs) {
		super(durationMs);
		this.accordionSection = accordionSection;
	}

	@Override
	public void starting () {
		accordionSection.getExpandableComponent().setVisible(true);
	}

	@Override
	protected void render(int value) {
		accordionSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, value));
		accordionSection.getExpandableComponent().setVisible(true);
		accordionSection.revalidate();
	}

	@Override
	public void stopped () {
		accordionSection.getExpandableComponent().setVisible(true);
		accordionSection.revalidate();
	}

}
