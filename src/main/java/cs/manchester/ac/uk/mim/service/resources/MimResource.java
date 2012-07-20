package cs.manchester.ac.uk.mim.service.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import cs.manchester.ac.uk.mim.core.Mim;

@Path("/mim")
public class MimResource {

	// The Java method will process HTTP GET requests
    @GET 
	// The Java method will produce content identified by the MIME Media
	// type "text/plain"
	@Produces("text/plain")
	@Path("/validate")
	public String validate(@QueryParam("checklist") String checklist,
			@QueryParam("data") String data,
			@QueryParam("mapping") String mapping) throws Exception {
		// TODO check for null values
		String response;
		try{
		response = Mim.validate(data, checklist, mapping,false);
		return response;
		}
		catch(Exception e){
			response = "There was an issue processing the request: " + e.getCause(); 
			System.out.println(response);
			return (response);
		}
		
	}
}
