package controller;

import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXParseException;

import model.XMLNode;
import model.UserParameter;

import java.nio.file.*;

public class Main {
	
	public static void main(String[] args) {
		
		boolean waitIndicator = false;
		boolean deleteIncomplete = false;
		
		//TODO 0. Prepare log
		LogController logController = null;
		try {
			logController = new LogController();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		logController.writeInfoLog("Start Library");
		
		try {
			
			// TODO 1.Validate input parameter
			logController.writeInfoLog("Validate parameter");
			ParameterController paramController = new ParameterController(args);
			HashMap<String, String> libraryparamList = paramController.getLibraryParameter();
			List<UserParameter> userParameterList = paramController.getUserParameterList();
			if (libraryparamList.get("-wait") != null) {
				waitIndicator = libraryparamList.get("-wait").equalsIgnoreCase("true") == true ? true : false;
			}
			
			if (libraryparamList.get("-deleteIncomplete") != null) {
				deleteIncomplete = libraryparamList.get("-deleteIncomplete").equalsIgnoreCase("true") == true ? true : false;
			}
			
			// TODO 2.Get database connection
			logController.writeInfoLog("Get database connection.");
			DatabaseController databaseController = new DatabaseController(libraryparamList.get("-database"));
			
			// TODO 3.Load schema control
			logController.writeInfoLog("Load schema.");
			SchemaController schemaController = new SchemaController(libraryparamList.get("-schemas"), libraryparamList.get("-template"));
			List<XMLNode> schemaControllerList = schemaController.getXmlNodeList();
			
			for (UserParameter userParameter : userParameterList) {
				
				// TODO 4.Read template, mapping with database and generate output
				logController.writeInfoLog("Generate XML File: " + userParameter.getOutputFileName());
				//XMLController xmlController = new XMLController(libraryparamList.get("-output"), libraryparamList.get("-template"), databaseController, schemaControllerList);
				XMLController xmlController = new XMLController(userParameter, libraryparamList.get("-template"), databaseController, schemaControllerList);
				xmlController.GenerateXMLFile();
				
				// TODO 5.Validate XML
				logController.writeInfoLog("Validate XML File.");
				//XMLValidator xmlValidator = new XMLValidator(libraryparamList.get("-output"), libraryparamList.get("-schemas"));
				XMLValidator xmlValidator = new XMLValidator(userParameter.getOutputFileName(), libraryparamList.get("-schemas"));
				
				List<SAXParseException> exceptions = xmlValidator.validate();
				
				if (exceptions.size() > 0) {
					for (SAXParseException exception : exceptions) {
						logController.writeErrorLog(exception.getMessage() + " at line " + exception.getLineNumber());
					}
					
					logController.writeErrorLog(userParameter.getOutputFileName() + " is invalid!");
					
					if (deleteIncomplete) {
						if (Files.exists(Paths.get(userParameter.getOutputFileName()))) {
							logController.writeInfoLog("Clean incomplete output file.");
							Files.delete(Paths.get(userParameter.getOutputFileName()));
						}
					} else {
						logController.writeWarningLog("Incomplete output still remain.");
					}
				}
				else {
					logController.writeInfoLog(userParameter.getOutputFileName() + " is valid.");
				}
				
				if (libraryparamList.get("-enableFHIRValidation") != null) {
					if (libraryparamList.get("-enableFHIRValidation").equalsIgnoreCase("true")) {
						//xmlValidator.validateWithFHIRCli(userParameter.getOutputFileName());
						
						xmlValidator.validateWithFHIRCli(userParameter.getOutputFileName());
					}
				}
				
			}
			
			// TODO 6.Close database connection
				logController.writeInfoLog("Close database connection.");
				databaseController.closeConnection();
			
			
		} catch (Exception e) {
			// TODO E. Exception/Error occur
			logController.writeErrorLog(e.getMessage());
			System.err.println(e.getMessage());
			e.printStackTrace();
			
		} finally {
			logController.writeInfoLog("Finish Library.");
		}
		
		if (waitIndicator) {
			System.out.print("\nPress enter to continue...");
			try {
				System.in.read();
			} catch (Exception ex) {
				System.exit(0);
			} finally {
				System.exit(0);
			}
		}
	}

}
