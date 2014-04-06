package edu.isi;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

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

import org.apache.log4j.Logger;
import org.eclipse.persistence.tools.schemaframework.FieldDefinition;
import org.json.JSONObject;

@Path("data")
public class DataRetrievingService {
	
	private Logger log = Logger.getLogger(DataRetrievingService.class.getName());
	public DataRetrievingService(@Context HttpHeaders headers) {
		
	}
	
	
	/**
	 * get any file as csv
	 * */
    @GET
	@Path("/csv/{file_name}")
    @Produces({"text/csv"})
    public Response getFileByName(
    		@Context HttpHeaders headers,
    		@PathParam("file_name") String file_name) {
    	
//    	log.info(String.format("%s | %s | %s", "/data/csv", headers.getRequestHeader("Host"), headers.getRequestHeader("User-Agent")));
    	try {
	    	File f = new File(Util.CurrentDir + "/" + Util.DataDir+"/"+file_name);
	    	if(f.exists()) {
	    		return Response.ok().entity(new FileInputStream(f)).build();
	    	} 
    	} catch (Exception e) {
    		log.error("Error reading the file. ",e);
    	}
    	return Response.status(500).entity("Could not locate file").build();
    }
    
    
    
    @GET
	@Path("/csv")
    @Produces({"text/csv"})
    public Response getAllFile(
    		@Context HttpHeaders headers ) {
    	
    	StringBuilder buf = new StringBuilder();
    	String path = Util.CurrentDir+"/"+Util.DataDir; 
    	String files;
    	File folder = new File(path);
    	File[] listOfFiles = folder.listFiles(); 
    	for (int i = 0; i < listOfFiles.length; i++) 
    	{
    		if (listOfFiles[i].isFile()) 
    		{
    			files = listOfFiles[i].getName();
    			buf.append(files).append("\n");
    		}
    	}
    	return Response.status(200).entity(buf.toString()).build();
    }
    
}
