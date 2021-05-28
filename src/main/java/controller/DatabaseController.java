package controller;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;

import model.DBMapping;

public class DatabaseController {
	
	private Connection connection;
	private boolean hasNext;
	private int lastOffset;
	private HashMap<String, String> databaseParamList;
	private List<DBMapping> dbMappingList;
	
	public DatabaseController(String databaseConnectionFile) throws Exception {
		dbMappingList = new ArrayList<DBMapping>();
		passDatabaseConfig(databaseConnectionFile);
		//setDatabaseParamList(databaseParamList);
	}
	
	@SuppressWarnings("unchecked")
	private void passDatabaseConfig(String databaseConnectionFile) throws Exception {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader(databaseConnectionFile));
		JSONObject jsonObject = (JSONObject) obj;
		connectDatabase(
				jsonObject.get("databaseType").toString(), 
				jsonObject.get("hostName").toString(), 
				jsonObject.get("port").toString(),
				jsonObject.get("databaseName").toString(), 
				jsonObject.get("userName").toString(), 
				jsonObject.get("password").toString()
				);
		
		Object databaseBindingObj = jsonObject.get("parameter_biding");
		JSONArray employeeList = (JSONArray) databaseBindingObj;
    	//Iterate over employee array
    	employeeList.forEach( new Consumer<Object>() {
			public void accept(Object dbBindingObj) {
				try {
					passDatabaseBinding( (JSONObject) dbBindingObj );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} );	
	}
	
	private void passDatabaseBinding(JSONObject dbBindingObj) {
		dbMappingList.add(
				new DBMapping(
						dbBindingObj.get("tableName").toString(),
						dbBindingObj.get("orderBy").toString(), 
						dbBindingObj.get("where").toString()
						)
				);
	}
	
	private void connectDatabase(String databaseType, String hostName, String port, String databaseName, String userName, String password) throws Exception {
		if (databaseType.equalsIgnoreCase("MSSQL")) {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection =  DriverManager.getConnection("" +
			"jdbc:sqlserver://" + hostName + ";databaseName=" + databaseName + ";user=" + userName + ";password=" + password);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Object readDatabase(String entity, int offset) throws Exception {
		//System.out.println(entity);
		
		// Create statement from connection
		Statement s = null;
		s = connection.createStatement();
		
		//split table and field name
		entity = entity.replace("@", "");
		
		String tableName = entity.split("\\.")[0];
		String fieldName = entity.split("\\.")[1];
		
		DBMapping databaseBinding = dbMappingList.stream()
				  .filter(dbBinding -> tableName.equals(dbBinding.getTableName()))
				  .findAny()
				  .orElse(null);
		
		String where = databaseBinding.getWhere();
		String orderBy = databaseBinding.getOrderBy();
		
		
		String sql = "SELECT " + fieldName + " FROM " + tableName;
		
				if (where != null && !where.trim().equals("")) {
					sql = sql + " WHERE " + where;
				}
				
				if (orderBy != null && !orderBy.trim().equals("")) {
					sql = sql + " ORDER BY " + orderBy;
				}
				
				sql = sql + " OFFSET " + offset + " ROWS FETCH NEXT 1 ROWS ONLY";
				
		
		Iterator iterator = databaseParamList.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry) iterator.next();
			if (sql.contains((String)mapElement.getKey())) {
				sql = sql.replace((String)mapElement.getKey(), (String)mapElement.getValue());
			}
		}
		
		ResultSet rec = s.executeQuery(sql);
		
		Object resultObj;
		if ((rec!=null) && (rec.next())) {
			//Object resultObject = rec.getObject(fieldName);
			resultObj = rec.getObject(fieldName);
		} else {
			resultObj = null;
		}
		
		s.close();
		
		return resultObj;
	}
	
	@SuppressWarnings("rawtypes")
	public int checkAll(String entity) throws Exception {
		
		
		// Create statement from connection
		Statement s = null;
		s = connection.createStatement();
		
		//split table and field name
		entity = entity.replace("@", "");
		String tableName = entity.split("\\.")[0];
		//String fieldName = entity.split("\\.")[1];
		DBMapping databaseBinding = dbMappingList.stream()
				  .filter(dbBinding -> tableName.equals(dbBinding.getTableName()))
				  .findAny()
				  .orElse(null);
		
		String where = databaseBinding.getWhere();
		//String orderBy = databaseBinding.getOrderBy();
		
		String sql = "SELECT COUNT(1) AS RESULT FROM " + tableName;
		
				if (where != null && !where.trim().equals("")) {
					sql = sql + " WHERE " + where;
				}
				
				Iterator iterator = databaseParamList.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry mapElement = (Map.Entry) iterator.next();
					if (sql.contains((String)mapElement.getKey())) {
						sql = sql.replace((String)mapElement.getKey(), (String)mapElement.getValue());
					}
				}	
			
		
		ResultSet rec = s.executeQuery(sql);
		
		if ((rec!=null) && (rec.next())) {
			int resultObject = rec.getInt("RESULT");
			s.close();
			return resultObject;
		} else {
			s.close();
			return 0;
		}
		
		//String resultString = (String) rec.getObject(fieldName);
		
		//System.out.println(resultString);
		
		
	}
	
	public void closeConnection() throws Exception {
		connection.close();
	}

	/**
	 * @return the databaseparamList
	 */
	public HashMap<String, String> getDatabaseParamList() {
		return databaseParamList;
	}

	/**
	 * @param databaseparamList the databaseparamList to set
	 */
	public void setDatabaseParamList(HashMap<String, String> databaseparamList) {
		this.databaseParamList = databaseparamList;
	}

	

}
