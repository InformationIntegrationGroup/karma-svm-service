package edu.isi;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author shri
 * */
public class Util {
	
	public enum ModelKeys{
		ModelName, Host, Key, Value, DataFile, Command,
	}

	private static Connection conn;
	private Logger log = Logger.getLogger(Util.class);
	public static final String CurrentDir = System.getProperty("user.dir");
	public static final String SQLiteDB = "services_db.sqlite";
	
	static {
		try {
			// initialize the directory is not present
			Logger log = Logger.getLogger(Util.class);
	        File f = new File(Util.CurrentDir + "/temp_data_dm_service");
	        if(!f.exists()) {
	        	log.info("Creating dir:" +Util.CurrentDir + "/temp_data_dm_service");
	        	f.mkdir();
	        }
	        // initialize the model directory is not present
	        f = new File(Util.CurrentDir + "/models");
	        if(!f.exists()) {
	        	log.info("Creating dir:" +Util.CurrentDir + "/models");
	        	f.mkdir();
	        }
	        f = new File(Util.CurrentDir + "/" + SQLiteDB);
	        if(!f.exists()) {
	        	log.info("Creating SQLite database:" +Util.CurrentDir + "/" + SQLiteDB);
	        	Util.initSQLite();
	        } else {
	        	Class.forName("org.sqlite.JDBC");
				System.out.println(CurrentDir+"/" + SQLiteDB);
				conn = DriverManager.getConnection("jdbc:sqlite:"+CurrentDir+"/"+SQLiteDB);
	        }
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initSQLite() {

		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:"+CurrentDir+"/"+SQLiteDB);
			Statement statement = conn.createStatement();

			statement.executeUpdate("CREATE TABLE \"services\" (\"Id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Name\" TEXT NOT NULL , \"Description\" TEXT, \"Url\" TEXT)");
			statement.executeUpdate("CREATE TABLE \"service_params\" (\"Id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Name\" TEXT NOT NULL , \"Description\" TEXT, \"ServiceId\" INTEGER NOT NULL , \"default_value\" TEXT)");
			statement.executeUpdate("CREATE TABLE \"service_executions\" (\"Id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"Host\" TEXT, \"ModelName\" TEXT, \"Key\" TEXT, \"Value\" TEXT)");

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertExecutionInfo(JSONObject json, String host, String command, String modelName, String dataFile) {
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement("Insert into service_executions (Host, ModelName, Key, Value) values (?, ?, ?, ?)");
			Iterator<String> itr = json.keys();
			while(itr.hasNext()) {
				String val = "";
				String k = itr.next();
				statement.setString(1, host);
				statement.setString(2, modelName);
				statement.setString(3, k);
				try {
					val = json.getString(k);
				} catch (Exception e) {
					val = JSONObject.valueToString(json.get(k));
				}
				statement.setString(4, val);
				statement.addBatch();
			}
			if(dataFile != null ) {
				statement.setString(1, host);
				statement.setString(2, modelName);
				statement.setString(3, ModelKeys.DataFile.name());
				statement.setString(4, dataFile);
				statement.addBatch();
			}
			if(command != null ) {
				statement.setString(1, host);
				statement.setString(2, modelName);
				statement.setString(3, ModelKeys.Command.name());
				statement.setString(4, command);
				statement.addBatch();
			}
			log.info("Executing the batch query : " + statement.executeBatch());
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public boolean isModelNameUnique(String name) {
		try {
			PreparedStatement stmt = conn.prepareStatement("select distinct ModelName from service_executions where ModelName like ?");
			stmt.setString(1, name);
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				rs.close();
				stmt.close();
				return true;
			}
		
		} catch (Exception e) {
			log.error(e);
		}
		return false;
	}
	
	public JSONObject getAllExecution() {
		return getAllExecution(null);
	}
	
	public JSONObject getAllExecution(String name) {
		JSONObject retVal = new JSONObject();
		PreparedStatement stmt;
		try {
			if(name == null || name.trim().isEmpty()) {
				stmt = conn.prepareStatement("select distinct * from service_executions order by ModelName asc");
			} else {
				stmt = conn.prepareStatement("select distinct * from service_executions where ModelName like ?");
				stmt.setString(1, name);
			}
			ResultSet rs = stmt.executeQuery();
			JSONArray arr = new JSONArray();
			JSONObject obj = new JSONObject();
			String prevModel = "";
			while(rs.next()) {
				if(!prevModel.equalsIgnoreCase(rs.getString(ModelKeys.ModelName.name()))) {
					prevModel = rs.getString(ModelKeys.ModelName.name());
					arr.put(obj);
					obj = new JSONObject();
					obj.put(rs.getString(ModelKeys.Key.name()), rs.getString(ModelKeys.Value.name()));
				} else {
					obj.put("Host", rs.getString("Host"));
					obj.put(ModelKeys.ModelName.name(), rs.getString(ModelKeys.ModelName.name()));
					obj.put(rs.getString(ModelKeys.Key.name()), rs.getString(ModelKeys.Value.name()));
				}
			}
			arr.put(obj);
			retVal.put("models", arr);
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return retVal;
	}

	public JSONObject getAllServices() {
		
		JSONObject retVal = new JSONObject();
		PreparedStatement stmt;
		try {
			//stmt = conn.prepareStatement("select a1.*, a2.Id as ParamId, a2.Name as ParamName, a2.Description as ParamDesc, a2.default_value from services a1 join service_params a2 on a1.Id = a2.ServiceId order by a1.Id asc");
			stmt = conn.prepareStatement("select * from services order by Id asc");
			ResultSet rs = stmt.executeQuery();
			List<JSONObject> services = new ArrayList<JSONObject>();
			JSONObject obj = new JSONObject();
			while(rs.next()) {
				obj = new JSONObject();
				obj.put("Id", rs.getInt("Id"));
				obj.put("Name", rs.getString("Name"));
				obj.put("Description", rs.getString("Description"));
				obj.put("Url", rs.getString("Url"));
				services.add(obj);
			}
			stmt = conn.prepareStatement("select * from service_params where ServiceId = ? order by Id asc");
			for(JSONObject jObj : services) {
				stmt.setInt(1, jObj.getInt("Id"));
				rs = stmt.executeQuery();
				JSONArray paramArr = new JSONArray();
				while(rs.next()) {
					obj = new JSONObject();
					obj.put("Id", rs.getInt("Id"));
					obj.put("Name", rs.getString("Name"));
					obj.put("Description", rs.getString("Description"));
					obj.put("default_value", rs.getString("default_value"));
					paramArr.put(obj);
				}
				jObj.put("params", paramArr);
			}
			retVal.put("models", services);
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return retVal;
		
	}
}
