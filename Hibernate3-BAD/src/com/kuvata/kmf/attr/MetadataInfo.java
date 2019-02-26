package com.kuvata.kmf.attr;

public class MetadataInfo
{
	private String attrDefinitionId;
	private String name;
	private String values;
	private String selectedValue;		
	private String type;
	private String userInterface;
	
	public MetadataInfo(){}
	
	public MetadataInfo(String attrDefintionId, String name, String attrValues, String selectedValue, String type, String userInterface){
		this.attrDefinitionId = attrDefintionId;
		this.name = name;
		this.values = attrValues;
		this.selectedValue = selectedValue;
		this.type = type;
		this.userInterface = userInterface;
	}

	
	/**
	 * @return Returns the attrDefinitionId.
	 */
	public String getAttrDefinitionId() {
		return attrDefinitionId;
	}

	
	/**
	 * @param attrDefinitionId The attrDefinitionId to set.
	 */
	public void setAttrDefinitionId(String attrDefinitionId) {
		this.attrDefinitionId = attrDefinitionId;
	}

	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	
	
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	/**
	 * @return Returns the selectedValue.
	 */
	public String getSelectedValue() {
		return selectedValue;
	}
	

	/**
	 * @param selectedValue The selectedValue to set.
	 */
	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
	}
	

	/**
	 * @return Returns the userInterface.
	 */
	public String getUserInterface() {
		return userInterface;
	}
	

	/**
	 * @param userInterface The userInterface to set.
	 */
	public void setUserInterface(String userInterface) {
		this.userInterface = userInterface;
	}
	

	/**
	 * @return Returns the values.
	 */
	public String getValues() {
		return values;
	}
	

	/**
	 * @param values The values to set.
	 */
	public void setValues(String values) {
		this.values = values;
	}
}