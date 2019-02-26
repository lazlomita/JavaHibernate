package com.kuvata.kmf.util;

import java.util.LinkedList;

import parkmedia.KmfException;


public class KmfXmlElement{
	private static final String XML_TAG_END_IDENTIFIER = "/>";
	private static final String XML_END_TAG_IDENTIFIER = "</";
	
	private String elementName;
	private String content;
	private StringBuffer attributes = new StringBuffer("");
	private LinkedList<KmfXmlElement> subElements = new LinkedList<KmfXmlElement>();
	
	public KmfXmlElement(String elementName){
		this.elementName = elementName;
		content = "<" + elementName + XML_TAG_END_IDENTIFIER;
	}
	
	public void addSubElement(KmfXmlElement subElement) throws KmfException{
		subElements.add(subElement);
	}
	
	public void addAttributesToElement(){
		String contentBeginsWith = content.trim().substring(0, elementName.length() + 1);
		String contentEndsWith = content.trim().substring(elementName.length() + 1);
		content = contentBeginsWith + " " + attributes.toString().trim() + contentEndsWith;
		
		// Empty the attributes
		attributes = new StringBuffer("");
	}
	
	public void addToAttributes(String name, String value){
		attributes.append(name + "='" + Reformat.getEscapeXml(value) + "' ");
	}
	
	public String toString(){
		return toString("");
	}
	
	public String toString(String indentation){
		if(content != null && content.length() > 0){
			if(subElements.size() > 0){
				// If the element doesn't have a separate end tag eg. <ElementName/>
				if(content.endsWith(XML_TAG_END_IDENTIFIER)){
					content = indentation + content.trim().substring(0, content.trim().length() - 2) + ">";
				}
				else{
					String endTag = XML_END_TAG_IDENTIFIER + elementName + ">";
					// If the element has a separate end tag eg. <ElementName>data</ElementName>
					if(content.endsWith(endTag)){
						content = indentation + content.trim().substring(0, content.trim().indexOf(endTag));
					}else{
						throw new KmfException("Cannot understand XML format in KmfXmlElement: " + content);
					}
				}
				for(KmfXmlElement subElement : subElements){
					content += "\n" + subElement.toString(indentation + "  ");
				}
				content += "\n" + indentation + XML_END_TAG_IDENTIFIER + elementName + ">";
			}else{
				content = indentation + content;
			}
		}
		
		return content;
	}
}
