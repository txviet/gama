package msi.gama.util.graph.writer;

import org.gephi.io.exporter.plugin.ExporterDL;
import org.gephi.io.exporter.spi.GraphExporter;

/**
 * 
 * @author Samuel Thiriot
 */
public class GephiWriterDLList extends GephiWriterAbstract {

	@Override
	protected void initializeExporter(GraphExporter exporter) {
		((ExporterDL)exporter).setUseListFormat(true);
	}

	@Override
	protected String getFormat() {
		return "dl";
	}

	

}