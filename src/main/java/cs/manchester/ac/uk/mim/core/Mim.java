package cs.manchester.ac.uk.mim.core;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.topbraid.base.progress.ProgressMonitor;
import org.topbraid.base.progress.SimpleProgressMonitor;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.ResourceUtils;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

/**
 * MIM Web Service!
 * 
 */

public class Mim {
	String cl;
	String clMapping; 
	boolean owl;
	Model coreModel;
	
	public Mim(String checklist,
			String checklistMapping, boolean owlrl
			) throws Exception{
	
		coreModel = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		LocationMapper lm = LocationMapper.get();

		lm.addAltEntry("http://purl.org/net/mim/ns",
				"http://sierra-nevada.cs.man.ac.uk/mim/mim/mim.ttl");
		lm.addAltEntry("http://purl.org/net/mim/mimspin",
				"http://sierra-nevada.cs.man.ac.uk/mim/mim/mimspin.ttl");
		lm.addAltEntry("http://sierra-nevada.cs.man.ac.uk/chemmim-spin",
				"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim-spin.ttl");
		lm.addAltEntry("http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim",
				"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim.ttl");

		// TODO - use the URL to check that this is a valid URL
		
		URL checklistURL = new URL(checklist);
		URL mappingURL = new URL(checklistMapping);
		
		if(this.owl){
			coreModel.read("http://topbraid.org/spin/owlrl-all");
			}
			// Import the source data that we are validating
			
			 	coreModel.read("http://sierra-nevada.cs.man.ac.uk/mim/mim/mim",
						"http://purl.org/net/mim/ns", "TURTLE");
				// Import the mim-spin rules
			 	//baseModel.read("http://sierra-nevada.cs.man.ac.uk/mim/mim/mimspin",
				//		"http://purl.org/net/mim/mimspin", "TURTLE");
			 	coreModel.read("http://spinrdf.org/sp");
			
			 // Load the domain specific spin mapping rules	
				coreModel.read(mappingURL.toString(), "TURTLE");
				// Load the MIM Checklist
				coreModel.read(checklistURL.toString(), "TURTLE");
				//process the imports myself as Jena does a terrible job
			processImports(coreModel);
	
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
		
	}
	
	public Model validate(String dataURL) throws Exception {

		
		// Temporarily Map locations of required ontologies that aren't hosted
		// yet
		
		Model baseModel = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		baseModel.add(coreModel);
		
		String type = dataURL.toString().substring(dataURL.toString().length() - 3, dataURL.toString().length());
		//System.out.println(dataURL.toString() + " has type: " + type);
		if (type.equalsIgnoreCase("ttl")) {
			 baseModel.read(dataURL.toString(), "TURTLE");
		} else {
			 baseModel.read(dataURL.toString());
		}
		 
		OntDocumentManager dm = OntDocumentManager.getInstance();
		dm.setProcessImports(false);

		// Create OntModel with imports
		
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM, baseModel);

		// Create and add Model for inferred triples
		Model newTriples = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		ontModel.addSubModel(newTriples);

		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(ontModel, null);
		//System.out.println("Templates:");
		//Collection<Template> templates = SPINModuleRegistry.get()
		//		.getTemplates();
		//for (Template t : templates) {
		//	System.out.println(t.getURI());
		//}
		//System.out.println("Functions:");
		//Collection<Function> functions = SPINModuleRegistry.get()
		//		.getFunctions();
		//for (Function f : functions) {
		//	System.out.println(f.getURI());
		//}
		
		ProgressMonitor pm = new SimpleProgressMonitor("Progress");
		// Run all inferences
		SPINInferences.run(ontModel, newTriples, null, null, false, null);
		System.out.println("Inferred triples: " + newTriples.size());
		
		
		return newTriples;
		
	}

	public static void main(String[] args) throws Exception {

		String out = Mim
				.validate(
						"http://www.chemspider.com/Chemical-Structure.118630.rdf",
						"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim.ttl",
						"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim-spin.ttl",false);
		System.out.println(out);
	}
	
	public static String validate(String data, String checklist,
			String checklistMapping, boolean owlrl) throws Exception {
		
		Model baseModel = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		URL dataURL = new URL(data);
		String type = dataURL.toString().substring(dataURL.toString().length() - 3, dataURL.toString().length());
		//System.out.println(dataURL.toString() + " has type: " + type);
		if (type.equalsIgnoreCase("ttl")) {
			 baseModel.read(dataURL.toString(), "TURTLE");
		} else {
			 baseModel.read(dataURL.toString());
		}
		
		return validate(baseModel,checklist,checklistMapping,owlrl);
	}
	
	public static String validate(Model data, String checklist,
			String checklistMapping, boolean owlrl) throws Exception {
		
		Model newTriples = getValidatedModel(data,checklist,checklistMapping,owlrl);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		newTriples.write(baos, "TURTLE");
		String graph = baos.toString();
		//System.out.println(graph);	
		return graph;
	
	}
	
	public static Model getValidatedModel(String data, String checklist,
			String checklistMapping, boolean owlrl) throws Exception {
		
		Model baseModel = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		URL dataURL = new URL(data);
		String type = dataURL.toString().substring(dataURL.toString().length() - 3, dataURL.toString().length());
		System.out.println(dataURL.toString() + " has type: " + type);
		if (type.equalsIgnoreCase("ttl")) {
			 baseModel.read(dataURL.toString(), "TURTLE");
		} else {
			 baseModel.read(dataURL.toString());
		}
		
		return getValidatedModel(baseModel,checklist,checklistMapping,owlrl);
	}
	
	// TODO need to validate that the checklist passed in is the same as the
	// checklist imported by the mapping (if it imports the checklist).
	public static Model getValidatedModel(Model data, String checklist,
			String checklistMapping, boolean owlrl) throws Exception {

		
		// Temporarily Map locations of required ontologies that aren't hosted
		// yet
		LocationMapper lm = LocationMapper.get();

		lm.addAltEntry("http://purl.org/net/mim/ns",
				"http://sierra-nevada.cs.man.ac.uk/mim/mim/mim.ttl");
		lm.addAltEntry("http://purl.org/net/mim/mimspin",
				"http://sierra-nevada.cs.man.ac.uk/mim/mim/mimspin.ttl");
		lm.addAltEntry("http://sierra-nevada.cs.man.ac.uk/chemmim-spin",
				"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim-spin.ttl");
		lm.addAltEntry("http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim",
				"http://sierra-nevada.cs.man.ac.uk/mim/chemmim/chemmim.ttl");

		// TODO - use the URL to check that this is a valid URL
		
		URL checklistURL = new URL(checklist);
		URL mappingURL = new URL(checklistMapping);
		Model baseModel;
		// Initialize system functions and templates
		SPINModuleRegistry.get().init();
		
		if(data != null){
			baseModel = data;
		}else{
			baseModel = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		}
		OntDocumentManager dm = OntDocumentManager.getInstance();
		dm.setProcessImports(false);
		
		// Import the owlrl spin implementation
		if(owlrl){
		baseModel.read("http://topbraid.org/spin/owlrl-all");
		}
		// Import the source data that we are validating
		
		 	baseModel.read("http://sierra-nevada.cs.man.ac.uk/mim/mim/mim",
					"http://purl.org/net/mim/ns", "TURTLE");
			// Import the mim-spin rules
		 	//baseModel.read("http://sierra-nevada.cs.man.ac.uk/mim/mim/mimspin",
			//		"http://purl.org/net/mim/mimspin", "TURTLE");
		 	baseModel.read("http://spinrdf.org/sp");
		
		 // Load the domain specific spin mapping rules	
			baseModel.read(mappingURL.toString(), "TURTLE");
			// Load the MIM Checklist
			baseModel.read(checklistURL.toString(), "TURTLE");
			//process the imports myself as Jena does a terrible job
		processImports(baseModel);

		// Create OntModel with imports
		
		OntModel ontModel = ModelFactory.createOntologyModel(
				OntModelSpec.OWL_MEM, baseModel);

		// Create and add Model for inferred triples
		Model newTriples = ModelFactory
				.createDefaultModel(ReificationStyle.Minimal);
		ontModel.addSubModel(newTriples);

		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(ontModel, null);
		//System.out.println("Templates:");
		//Collection<Template> templates = SPINModuleRegistry.get()
		//		.getTemplates();
		//for (Template t : templates) {
		//	System.out.println(t.getURI());
		//}
		//System.out.println("Functions:");
		//Collection<Function> functions = SPINModuleRegistry.get()
		//		.getFunctions();
		//for (Function f : functions) {
		//	System.out.println(f.getURI());
		//}
		
		ProgressMonitor pm = new SimpleProgressMonitor("Progress");
		// Run all inferences
		SPINInferences.run(ontModel, newTriples, null, null, false, null);
		System.out.println("Inferred triples: " + newTriples.size());
		
		
		return newTriples;
		
	}

	// TODO need to deal better with imported ontologies that are in ttl format
	public static Model processImports(Model model) {

		Property prop = ResourceFactory
				.createProperty("http://www.w3.org/2002/07/owl#imports");
		StmtIterator s = model.listStatements((Resource) null, prop,
				(RDFNode) null);
		Vector<String> imports = new Vector<String>();
		LocationMapper lm = LocationMapper.get();

		while (s.hasNext()) {
			Statement st = s.next();
			String o = st.getObject().toString();
			imports.add(o);
			System.out.println(st.toString());
		}

		for (String i : imports) {
			// See if there is an alternative location in the Location Mapper
			//System.out.println(i + " maps to");
			if (lm.getAltEntry(i) != null) {
				i = lm.getAltEntry(i);
				//System.out.println(i);
			} else {
				//System.out.println("nothing");
			}
			// See if we can make a crude stab at the file type
			try {
				String type = i.substring(i.length() - 3, i.length());
				System.out.println(i + " has type: " + type);
				if (type.equalsIgnoreCase("ttl")) {
					model.read(i, "TURTLE");
				} else {
					model.read(i);

				}
			} catch (Exception e) {
				System.out.println("Failed to import: " + e.getMessage()
						+ " : " + e.getCause());

			}
		}

		return model;
	}

}
