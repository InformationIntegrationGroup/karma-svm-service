package edu.isi;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.json.JSONObject;

@Path("service")
public class Services {
	
	private Logger log = Logger.getLogger(Services.class.getName());
	private Util utilObj;
	private UriInfo uriInfo;
	public Services(@Context HttpHeaders headers,
			@Context UriInfo uri) {
		this.utilObj = new Util();
		this.uriInfo = uri;
	}
	
	
	
    @GET
	@Path("/executions/{model_name}")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getExecutionsByName(
    		@Context HttpHeaders headers,
    		@PathParam("model_name") String model_name) {
    	
    	log.info(this.uriInfo.getPath());
    	JSONObject json = utilObj.getAllExecution(model_name.trim());
    	return Response.status(200).entity(json.toString()).build();
    }
    @GET
	@Path("/executions")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getExecutions(
    		@Context HttpHeaders headers) {
    	
    	log.info(this.uriInfo.getPath());
    	JSONObject json = utilObj.getAllExecution();
    	return Response.status(200).entity(json.toString()).build();
    }
    
    
    @GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
    public Response getModelDetailByName(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @QueryParam("model_name") String model_name) {
    	
    	log.info(this.uriInfo.getPath());
    	JSONObject json = utilObj.getAllServices(this.uriInfo);
    	return Response.status(200).entity(json.toString()).build();
    }
    
    @GET
    @Path("/testCSV")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testCSV(
    		@Context HttpHeaders headers) {
    	
    	log.info(this.uriInfo.getPath());
    	return Response.status(200).entity(this.utilObj.parseConfusionMatrix(Util.CurrentDir + "/temp_data_dm_service/Matrix_SVM_Model_linear_06-Apr-2014_235517756.csv").toString()).build();
    }
    
    

}
