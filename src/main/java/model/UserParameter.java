package model;

import java.util.HashMap;

public class UserParameter {
	private String outputFileName;	
	private HashMap<String, String> databaseParameter;
	
	public UserParameter() {
		databaseParameter = new HashMap<String, String>();
	}
	/**
	 * @return the outputFileName
	 */
	public String getOutputFileName() {
		return outputFileName;
	}
	/**
	 * @param outputFileName the outputFileName to set
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void addDatabaseParameter(String key, String value) {
		databaseParameter.put(key, value);
	}
	
	/**
	 * @param key
	 */
	public void removeDatabaseParameter(String key) {
		databaseParameter.remove(key);
	}
	
	/**
	 * 
	 */
	public void clearDatabaseParameter() {
		databaseParameter.clear();
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String getDatabaseParameter(String key) {
		return databaseParameter.get(key);
	}
	
	/**
	 * @return the databaseParameter
	 */
	public HashMap<String, String> getDatabaseParameterList() {
		return databaseParameter;
	}
}
