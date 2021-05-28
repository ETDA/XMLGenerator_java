package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import model.XMLNode;

public class SchemaController {
	
	private List<XMLNode> xmlNodeList;
	private Map<String, String> nameSpaceList;
	private List<String> schemaFileList;
	
	public SchemaController(String messagepackage, String templateFile) throws Exception {
		xmlNodeList = new ArrayList<XMLNode>();
		schemaFileList = new ArrayList<String>();
		nameSpaceList = new HashMap<String, String>();
		
		getNameSpaceList(templateFile);
		getSchema(messagepackage);
		
	}
    
    private void getNameSpaceList(String templateFile) throws Exception {
    	
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse (new File(templateFile));
        
        Element documentElement = document.getDocumentElement();
        NamedNodeMap attributeList = documentElement.getAttributes();
        for (int i=0; i<attributeList.getLength(); i++) {
        	if (attributeList.item(i).getNodeName().startsWith("xmlns")) {
        		System.out.println("\t" + attributeList.item(i).getNodeName().substring(attributeList.item(i).getNodeName().indexOf(":")+1) + ": " + attributeList.item(i).getNodeValue());
        		nameSpaceList.put(
        				attributeList.item(i).getNodeValue(), 
        				attributeList.item(i).getNodeName().substring(attributeList.item(i).getNodeName().indexOf(":")+1)
        				);
        	}
        }
    }
    
    private void getSchema(String messagePackage) throws Exception {
    	listSchemaFile(new File(messagePackage));
    	
    	//Assign prefix
    	for (String schemaFile : schemaFileList) {
    		//System.out.println(schemaFile);
    		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse (new File(schemaFile));
            
            Element documentElement = document.getDocumentElement();
            String targetNameSpace = documentElement.getAttribute("targetNamespace");
            String prefix = nameSpaceList.get(targetNameSpace);
            if (prefix != null ) {
            	if (!prefix.equalsIgnoreCase("")) {
            		getComplexNode(document, prefix);
                }
            }
            
    	}
    }
    
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
    
	private void getComplexNode(Document document, String prefix) throws Exception {         
         //Find all complex type to loop
		NodeList complexNodeList = null;
		if (document.getElementsByTagName("xsd:complexType").getLength() > 0) {
			complexNodeList = document.getElementsByTagName("xsd:complexType"); 
		} else if (document.getElementsByTagName("xs:complexType").getLength() > 0) {
			complexNodeList = document.getElementsByTagName("xs:complexType");
		} else {
			//throw new Exception("Library support XSD with prefix 'xs' or 'xsd' only");
			return;
		}
		
         //System.out.println("Count: " + complexNodeList.getLength());
         for(int i = 0 ; i < complexNodeList.getLength(); i++) {
        	 //System.out.println("Complex: " + ((Element)complexNodeList.item(i)).getAttribute("name"));
        	 getSequenceNode(complexNodeList.item(i), prefix);
         }
         
         //Assign isComplex
         
         for(int i = 0 ; i < xmlNodeList.size(); i++) {
        	 String xmlNodeName = xmlNodeList.get(i).getType();
        	 for(int j = 0 ; j < xmlNodeList.size(); j++) {
        		 if (xmlNodeList.get(j).getParentName().equals(xmlNodeName)) {
        			 //System.out.println(xmlNodeList.get(i).getName() + " is a complex type");
        			 xmlNodeList.get(i).setComplexType(true);
        			 if (!xmlNodeList.get(i).getMaxOccur().equalsIgnoreCase("") && !xmlNodeList.get(i).getMaxOccur().equalsIgnoreCase("1")) {
        				 xmlNodeList.get(i).setArray(true);
        			 }
        			 break;
        		 }
        	 }
         }
    }
    
    private void getSequenceNode(Node node, String prefix) throws Exception {
    	NodeList sequenceNodeList = node.getChildNodes();
    	for(int i = 0 ; i < sequenceNodeList.getLength(); i++) {
    		if (sequenceNodeList.item(i).getNodeName().equalsIgnoreCase("xsd:sequence") ||  sequenceNodeList.item(i).getNodeName().equalsIgnoreCase("xs:sequence")) {
    			getElementNode(sequenceNodeList.item(i), node, prefix);
    			break;
    		}
        }
    }
    
    public void getElementNode(Node node, Node parentComplexNode, String prefix) throws Exception {
    	//Element element = (Element)node;
    	Element parentComplexElement = (Element)parentComplexNode;
    	
    	NodeList elementNodeList = node.getChildNodes();
    	for(int i = 0 ; i < elementNodeList.getLength(); i++) {
    		if (elementNodeList.item(i).getNodeName().equalsIgnoreCase("xsd:element") || elementNodeList.item(i).getNodeName().equalsIgnoreCase("xs:element")) {
    			Element element = (Element)elementNodeList.item(i);
    			xmlNodeList.add(
    	    			new XMLNode(
    	         				prefix + ":" + element.getAttribute("name"), 
    	         				element.getAttribute("type"), 
    	         				element.getAttribute("minOccurs"), 
    	         				element.getAttribute("maxOccurs"),
    	         				prefix + ":" + parentComplexElement.getAttribute("name")
    	         				)
    	         		);
    		}
    	}
    }

	/**
	 * @return the xmlNodeList
	 */
	public List<XMLNode> getXmlNodeList() {
		return xmlNodeList;
	}
    
}