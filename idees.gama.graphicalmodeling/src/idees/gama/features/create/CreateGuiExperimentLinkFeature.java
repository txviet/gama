package idees.gama.features.create;

import idees.gama.features.add.AddGuiExperimentFeature;
import idees.gama.ui.image.GamaImageProvider;
import gama.EExperiment;
import gama.EExperimentLink;
import gama.EGUIExperiment;
import gama.EWorldAgent;

import org.eclipse.graphiti.examples.common.ExampleUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateConnectionFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

public class CreateGuiExperimentLinkFeature  extends AbstractCreateConnectionFeature {

	 private static final String TITLE = "Create a GUI experiment";
	  
	 private static final String USER_QUESTION = "Enter new GUI experiment name";
	 
	public CreateGuiExperimentLinkFeature(IFeatureProvider fp) {
		// provide name and description for the UI, e.g. the palette
		super(fp, "is composed of a GUI experiment", "Create a new GUI experiment");
	}

	private EGUIExperiment createEGUIExperiment(ICreateConnectionContext context) {
		String newGUIName = ExampleUtil.askString(TITLE, USER_QUESTION, "");
	    if (newGUIName == null || newGUIName.length() == 0) {
	    	newGUIName = "my_GUI_xp";
	    }  
	    EGUIExperiment newGUIExp= gama.GamaFactory.eINSTANCE.createEGUIExperiment();
	    newGUIExp.setName(newGUIName);
		this.getDiagram().eResource().getContents().add(newGUIExp);
		CreateContext ac = new CreateContext();
		ac.setLocation(context.getTargetLocation().getX(), context.getTargetLocation().getY());
		ac.setSize(0, 0);
		ac.setTargetContainer(getDiagram());
		return newGUIExp;
	}
	
	private PictogramElement addEGUIExperiment(ICreateConnectionContext context, EGUIExperiment GUIExp) {
		CreateContext cc = new CreateContext();
		cc.setLocation(context.getTargetLocation().getX() - (int)(AddGuiExperimentFeature.INIT_WIDTH/2.0), context.getTargetLocation().getY() - (int)(AddGuiExperimentFeature.INIT_HEIGHT/2.0));
		cc.setSize(0, 0);
		cc.setTargetContainer(getDiagram());
		return getFeatureProvider().addIfPossible(new AddContext(cc, GUIExp));
	}
	
	public Connection create(ICreateConnectionContext context) {
		Connection newConnection = null;
		EWorldAgent source = getEWorldAgent(context.getSourceAnchor());
		EGUIExperiment target = createEGUIExperiment(context);
		PictogramElement targetpe = addEGUIExperiment(context, target);
		if (source != null && target != null) {
			// create new business object
			EExperimentLink eReference = createEReference(source, target);
			//eReference.setModel(source.getModel());
			getDiagram().eResource().getContents().add(eReference);
			// add connection for business object
			AddConnectionContext addContext = new AddConnectionContext(
					context.getSourceAnchor(), getAnchor(targetpe));
			addContext.setNewObject(eReference);
			newConnection = (Connection) getFeatureProvider().addIfPossible(
					addContext);
			source.getOutcomingLinks().add(eReference);
			target.getIncomingLinks().add(eReference);
		}

		return newConnection;
	}
	
	public boolean canCreate(ICreateConnectionContext context) {
		EWorldAgent source = getEWorldAgent(context.getSourceAnchor());
		if (source != null) {
			return true;
		}
		return false;
	}

	public boolean canStartConnection(ICreateConnectionContext context) {
		if (getEWorldAgent(context.getSourceAnchor()) != null) {
			return true;
		}
		return false;
	}
	
	protected EWorldAgent getEWorldAgent(Anchor anchor) {
		if (anchor != null) {
			Object object = getBusinessObjectForPictogramElement(anchor
					.getParent());
			if (object instanceof EWorldAgent) {
				return (EWorldAgent) object;
			}
		}
		return null;
	}
	protected Anchor getAnchor(PictogramElement targetpe) {
		Anchor ret = null;
		if (targetpe instanceof Anchor) {
			ret = (Anchor) targetpe;
		} else if (targetpe instanceof AnchorContainer) {
			ret = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) targetpe);
		}
		return ret;
	}
	


	/**
	 * Creates a EReference between two EClasses.
	 */
	private EExperimentLink createEReference(EWorldAgent source, EExperiment target) {
		EExperimentLink eReference = gama.GamaFactory.eINSTANCE.createEExperimentLink();
		return eReference;
	}
	
	@Override
	public String getCreateImageId() {
		return GamaImageProvider.IMG_GUIXPLINK;
	}
}