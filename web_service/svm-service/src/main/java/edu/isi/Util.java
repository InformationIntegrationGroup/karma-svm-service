package edu.isi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

/**
 * @author shri
 * */
public class Util {
	
	public enum ModelKeys{
		Id, ServiceUrl, Tag, Key, Value, createdOn
	}

	private static Connection conn;
	private Logger log = Logger.getLogger(Util.class);
	public static final String CurrentDir = System.getProperty("user.dir");
	public static final String SQLiteDB = "services_db.sqlite";
	public static final String DataDir = "temp_data_dm_service";
	
	static {
		try {
			// initialize the directory is not present
			Logger log = Logger.getLogger(Util.class);
	        File f = new File(Util.CurrentDir + "/" + DataDir);
	        if(!f.exists()) {
	        	log.info("Creating dir:" +Util.CurrentDir + "/" + DataDir);
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
			statement.executeUpdate("CREATE TABLE \"service_executions\" (\"Id\" TEXT NOT NULL ,\"ServiceUrl\" TEXT DEFAULT (null) ,\"Tag\" TEXT DEFAULT (null) ,\"Key\" TEXT,\"Value\" TEXT,\"ValueType\" TEXT, \"createdOn\" DATETIME)");

		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ExecId
	 * @param ServiceUrl
	 * @param tagName
	 * @param
	 */
	public void insertExecutionInfo(String ExecId, String ServiceUrl, String tagName, JSONObject json) {
		PreparedStatement statement;
		try {
			statement = conn.prepareStatement("Insert into service_executions (Id, ServiceUrl, Tag, Key, Value, createdOn) values (?, ?, ?, ?, ?, datetime('now'))");
			Iterator<String> itr = json.keys();
			while(itr.hasNext()) {
				String val = "";
				String k = itr.next();
				statement.setString(1, ExecId);
				statement.setString(2, ServiceUrl);
				statement.setString(3, tagName);
				statement.setString(4, k);
				val = json.optString(k, null);
//				if(val == null) {
//					JSONArray ar = json.optJSONArray(k);
//					if(ar == null) {
//						val = json.getJSONObject(k).toString();
//					} else {
//						val = ar.toString();
//					}
//				}
				try {
					val = json.getString(k);
				} catch (Exception e) {
					val = JSONObject.valueToString(json.get(k));
				}
				statement.setString(5, val);
				statement.addBatch();
			}
			statement.executeBatch();
			log.info("Saved ServiceExecution Id: " + ExecId + " Url : " + ServiceUrl + " Tag: "+ tagName);
			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public boolean isModelNameUnique(String name) {
		try {
			PreparedStatement stmt = conn.prepareStatement("select distinct Value as model_name from service_executions where Key = 'model_name' and Value like ?");
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
				stmt = conn.prepareStatement("select distinct * from service_executions order by createdOn, Id desc");
			} else {
				stmt = conn.prepareStatement("select  * from service_executions where Id = (select Id from service_executions where Key = 'model_name' and Value like ? ) order by createdOn, Id desc");
				stmt.setString(1, name);
			}
			ResultSet rs = stmt.executeQuery();
			JSONArray arr = new JSONArray();
			JSONArray attrs = new JSONArray();
			JSONObject obj = new JSONObject();
			String prevModel = "";
			while(rs.next()) {
				log.info(rs.getString(ModelKeys.Id.name()));
				if(!prevModel.equalsIgnoreCase(rs.getString(ModelKeys.Id.name()))) {
					prevModel = rs.getString(ModelKeys.Id.name());
					arr.put(obj);
					obj = new JSONObject();
					attrs = new JSONArray();
					obj.put(ModelKeys.ServiceUrl.name(), rs.getString(ModelKeys.ServiceUrl.name()));
					obj.put(ModelKeys.Id.name(), rs.getString(ModelKeys.Id.name()));
					obj.put(ModelKeys.Tag.name(), rs.getString(ModelKeys.Tag.name()));
					obj.put(ModelKeys.createdOn.name(), rs.getString(ModelKeys.createdOn.name()));
					
					JSONObject row = new JSONObject();
					row.put("attribute", rs.getString(ModelKeys.Key.name()));
					row.put("value", rs.getString(ModelKeys.Value.name()));
					attrs.put(row);					
					obj.put("summary", attrs);
					
					
				} else {
					JSONObject row = new JSONObject();
					row.put("attribute", rs.getString(ModelKeys.Key.name()));
					row.put("value", rs.getString(ModelKeys.Value.name()));
					obj.getJSONArray("summary").put(row);
					
//					obj.put(rs.getString(ModelKeys.Key.name()), rs.getString(ModelKeys.Value.name()));
				}
			}
			arr.put(obj);
			retVal.put("models", arr);
		
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return retVal;
	}

	public JSONObject getAllServices(UriInfo uriInfo) {
		
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
				obj.put("Url", uriInfo.getBaseUri()+rs.getString("Url"));
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
	
	
	public JSONArray parseConfusionMatrix(String pathToCsvFile) {
		ArrayList<JSONObject> retVal = new ArrayList<JSONObject>();
		
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		File csvFile = new File(pathToCsvFile);
		if(!csvFile.exists()) {
			JSONObject err = new JSONObject();
			log.error("Could not locate confusion matrix file : " + pathToCsvFile);
			err.put("Error", "Could not locate/generate csv file : ");
			return  new JSONArray().put(err);
		}
		try {
			MappingIterator<Object[]> it = mapper.reader(Object[].class).readValues(csvFile);
			// get the headers first
			ArrayList<String> headers = new ArrayList<String>();
			JSONObject data = new JSONObject();
			if(it.hasNext()) {
				Object[] row = it.next();
				for(Object o : row) {
					String val = o.toString().trim();
					if(val.isEmpty()) {
						headers.add("class");
					} else {
						headers.add(val);
					}
				}
			}
			// get the data
			while (it.hasNext()) {
			  Object[] row = it.next();
			  data = new JSONObject();
			  int idx = 0;
			  for(Object o : row) {
				  data.put(headers.get(idx), o);
				  idx++;
			  }
			  retVal.add(data);			  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray(retVal);
	}
	
	
	
	public JSONArray csv2json(String pathToCsvFile) {
		ArrayList<JSONObject> retVal = new ArrayList<JSONObject>();
		
		CsvMapper mapper = new CsvMapper();
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		File csvFile = new File(pathToCsvFile);
		if(!csvFile.exists()) {
			JSONObject err = new JSONObject();
			log.error("Could not locate csv file : " + pathToCsvFile);
			err.put("Error", "Could not locate/generate csv file : ");
			return  new JSONArray().put(err);
		}
		try {
			MappingIterator<Object[]> it = mapper.reader(Object[].class).readValues(csvFile);
			// get the headers first
			ArrayList<String> headers = new ArrayList<String>();
			JSONObject data = new JSONObject();
			if(it.hasNext()) {
				Object[] row = it.next();
				for(Object o : row) {
					String val = o.toString().trim();
					headers.add(val);
				}
			}
			// get the data
			while (it.hasNext()) {
			  Object[] row = it.next();
			  data = new JSONObject();
			  int idx = 0;
			  for(Object o : row) {
				  try {
					  data.put(headers.get(idx), o);
				  } catch (Exception e1) {
					  log.error(e1.getMessage());
				  }
				  idx++;
			  }
			  retVal.add(data);			  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray(retVal);
	}
}
