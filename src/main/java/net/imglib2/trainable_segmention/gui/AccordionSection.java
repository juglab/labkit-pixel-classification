package net.imglib2.trainable_segmention.gui;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class AccordionSection< AS extends AccordionSection< AS > > extends JPanel {

	private static final long serialVersionUID = 1L;
	protected static final int MIN_COMPONENT_HEIGHT = 40;
	protected static final int MIN_COMPONENT_WIDTH = 350;
	protected AccordionPanel< AS > owner;

	protected boolean collapsed = true;

	public void setOwner( AccordionPanel< AS > owner ) {
		this.owner = owner;
	}

	protected abstract int preferredCollapsedHeight();

	protected abstract int preferredExpandedHeight();

	abstract JComponent getExpandableComponent();

	@SuppressWarnings( "unchecked" )
	public void collapse() {
		owner.setSectionCollapsed( ( AS ) this );
		int calculatedHeight = this.preferredExpandedHeight();
		AccordionAnimation anim = new AccordionAnimation( this, 200 );
		anim.setStartValue( calculatedHeight );
		anim.setEndValue( MIN_COMPONENT_HEIGHT );
		anim.start();
		collapsed = true;
	}

	@SuppressWarnings( "unchecked" )
	public void expand() {
		owner.setSectionExpanded( ( AS ) this );
		int calculatedHeight = preferredExpandedHeight();
		AccordionAnimation anim = new AccordionAnimation( this, 200 );
		anim.setStartValue( MIN_COMPONENT_HEIGHT );
		anim.setEndValue( calculatedHeight );
		anim.start();
		collapsed = false;
	}
}
