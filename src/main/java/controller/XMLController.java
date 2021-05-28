package controller;

import java.io.File;
import java.sql.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;

import model.UserParameter;
import model.XMLNode;

public class XMLController {
	
	private Document templateDocument, outputDocument;
	private DatabaseController databaseController;
	private List<XMLNode> nodeConfigList;
	private UserParameter userParameter;
	private DateTimeFormatter dateTimeFormatter;
	private int loop = 0;
	
	private static String templateFile;
	
	
	public XMLController(UserParameter userParameter, String templateFile, DatabaseController databaseController, List<XMLNode> nodeConfigList) {
		setUserParameter(userParameter);
		setTemplateFile(templateFile);
		setDatabaseController(databaseController);
		setNodeConfigList(nodeConfigList);
		
		dateTimeFormatter = ISODateTimeFormat.dateTimeParser();  
	}
	
	public void GenerateXMLFile() throws Exception {
		
			
		databaseController.setDatabaseParamList(userParameter.getDatabaseParameterList());
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder loader = factory.newDocumentBuilder();
		Document document = loader.parse(getTemplateFile());
		outputDocument = loader.newDocument();
		DocumentTraversal traversal = (DocumentTraversal) document;
		TreeWalker walker = traversal.createTreeWalker(document.getDocumentElement(), (NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT), null, true);
		traverseLevel(walker, null, null, userParameter);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
		DOMSource domSource = new DOMSource(outputDocument);
		StreamResult streamResult = new StreamResult(new File(userParameter.getOutputFileName()));

		transformer.transform(domSource, streamResult);
	}
	
	@SuppressWarnings("unused")
	private void traverseLevel(TreeWalker walker, Node parentNode, Element outputElement, UserParameter userParameter) throws Exception {
		
		Node node = walker.getCurrentNode();

		boolean isRoot = false;
		boolean isMandatory = false;
		boolean isOptional = false;
		boolean isRepeatable = false;
		boolean isComplexType = false;
		boolean isArray = false;	
		
		Element insertingElement = null; 
		
		// Focus on Element node only. (XML may contains many node type e.g. ElementNode, #Text)
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			
			// Read XML Node configuration
			XMLNode nodeConfig = nodeConfigList.stream()
					  .filter(xmlNode -> node.getNodeName().equals(xmlNode.getName()))
					  .findAny()
					  .orElse(null);
			
			if (nodeConfig != null) {
				isMandatory = nodeConfig.isMandatory();
				isOptional = nodeConfig.isOptional();
				isRepeatable = nodeConfig.isRepeatable();
				isComplexType = nodeConfig.isComplexType();
				isArray = nodeConfig.isArray();
			}
			
			//Split operation for root and non-root node
			if (parentNode != null) {
				// Non-root node
				insertingElement = outputDocument.createElement(node.getNodeName());
							
				try {
					String testNode = node.getFirstChild().getNextSibling().getNodeName();
					//System.out.println(x.getNodeName());
					isComplexType = true;
				} catch (NullPointerException ex) {
					
				}
				//Node x = node.getFirstChild().getNextSibling();
				if (!isComplexType) {
					// If template contain parameter, read database and look up for value
					if (node.getTextContent().trim().startsWith("@")) {
						
						// Check data with node
						Object object = getDatabaseController().readDatabase(node.getTextContent(), loop);
						if (object != null) {
							if (!object.toString().trim().equalsIgnoreCase("")) {
								if (object instanceof Date) { 
									LocalDateTime dateTime = LocalDateTime.parse(object.toString(), dateTimeFormatter);
									insertingElement.appendChild(outputDocument.createTextNode(dateTime.toString()));
								} else {
									insertingElement.appendChild(outputDocument.createTextNode(object.toString()));
								}
							} else {
								if (isMandatory == true) {
									insertingElement.appendChild(outputDocument.createTextNode(""));
								} else {
									insertingElement = null;
								}
							}
						} else {
							insertingElement = null;
						}
						
					} else if (!node.getTextContent().trim().equals("")) {
						// if template contains any non-parameter data, fill XML with that data
						insertingElement.appendChild(outputDocument.createTextNode(node.getTextContent()));
					}
				}
				if (insertingElement != null) {
					// Check attribute
					NamedNodeMap attributeList = node.getAttributes();
					if (attributeList != null)
					{
						for (int i = 0; i < attributeList.getLength(); i++)
						{
							Node attributeNode = attributeList.item(i);
							if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE)
							{
								if (attributeNode.getNodeValue().trim().startsWith("@")) {
									Object object = getDatabaseController().readDatabase(attributeNode.getNodeValue().trim(), 0);
									if (object != null) {
										if (!object.toString().trim().equalsIgnoreCase("")) {
											if (object instanceof Date) { 
												LocalDateTime dateTime = LocalDateTime.parse(object.toString(), dateTimeFormatter);
												//insertingElement.appendChild(outputDocument.createTextNode(dateTime.toString()));
												insertingElement.setAttribute(attributeNode.getNodeName(), dateTime.toString());
											} else {
												//insertingElement.appendChild(outputDocument.createTextNode(object.toString()));
												insertingElement.setAttribute(attributeNode.getNodeName(), object.toString());
											}	
										} else {
											insertingElement.setAttribute(attributeNode.getNodeName(), "");
										}
									} else {
										insertingElement.setAttribute(attributeNode.getNodeName(), "");
									}
								} else {
									insertingElement.setAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
								}
								//insertingElement.setAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
							}
						}
					}
					
					outputElement.appendChild(insertingElement);
				}
				
			} else {
				// Root node
				insertingElement = outputDocument.createElement(node.getNodeName());

				// Get and insert initial NameSpace attribute
				NamedNodeMap namespaceAttribute = node.getAttributes();
				if (namespaceAttribute != null)
				{
					for (int i = 0; i < namespaceAttribute.getLength(); i++)
					{
						Node attributeNode = namespaceAttribute.item(i);
						if (attributeNode.getNodeType() == Node.ATTRIBUTE_NODE)
						{
							String name = attributeNode.getNodeName();
							insertingElement.setAttribute(name, attributeNode.getNodeValue());
						}
					}
				}
				outputDocument.appendChild(insertingElement);
			}
			
			// Append insertingElement to output XML document
			/*
			if (insertingElement != null) {
				outputDocument.appendChild(insertingElement);
			}*/
		} else if (node.getNodeType() == Node.COMMENT_NODE) {
			System.out.println("Found comment");
			insertingElement = outputDocument.createElement(node.getNodeValue());
			outputDocument.appendChild(insertingElement);
		}

		// Depth-first search traversal
		for (Node n = walker.firstChild(); n != null; n = walker.nextSibling()) {
			traverseLevel(walker, node, insertingElement, userParameter);
		}
		
		// Check loop condition
		int total;
		try {
			Element firstChildElement = (Element) node.getFirstChild().getNextSibling();
			//total = getDatabaseController().checkAll(node.getTextContent());
			total = getDatabaseController().checkAll(firstChildElement.getTextContent());
		} catch (Exception e) {
			total = 0;
		}
		
		if (isArray == true && loop < total-1) {

			loop += 1;
			walker.setCurrentNode(node.getPreviousSibling());
		} else {
			walker.setCurrentNode(node);
		}
	}
	
	

	public Document getTemplateDocument() {
		return templateDocument;
	}

	public void setTemplateDocument(Document templateDocument) {
		this.templateDocument = templateDocument;
	}

	/**
	 * @return the templateFile
	 */
	public static String getTemplateFile() {
		return templateFile;
	}

	/**
	 * @param templateFile the templateFile to set
	 */
	public static void setTemplateFile(String templateFile) {
		XMLController.templateFile = templateFile;
	}

	/**
	 * @return the databaseController
	 */
	public DatabaseController getDatabaseController() {
		return databaseController;
	}

	/**
	 * @param databaseController the databaseController to set
	 */
	public void setDatabaseController(DatabaseController databaseController) {
		this.databaseController = databaseController;
	}

	/**
	 * @return the nodeConfigList
	 */
	public List<XMLNode> getNodeConfigList() {
		return nodeConfigList;
	}

	/**
	 * @param nodeConfigList the nodeConfigList to set
	 */
	public void setNodeConfigList(List<XMLNode> nodeConfigList) {
		this.nodeConfigList = nodeConfigList;
	}

	/**
	 * @return the userParameterList
	 */
	public UserParameter getUserParameter() {
		return userParameter;
	}

	/**
	 * @param userParameterList the userParameterList to set
	 */
	public void setUserParameter(UserParameter userParameter) {
		this.userParameter = userParameter;
	}

}
