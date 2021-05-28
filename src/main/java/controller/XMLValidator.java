package controller;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class XMLValidator {
	
	private List<String> schemaFileList;
	private String xmlFilePath, schemaFolderPath; 
	
	public XMLValidator(String xmlFilePath, String schemaFolderPath) {
		setXmlFilePath(xmlFilePath);
		setSchemaFolderPath(schemaFolderPath);
		
		schemaFileList = new ArrayList<String>();
	}

	public List<SAXParseException> validate() throws Exception {
		
    	listSchemaFile(new File(this.schemaFolderPath));
    	
    	List<SAXParseException> exceptions = new LinkedList<SAXParseException>();
    	StreamSource[] schemaSourceList = Arrays.stream(schemaFileList.toArray(new String[0])).map(StreamSource::new).collect(Collectors.toList()).toArray(new StreamSource[schemaFileList.size()]); 
    	
    	Source xmlFile = new StreamSource(new File(this.xmlFilePath));
    	SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    	Schema schema = schemaFactory.newSchema(schemaSourceList);
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new ErrorHandler()
		  {
		    @Override
		    public void warning(SAXParseException exception) throws SAXException
		    {
		      exceptions.add(exception);
		    }

		    @Override
		    public void fatalError(SAXParseException exception) throws SAXException
		    {
		      exceptions.add(exception);
		    }

		    @Override
		    public void error(SAXParseException exception) throws SAXException
		    {
		      exceptions.add(exception);
		    }
		  });
		validator.validate(xmlFile);
		//System.out.println("\t" + xmlFile.getSystemId() + " is valid");
		return exceptions;
    }
	
	
	public void validateWithFHIRCli(String filePath) throws Exception {
		File file = new File(filePath);
		try {
			HL7FHIRController.convertAndValidate(file.getAbsolutePath());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/* Future support with custom profile
	public void validateWithFHIRCli(String filePath, String fhirProfile) throws Exception {
		File file = new File(filePath);
		try {
			HL7FHIRController.convertAndValidate(file.getAbsolutePath(), fhirProfile);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	*/
    
    public void listSchemaFile(final File folder) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            	listSchemaFile(fileEntry);
            } else {
            	if (fileEntry.getName().endsWith(".xsd"))
                schemaFileList.add(fileEntry.getAbsolutePath());
            }
        }
    }

	public String getXmlFilePath() {
		return xmlFilePath;
	}

	public void setXmlFilePath(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
	}

	public String getSchemaFolderPath() {
		return schemaFolderPath;
	}

	public void setSchemaFolderPath(String schemaFolderPath) {
		this.schemaFolderPath = schemaFolderPath;
	}
    
     
}