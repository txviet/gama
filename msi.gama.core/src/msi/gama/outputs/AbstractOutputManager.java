/*********************************************************************************************
 * 
 * 
 * 'AbstractOutputManager.java', in plugin 'msi.gama.core', is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2014 UMI 209 UMMISCO IRD/UPMC & Partners
 * 
 * Visit https://code.google.com/p/gama-platform/ for license information and developers contact.
 * 
 * 
 **********************************************************************************************/
package msi.gama.outputs;

import java.util.*;
import msi.gama.common.GamaPreferences;
import msi.gama.common.util.GuiUtils;
import msi.gama.runtime.*;
import msi.gama.util.TOrderedHashMap;
import msi.gaml.compilation.*;
import msi.gaml.descriptions.IDescription;
import com.google.common.collect.*;

/**
 * Class AbstractOutputManager.
 * 
 * @author drogoul
 * @since 9 juin 2013
 * 
 */
public abstract class AbstractOutputManager extends Symbol implements IOutputManager {

	protected final Map<String, IOutput> outputs = new TOrderedHashMap<String, IOutput>();

	public AbstractOutputManager(final IDescription desc) {
		super(desc);
	}

	@Override
	public Map<String, IOutput> getOutputs() {
		return outputs;
	}

	@Override
	public IOutput getOutput(final String id) {
		return outputs.get(id);
	}

	public IOutput getOutputWithName(final String name) {
		for ( final IOutput output : outputs.values() ) {
			if ( output.getName().equals(name) ) { return output; }
		}
		return null;
	}

	@Override
	public void addOutput(final IOutput output) {
		if ( output == null ) { return; }// || outputs.containsValue(output) ) { return; }
		outputs.put(output.getId(), output);
	}

	// hqnghi add output with alias name from micro-model
	@Override
	public void addOutput(final String name, final IOutput output) {
		if ( output == null ) { return; }// || outputs.containsValue(output) ) { return; }
		outputs.put(name, output);
	}

	// end-hqnghi
	@Override
	public synchronized void dispose() {
		super.dispose();
		try {
			// AD: explicit addition of an ArrayList to prevent dispose errors (when outputs remove themselves from the list)
			for ( final IOutput output : new ArrayList<IOutput>(outputs.values()) ) {
				output.dispose();
			}
			outputs.clear();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	// hqnghi
	// for instant, multi-simulation cannot have their owns outputs display at same time.
		public
		void removeAllOutput() {
		outputs.clear();
	}

	@Override
	public void removeOutput(final IOutput o) {
		if ( o == null ) { return; }
		if ( o.isUserCreated() ) {
			o.dispose();
			outputs.values().remove(o);
		} else {
			o.setPaused(true);
		}
	}

	@Override
	public void setChildren(final List<? extends ISymbol> commands) {
		for ( final ISymbol s : commands ) {
			if ( s instanceof IOutput ) {
				IOutput o = (IOutput) s;
				addOutput(o);
				o.setUserCreated(false);
			}
		}
	}

	@Override
	public void forceUpdateOutputs() {
		for ( final IDisplayOutput o : getDisplayOutputs() ) {
			o.update();
		}
	}

	private Iterable<IDisplayOutput> getDisplayOutputs() {
		return Iterables.filter(outputs.values(), IDisplayOutput.class);
	}

	@Override
	public boolean init(final IScope scope) {
		List<IOutput> list = new ArrayList(outputs.values());

		for ( final IOutput output : list ) {

			/**
			 * TODO For the moment, the try block fixes issue 470, must be replaced by better solution
			 */
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			if ( scope.init(output) ) {
				output.setPaused(false);
				if ( scope.step(output) ) {
					try {
						output.open();
						output.update();
					} catch (RuntimeException e) {
						e.printStackTrace();
						return false;
					}
				}
			}
			GuiUtils.waitForViewsToBeInitialized();
		}

		if ( GamaPreferences.CORE_AUTO_RUN.getValue() ) {
			GAMA.controller.userStart();
		} else {
			GuiUtils.informStatus("Simulation ready");
		}
		return true;
	}

	@Override
	public boolean step(final IScope scope) {
		// final int cycle = scope.getClock().getCycle();
		for ( final IOutput o : ImmutableList.copyOf(outputs.values()) ) {
			if ( !o.isPaused() && o.isOpen() && o.isRefreshable() && o.getScope().step(o) ) {
				try {
					o.update();
				} catch (RuntimeException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
		return true;
	}

}