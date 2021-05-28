package controller;

import java.util.HashMap;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import model.UserParameter;

public class ParameterController {
	
	private List<String> parameterList;
	private HashMap<String, String> libraryParameter;
	private List<UserParameter> userParameterList;
	
	
	public ParameterController(String[] args) throws Exception {
		libraryParameter = new HashMap<String, String>();
		userParameterList = new ArrayList<UserParameter>();
		
		parameterList = Arrays.asList(new String[] { "-parameter", "-database", "-schemas", "-template" });
		
		generateParameter(args);
		passUserParameter(libraryParameter.get("-parameter"));
	}

	
	private void generateParameter(String[] args) throws Exception {
		
		if (!validateRequireSchema(args)) {
			throw new Exception("Required parameter is missing");
		}
		
		if (args.length == 0) {
			throw new Exception("Parameter cannot be blank");
		}
				
		//HashMap<String, String> parameterMap = new HashMap<String, String>();
		
		for(int i=0; i<args.length; i+=2) {
			if (args[i].startsWith("-")) {
				libraryParameter.put(args[i], args[i+1]);
			} else {
				throw new Exception("Unrecognized paramater type");
			}
		}
		
	}
	
	private boolean validateRequireSchema(String[] args) {
		
		List<String> argsList = Arrays.asList(args);
		
		if (argsList.containsAll(parameterList)) {
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void passUserParameter(String userParameterFilePath) throws Exception {
		JSONParser jsonParser = new JSONParser();
    	FileReader reader = new FileReader(userParameterFilePath);
    	Object obj = jsonParser.parse(reader);
    	JSONArray employeeList = (JSONArray) obj;
    	//Iterate over employee array
    	employeeList.forEach( new Consumer() {
			public void accept(Object config) {
				try {
					parseConfig( (JSONObject) config );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} );
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void parseConfig(JSONObject config) throws Exception {
    	UserParameter userParameter = new UserParameter();
    	
    	List<String> keyList = new ArrayList(config.keySet());
    	for (String key : keyList) {
    		if (key.equalsIgnoreCase("output")) {
    			userParameter.setOutputFileName(config.get(key).toString());
    		} else {
    			userParameter.addDatabaseParameter(key, config.get(key).toString());
    		}
    	}
    	
    	getUserParameterList().add(userParameter);
    }
    
	
	public HashMap<String, String> getLibraryParameter() {
		return libraryParameter;
	}


	/**
	 * @return the userParameterList
	 */
	public List<UserParameter> getUserParameterList() {
		return userParameterList;
	}


	/**
	 * @param userParameterList the userParameterList to set
	 */
	public void setUserParameterList(List<UserParameter> userParameterList) {
		this.userParameterList = userParameterList;
	}
	
	/*
	public HashMap<String, String> getDatabaseParameter() {
		return databaseParameter;
	}
	*/

}
