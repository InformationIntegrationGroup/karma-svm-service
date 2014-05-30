package edu.isi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ServerConfig;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("dtree")
public class DecisionTreeService {
	
	private Logger log = Logger.getLogger(DecisionTreeService.class.getName());

	private Util utilObj;
	private UriInfo uriInfo;
	
	public DecisionTreeService(@Context HttpHeaders headers, 
			@Context UriInfo uri) {
		this.utilObj = new Util();
		this.uriInfo = uri;
	}
	
    @GET
	@Path("/train/{model_name}")
	@Produces(MediaType.TEXT_PLAIN)
    public Response trainingGET(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @PathParam("model_name") String model_name,
    		@DefaultValue("") @QueryParam("tag") String tag_name) {
    	log.info(this.uriInfo.getPath());
    	return Response.status(200).entity("The Decission Tree training service is invoked using a POST request. It accepts data in the POST payload").build();
    
    }
    
    @POST
	@Path("/train")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
    public Response trainingPOST(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @QueryParam("tag") String tag_name) {
    	
    	return performDT_training("", data);
    }
    
    private Response performDT_training(String model_name, String data) {
    	
    	// get the date format for file name generation
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
    	JSONObject summary = new JSONObject();
    	
    	if(model_name.trim().length()< 1) {
    		model_name = "DTree_Model_" + sdf.format(Calendar.getInstance().getTime());
    	}
    	
    	// check if the model name is already present
    	if(utilObj.isModelNameUnique(model_name)) {
    		summary = new JSONObject();
    		summary.put("Error", "Duplicate model name : " + model_name);
    		log.error("Duplicate model name");
    		return Response.status(200).entity(summary.toString()).build();
    	}
    	
    	log.info(String.format("model_Name: %s", model_name ));
    	String modelFilePath = Util.CurrentDir+"/models/"+model_name;
    	
    	try {

    		// write the data to the disk
    		final String inputFileName = "TrainData_DT_" + sdf.format(Calendar.getInstance().getTime()) + ".csv";
    		final String inputFilePath = Util.CurrentDir+"/"+Util.DataDir+"/"+inputFileName;
    		FileWriter writer = new FileWriter(inputFilePath);
    		writer.write(data);
    		writer.flush();
    		writer.close();
    		log.info("File created : " + inputFilePath);
    		
    		String cmd = String.format("Rscript "+Util.CurrentDir+"/Rscripts/DTTraining.R %s %s ",inputFilePath, model_name);
    		log.info(cmd);
	    	Process pr = Runtime.getRuntime().exec(cmd);
			
			BufferedReader results = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			
			StringBuilder buf = new StringBuilder();
			while((line=results.readLine()) != null) {
				buf.append(line);
				if(line.trim().isEmpty()) {
					continue;
				}
				if(line.trim().equalsIgnoreCase("Primary splits:")) {
					line = results.readLine().trim();
					buf.append(line);
					summary.put("summary", line);
				} 
				else if(line.trim().equalsIgnoreCase("Primary splits:")) {
					line = results.readLine();
					buf.append(line);
					JSONArray Psplits = new JSONArray();					
					while(!line.trim().equalsIgnoreCase("Surrogate splits:")) {
						String s[] = line.split("\t");
						for(String s1 : s) {
							System.out.println(s1);
						}
						JSONArray splitCols  = new JSONArray(s);
						line = results.readLine();
						buf.append(line);
					}
				} 
				summary.put("model_name", model_name + ".RData");
				summary.put("InputFile", inputFileName);
				summary.put("CommandName", "Rscripts/DTTraining.R");
			}
			summary.put("raw", buf.toString());
			
			// format the summary json as params { [key : val] }
			JSONArray finalSummary = new JSONArray();
			Iterator<String> keys =  summary.keySet().iterator();
			while(keys.hasNext()) {
				JSONObject row = new JSONObject();
				row.put("attribute", keys.next());
				row.put("value", summary.get(row.getString("attribute")));
				finalSummary.put(row);
			}
			return Response.status(200).entity(new JSONObject().put("summary", finalSummary).toString()).build();
			
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
    	return Response.status(200).entity(summary.toString()).build();
    }
    
    @POST
	@Path("/train/{model_name}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
    public Response trainingPOST(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @PathParam("model_name") String model_name,
    		@DefaultValue("") @QueryParam("tag") String tag_name) {
    	return performDT_training(model_name, data);
    	
	}
    
    
    @GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
    public Response testingGET(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @QueryParam("model_name") String model_name,
    		@DefaultValue("") @QueryParam("tag") String tag_name) {
    	
    	log.info(this.uriInfo.getPath());
    	return Response.status(200).entity("Got here").build();
    
    }
    
    @POST
	@Path("/test/{model_name}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
    public Response testingPOST(
    		String data,
    		@Context HttpHeaders headers,
    		@PathParam("model_name") String model_name,
    		@DefaultValue("") @QueryParam("tag") String tag_name) {
    	
    	log.info(this.uriInfo.getPath());
    	
    	
    	// get the date format for file name generation
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
    	String ts = sdf.format(Calendar.getInstance().getTime());
    	JSONObject summary = new JSONObject();
    	
    	if(model_name.trim().length()< 1) {
    		summary = new JSONObject();
    		summary.put("Error", "Model name not specifed");
    		log.error("Model name not specifed");
    		return Response.status(200).entity(summary.toString()).build();
    	}
    	
    	// check if model is present
    	File f = new File(Util.CurrentDir + "/models/" + model_name);
    	if(!f.exists()) {
    		summary = new JSONObject();
    		summary.put("Error", "Model file could not be located (" + model_name + ")");
    		log.error("Model file could not be located (" + model_name + ")");
    		return Response.status(200).entity(summary.toString()).build();
    	}
    	
    	String model_name_trimmed = model_name;
    	if(model_name.contains(".RData")) {
    		model_name_trimmed = model_name_trimmed.substring(0, model_name_trimmed.indexOf(".RData"));
    	}
    	
    	final String predictedFileName = "Prediction_" +model_name_trimmed +".csv";
    	final String predictedFilePath = Util.CurrentDir+"/"+Util.DataDir+"/"+predictedFileName;
    	
    	final String confusionMatrixFileName = "Matrix_" +model_name_trimmed +".csv";
    	final String confusionMatrixFilePath = Util.CurrentDir+"/"+Util.DataDir+"/"+confusionMatrixFileName;
    	
    	final String inputFileName = "TestDataSet_" + ts + ".csv";
    	final String inputFilePath = Util.CurrentDir+"/"+Util.DataDir+"/"+inputFileName;
    	
    	try {
    		// write the data to the disk
    		FileWriter writer = new FileWriter(inputFilePath);
    		writer.write(data);
    		writer.flush();
    		writer.close();
    		log.info("File created : " + inputFilePath);
    		

    		// execute the R script
    		String cmd = String.format("Rscript "+Util.CurrentDir+"/Rscripts/DTTesting.R %s %s %s %s",
    				inputFilePath,
    				Util.CurrentDir+"/models/"+model_name,
    				predictedFilePath,
    				confusionMatrixFilePath);
    		log.info(cmd);
	    	Process pr = Runtime.getRuntime().exec(cmd);
			
	    	
			BufferedReader results = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			
			// parse the output of the Rscript
			StringBuilder buf = new StringBuilder();
			while((line=results.readLine()) != null) {
				buf.append(line);
				log.info(line);
				if(line.trim().contains("Execution halted")) {
					summary.put("Error", "true");
				} 
			}
			summary.put("raw", buf.toString());			// add the output of the rscript to the json
			
			if(summary.optString("Error",null) != null) {
				summary = new JSONObject();
				summary.put("Error", buf.toString());
			} else {
				summary.put("ConfusionMatrixFileName", confusionMatrixFileName);
				summary.put("PredictionFileName", predictedFileName);
				summary.put("InputFileName", inputFileName);
				summary.put("CommandName", "Rscripts/svmTesting.R");
				utilObj.insertExecutionInfo(UUID.randomUUID().toString(), this.uriInfo.getPath(), tag_name, summary);
				
				// now add the urls to the get matrix and prediction file
				summary.put("ConfusionMatrixPath", this.uriInfo.getBaseUri().toString()+"data/csv/"+confusionMatrixFileName);
				summary.put("PredictionPath", this.uriInfo.getBaseUri().toString()+"data/csv/"+predictedFileName);
				summary.put("TestFilePath", this.uriInfo.getBaseUri().toString()+"data/csv/"+inputFileName);
				
			}
			
			// format the summary json as params { [key : val] }
			JSONArray attrs = new JSONArray();
			Iterator<String> keys =  summary.keySet().iterator();
			while(keys.hasNext()) {
				JSONObject row = new JSONObject();
				row.put("attribute", keys.next());
				row.put("value", summary.get(row.getString("attribute")));
				attrs.put(row);
			}
			JSONObject finalSummary = new JSONObject();
			finalSummary.put("confusionMatrix", this.utilObj.parseConfusionMatrix(confusionMatrixFilePath));
			finalSummary.put("attributes", attrs);
			finalSummary.put("prediction", this.utilObj.csv2json(predictedFilePath));
			
			return Response.status(200).entity(new JSONObject().put("summary", finalSummary).toString()).build();
			
			
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
    	return Response.status(200).entity(summary.toString()).build();
	}
    
    
    // save uploaded file to new location
 	private boolean writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
 		boolean retVal = false;
 		try {
 			OutputStream out = new FileOutputStream(new File( uploadedFileLocation));
 			int read = 0;
 			byte[] bytes = new byte[1024];
  
 			out = new FileOutputStream(new File(uploadedFileLocation));
 			while ((read = uploadedInputStream.read(bytes)) != -1) {
 				out.write(bytes, 0, read);
 			}
 			out.flush();
 			out.close();
 			System.out.println("File uploaded to : " + uploadedFileLocation);
 			retVal = true;
 		} catch (Exception e) {
  
 			e.printStackTrace();
 		}
 		return retVal;
  
 	}
  
 
}

