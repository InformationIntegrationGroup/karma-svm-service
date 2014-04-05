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

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ServerConfig;
import org.json.JSONObject;

@Path("svm")
public class SVM_Service {
	
	private Logger log = Logger.getLogger(SVM_Service.class.getName());

	
	private Util utilObj;
	
	public SVM_Service(@Context HttpHeaders headers) {
		utilObj = new Util();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/t1")
    public Response getIt(@Context HttpHeaders headers) {
    	Iterator<String> itr = headers.getRequestHeaders().keySet().iterator();
    	while(itr.hasNext()){
    		String k = itr.next();
    		log.info(k + " : " + headers.getRequestHeader(k));
    	}
    	return Response.status(200).entity("Got here").build();
    }
    
    
    @GET
	@Path("/train")
	@Produces(MediaType.TEXT_PLAIN)
    public Response trainingGET(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("linear") @QueryParam("kernel_type") String kernel_type, 
    		@DefaultValue("C-classification") @QueryParam("c-type") String c_type,
    		@DefaultValue("") @QueryParam("model_name") String model_name) {
    	
    	return Response.status(200).entity("Got here").build();
    
    }
    
    @POST
	@Path("/train")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
    public Response trainingPOST(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("linear") @QueryParam("kernel_type") String kernel_type, 
    		@DefaultValue("C-classification") @QueryParam("c-type") String c_type,
    		@DefaultValue("") @QueryParam("model_name") String model_name) {
    	
    	log.debug(String.format("%s %s", headers.getRequestHeader("Host"), headers.getRequestHeader("User-Agent")));
    	
    	// get the date format for file name generation
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
    	JSONObject summary = new JSONObject();
    	
    	kernel_type = kernel_type.trim().toLowerCase();
    	if(c_type.trim().length()< 1) {
    		c_type = "C-classification";
    	}
    	if(kernel_type.trim().length()< 1) {
    		kernel_type = "linear";
    	}
    	if(model_name.trim().length()< 1) {
    		model_name = "SVM_Model_" + kernel_type + "_" + sdf.format(Calendar.getInstance().getTime());
    	}
    	
    	// check if the model name is already present
    	if(utilObj.isModelNameUnique(model_name)) {
    		summary = new JSONObject();
    		summary.put("Error", "Duplicate model name : " + model_name);
    		log.error("Duplicate model name");
    		return Response.status(200).entity(summary.toString()).build();
    	}
    	
    	log.info(String.format("kernel: %s Classification: %s, model_Name: %s", kernel_type, c_type, model_name ));
    	String modelFilePath = Util.CurrentDir+"/models/"+model_name;
    	
    	try {

    		// write the data to the disk
    		final String inputFileName = Util.CurrentDir+"/temp_data_dm_service/"+"TrainData_" + kernel_type + "_" + sdf.format(Calendar.getInstance().getTime()) + ".csv";
    		FileWriter writer = new FileWriter(inputFileName);
    		writer.write(data);
    		writer.flush();
    		writer.close();
    		log.info("File created : " + inputFileName);
    		
    		String cmd = String.format("Rscript "+Util.CurrentDir+"/Rscripts/svmTraining.R %s %s %s %s ",inputFileName, kernel_type, c_type, model_name);
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
				if(line.trim().equalsIgnoreCase("Call:")) {
					line = results.readLine().trim();
					buf.append(line);
					summary.put("summary", line);
				} 
				else if(line.trim().equalsIgnoreCase("Parameters:")) {
					results.readLine();
					line = results.readLine();
					buf.append(line);
					while(!line.isEmpty()) {
						String s[] = line.split(":");
						summary.put(s[0].trim(), s[1].trim());
						line = results.readLine();
						buf.append(line);
					}
				} else if(line.trim().contains("Number of Support Vectors:")) {
					summary.put("Number of Support Vectors", line.split(":")[1]);
				}  else if(line.trim().contains("Number of Classes:")) {
					summary.put("Number of Classes", line.trim().split(":")[1]);
				} else if(line.trim().equalsIgnoreCase("Levels:")) {
					line = results.readLine().trim();
					buf.append(line);
					summary.put("Levels", line.split(" "));
				} 
				summary.put("model_name", model_name + ".RData");
			}
			summary.put("raw", buf.toString());
			log.info(buf.toString());
			
			if(summary.optString("Number of Support Vectors",null) == null) {
				summary = new JSONObject();
				summary.put("Error", buf.toString());
			} else {
				log.info("Model " + model_name +" created!!");
				utilObj.insertExecutionInfo(summary, headers.getRequestHeader("Host").toString(), "Rscripts/svmTraining.R", model_name, inputFileName);
			}
			
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
    	return Response.status(200).entity(summary.toString()).build();
	}
    
    
    @GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
    public Response testingGET(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @QueryParam("model_name") String model_name) {
    	
    	log.info(String.format("%s %s", headers.getRequestHeader("Host"), headers.getRequestHeader("User-Agent")));
    	return Response.status(200).entity("Got here").build();
    
    }
    
    @POST
	@Path("/test")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
    public Response testingPOST(
    		String data,
    		@Context HttpHeaders headers,
    		@DefaultValue("") @QueryParam("model_name") String model_name) {
    	
    	log.info(String.format("%s %s", headers.getRequestHeader("Host"), headers.getRequestHeader("User-Agent")));
    	
    	
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
    	
    	final String predictedFilePath = Util.CurrentDir+"/temp_data_dm_service/"+"Prediction_" +model_name;
    	final String confusionMatrixFilePath = Util.CurrentDir+"/temp_data_dm_service/"+"Matrix_" +model_name;
    	
    	// write the data to the disk
    	final String inputFileName = Util.CurrentDir+"/temp_data_dm_service/"+"TestDataSet_" + ts + ".csv";
    	try {
    		
    		FileWriter writer = new FileWriter(inputFileName);
    		writer.write(data);
    		writer.flush();
    		writer.close();
    		log.info("File created : " + inputFileName);
    		
    		String cmd = String.format("Rscript "+Util.CurrentDir+"/Rscripts/svmTesting.R %s %s %s %s",
    				inputFileName,
    				Util.CurrentDir+"/models/"+model_name,
    				predictedFilePath,
    				confusionMatrixFilePath);
    		log.info(cmd);
	    	Process pr = Runtime.getRuntime().exec(cmd);
			
			BufferedReader results = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			
			StringBuilder buf = new StringBuilder();
			while((line=results.readLine()) != null) {
				buf.append(line);
				log.info(line);
				if(line.trim().isEmpty()) {
					continue;
				}
				if(line.trim().contains("Accuracy:")) {
					summary.put("Accuracy", line);
				} 
			}
			summary.put("raw", buf.toString());
			log.info(buf.toString());
			
			if(summary.optString("Accuracy",null) == null) {
				summary = new JSONObject();
				summary.put("Error", buf.toString());
			} else {
				summary.put("ConfusionMatrixPath", "Matrix_" +model_name);
				summary.put("PredictionPath", "Prediction_" +model_name);
				utilObj.insertExecutionInfo(summary, headers.getRequestHeader("Host").toString(), "svmTraining.R", model_name, inputFileName);
			}
			
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
    	return Response.status(200).entity(summary.toString()).build();
	}
    
//    
//    @POST
//	@Path("/upload_svm")
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	public String uploadFile(
//		@FormDataParam("file") InputStream uploadedInputStream,
//		@FormDataParam("file") FormDataContentDisposition fileDetail,
//		@QueryParam("kernel") String kernel_type) {
// 
//    	// get the date format for file name generation
//    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
//    	String modelName = kernel_type + "_" + sdf.format(Calendar.getInstance().getTime()) + "_model";
//    	
//    	// write the data to the disk
//    	String inputFileName = kernel_type+"_"+System.nanoTime();
//    	
//		String uploadedFileLocation = "./" + inputFileName;
// 
//		// save the file
//		if(!writeToFile(uploadedInputStream, uploadedFileLocation) ) {
//			System.out.println("\n\nError... could not write file");
////			return Response.status(400).entity("Error").build();
//			return "Error";
//		}
//		
//		try {
//    		String line = "";
//	    	Process pr = Runtime.getRuntime().exec("Rscript svmTraining.R "+ inputFileName + " " + kernel_type + " " + modelName);
//			System.out.println("Model created!!");
//			BufferedReader results = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//			String out=null;
//			while((out=results.readLine()) != null) {
//				System.out.println(line);
//			}  
//    	} catch (Exception e) {
//    		e.printStackTrace();
////    		return Response.status(400).entity(e.getMessage()).build();
//    		return e.getMessage();
//    	}
//		
//		return "success" ;
////				Response.status(200).entity(inputFileName).build();
// 
//	}
    
    
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

