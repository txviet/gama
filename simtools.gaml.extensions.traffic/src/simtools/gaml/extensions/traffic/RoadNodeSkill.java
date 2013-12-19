package simtools.gaml.extensions.traffic;


import java.util.List;

import msi.gama.metamodel.agent.IAgent;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gaml.skills.MovingSkill;
import msi.gaml.types.IType;


@vars({
	 @var(name = "roads_in", type = IType.LIST, of = IType.AGENT), @var(name = "roads_out", type = IType.LIST, of = IType.AGENT)})
@skill(name = "road_node")
public class RoadNodeSkill extends MovingSkill {

	public final static String ROADS_IN = "roads_in";
	public final static String ROADS_OUT = "roads_out";

	@getter(ROADS_IN)
	public List getRoadsIn(final IAgent agent) {
		return (List) agent.getAttribute(ROADS_IN);
	}
	
	@getter(ROADS_OUT)
	public List getRoadsOut(final IAgent agent) {
		return (List) agent.getAttribute(ROADS_OUT);
	}
	
	@setter(ROADS_IN)
	public void setSourceNode(final IAgent agent, final List rds) {
		agent.setAttribute(ROADS_IN, rds);
	}
	
	@setter(ROADS_OUT)
	public void setTargetNode(final IAgent agent, final List rds) {
		agent.setAttribute(ROADS_OUT, rds);
	}
	
}