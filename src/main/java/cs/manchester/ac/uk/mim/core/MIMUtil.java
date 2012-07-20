package cs.manchester.ac.uk.mim.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.util.LocationMapper;
import com.hp.hpl.jena.util.ResourceUtils;


/**
 * Hello world!
 *
 */
public class MIMUtil 
{
	//TODO Fix to actually give the string rather than write out to file
    public static String getTurtle(String file) throws Exception
    {

    	Model model = ModelFactory.createDefaultModel();
    	
    	model.read("http://www.chemspider.com/Chemical-Structure.7787.rdf");
    	
        
    	model.write(System.out,"TURTLE");
        
        skollemize("http://www.chemspider.com/Chemical-Structure.7787.rdf", "resources/Chemical-Structure.7787.skollemized.rdf");
   
        return "";
    }


    public static Model skollemize(String url, String out) throws Exception {

		// Skolemize rdf from chemspider - bloody blank nodes!
		Model model = ModelFactory.createDefaultModel();
		model.read(url);

		// find blank nodes in res and add them to blanks
		ResIterator res = model.listSubjects();
		Vector<Resource> blanks = new Vector<Resource>();
		while (res.hasNext()) {
			Resource r = res.next();
			if (r.isAnon()) {
				blanks.add(r);
			}// if
		}// while
			// Now iterate through blanks and name the resource using the unique ID Jena has assigned
		Iterator<Resource> i = blanks.iterator();
		while (i.hasNext()) {
			Resource a = i.next();
			System.out.println(a.getId().toString());
			ResourceUtils.renameResource(a, a.getId().getLabelString());
		}

		model.write(new FileOutputStream(out), "N-TRIPLE");

		// read in chemspider data
		Model resultModel = ModelFactory.createDefaultModel();
		InputStream spider = new BufferedInputStream(new FileInputStream(out));
		resultModel.read(spider, null, "N-TRIPLE");

		return resultModel;
	}

    public static ArrayList<String> listFromResultSet(ResultSet rs,
			String resource) {

		ArrayList<String> uris = new ArrayList<String>();
		while (rs.hasNext()) {

			QuerySolution sol = rs.next();
			Resource pageResource = sol.getResource(resource);
			String pageString = pageResource.toString();
			uris.add(pageString);
		}
		return uris;
	}
    
    
    public static ArrayList<String> listFromTextFile(String filename)
			throws Exception {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		ArrayList<String> uris = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			uris.add(line);
		}
		return uris;
	}






}
