/**
 * Created by drogoul, 18 nov. 2014
 * 
 */
package msi.gama.gui.navigator;

import msi.gama.gui.swt.*;
import msi.gaml.descriptions.IDescription;
import org.eclipse.swt.graphics.*;

/**
 * Class WrappedGamlObject.
 * 
 * @author drogoul
 * @since 18 nov. 2014
 * 
 */
public abstract class WrappedGamlObject extends VirtualContent implements Comparable<WrappedGamlObject> {

	private final IDescription object;

	public WrappedGamlObject(final Object root, final IDescription object) {
		super(root, object.getName());
		this.object = object;
	}

	public WrappedGamlObject(final Object root, final String object) {
		super(root, object);
		this.object = null;
	}

	public IDescription getObject() {
		return object;
	}

	@Override
	public Font getFont() {
		return SwtGui.getNavigFileFont(); // by default
	}

	@Override
	public int compareTo(final WrappedGamlObject other) {
		return getName().compareTo(other.getName());
	}

	@Override
	public Color getColor() {
		return IGamaColors.BLUE.inactive();
	}

}
