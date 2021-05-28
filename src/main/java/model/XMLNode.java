package model;

public class XMLNode {
	private String name, type, minOccur, maxOccur, parentName;
	private boolean isRoot, isMandatory, isOptional, isRepeatable, isArray, isComplexType;	
	
	public XMLNode(String name, String type, String minOccur, String maxOccur, String parentName) {
		setName(name);
		setType(type);
		setMinOccur(minOccur);
		setMaxOccur(maxOccur);
		setParentName(parentName);

		validateProperties();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the minOccur
	 */
	public String getMinOccur() {
		return minOccur;
	}

	/**
	 * @param minOccur the minOccur to set
	 */
	public void setMinOccur(String minOccur) {
		this.minOccur = minOccur;
		validateProperties();
	}

	/**
	 * @return the maxOccur
	 */
	public String getMaxOccur() {
		return maxOccur;
	}

	/**
	 * @param maxOccur the maxOccur to set
	 */
	public void setMaxOccur(String maxOccur) {
		this.maxOccur = maxOccur;
		validateProperties();
	}

	/**
	 * @return the parentName
	 */
	public String getParentName() {
		return parentName;
	}

	/**
	 * @param parentName the parentName to set
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	/**
	 * @return the isRoot
	 */
	public boolean isRoot() {
		return isRoot;
	}

	/**
	 * @param isRoot the isRoot to set
	 */
	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	/**
	 * @return the isMandatory
	 */
	public boolean isMandatory() {
		return isMandatory;
	}

	/**
	 * @param isMandatory the isMandatory to set
	 */
	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	/**
	 * @return the isOptional
	 */
	public boolean isOptional() {
		return isOptional;
	}

	/**
	 * @param isOptional the isOptional to set
	 */
	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}

	/**
	 * @return the isRepeatable
	 */
	public boolean isRepeatable() {
		return isRepeatable;
	}

	/**
	 * @param isRepeatable the isRepeatable to set
	 */
	public void setRepeatable(boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}

	/**
	 * @return the isArray
	 */
	public boolean isArray() {
		return isArray;
	}

	/**
	 * @param isArray the isArray to set
	 */
	public void setArray(boolean isArray) {
		this.isArray = isArray;
	}

	/**
	 * @return the isComplexType
	 */
	public boolean isComplexType() {
		return isComplexType;
	}

	/**
	 * @param isComplexType the isComplexType to set
	 */
	public void setComplexType(boolean isComplexType) {
		this.isComplexType = isComplexType;
	}
	
	private void validateProperties() {
		// Validate mandatory and optional
		if (getMinOccur() != null) {
			if (getMinOccur().equalsIgnoreCase("") || !getMinOccur().equalsIgnoreCase("0")) {
			setMandatory(true);
			setOptional(false);
			} else {
				setMandatory(false);
				setOptional(true);
			}
		} else {
			setMandatory(false);
			setOptional(true);
		}
		
		// Validate repeatable
		if (getMaxOccur() != null) {
			if (getMaxOccur().equalsIgnoreCase("") || !getMaxOccur().equalsIgnoreCase("0")) {
				setRepeatable(true);
			} else {
				setRepeatable(false);
			}
		} else {
			setRepeatable(false);
		}
		
	}
}
