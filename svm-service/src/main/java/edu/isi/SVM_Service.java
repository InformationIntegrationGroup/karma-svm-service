package edu.isi;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
//import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("svm")
public class SVM_Service {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
    @POST
	@Path("/train")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response training(String data,@QueryParam("kernel_type") String kernel_type ) {
    	kernel_type = kernel_type.trim().toLowerCase();
    	String path=System.getProperty("user.dir");
    	String newline=System.getProperty("line.separator");
    	String response="";
    	// get the date format for file name generation
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
    	String modelName = kernel_type + "_" + sdf.format(Calendar.getInstance().getTime()) + "_model";
    	String modelpath=path+"/Rscripts/"+modelName;
    	String fileName=path+"/Rscripts/Train_"+sdf.format(Calendar.getInstance().getTime())+".csv";
    	String command="";
    	//converting the data into csv format
    	try {
			FileWriter writer = new FileWriter(fileName);
			String[] dataArray=data.split(newline);
	    	for(String d:dataArray)
	    	{
	    		String[] row=d.split("\t");
	    		for(int i=0;i<row.length;i++)
	    		{
	    			writer.append(row[i]);
	    			if(i!=row.length-1)
	    			writer.append(",");
	    		}
	    		writer.append(newline);
	    	}
	    	writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	try {
    		command="Rscript "+path+"/Rscripts/svmTraining.R "+fileName+ " " + kernel_type + " " + modelpath;
	    	Process pr = Runtime.getRuntime().exec(command);
	    	BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line=null;
            response=modelName+" created!!"+newline;
           while((line=input.readLine()) != null) {
               response+=line;
               response+=newline;
            }
			 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return Response.status(200).entity(response).build();
	}
    

    @POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_PLAIN)
	public Response testing(String data,@QueryParam("model-name") String model_name ) {
    	model_name = model_name.trim().toLowerCase();
    	String path=System.getProperty("user.dir");
    	String newline=System.getProperty("line.separator");
    	String response="";
    	// get the date format for file name generation
    	SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HmsS");
    	String modelpath=path+"/Rscripts/"+model_name;
    	String fileName=path+"/Rscripts/Test_"+sdf.format(Calendar.getInstance().getTime())+".csv";
    	String command="";
    	//converting the data into csv format
    	try {
			FileWriter writer = new FileWriter(fileName);
			String[] dataArray=data.split(newline);
	    	for(String d:dataArray)
	    	{
	    		String[] row=d.split("\t");
	    		for(int i=0;i<row.length;i++)
	    		{
	    			writer.append(row[i]);
	    			if(i!=row.length-1)
	    			writer.append(",");
	    		}
	    		writer.append(newline);
	    	}
	    	writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	try {
    		command="Rscript "+path+"/Rscripts/svmTesting.R "+fileName+ " " + modelpath;
	    	Process pr = Runtime.getRuntime().exec(command);
	    	BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line=null;
            response="Testing phase completed!! Here is the confusion matrix...."+newline;
           while((line=input.readLine()) != null) {
               response+=line;
               response+=newline;
            }
			 
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return Response.status(200).entity(response).build();
	}

  
 
}

