package net.imglib2.trainable_segmention.gui;

import javax.swing.*;

public abstract class AccordionSection extends JPanel {

	private static final long serialVersionUID = 1L;
	protected static final int MIN_COMPONENT_HEIGHT = 40;
	protected static final int MIN_COMPONENT_WIDTH = 500;

	boolean collapsed = true;

	protected abstract int preferredCollapsedHeight();

	protected abstract int preferredExpandedHeight();

	protected abstract JComponent getExpandableComponent();

	public void collapse() {
		int calculatedHeight = this.preferredExpandedHeight();
		AccordionAnimation anim = new AccordionAnimation( this, 5);
		anim.setStartValue( calculatedHeight );
		anim.setEndValue( MIN_COMPONENT_HEIGHT );
		anim.start();
		collapsed = true;
		repaint();
	}

	public void expand() {
		int calculatedHeight = preferredExpandedHeight();
		AccordionAnimation anim = new AccordionAnimation( this, 5);
		anim.setStartValue( MIN_COMPONENT_HEIGHT );
		anim.setEndValue( calculatedHeight );
		anim.start();
		collapsed = false;
		repaint();
	}
}
