package com.kuvata.kmf;

import java.util.Comparator;

public class TreeviewItem implements Comparator{

	private Long grpId;
	private String grpName;
	private Long nodeId;
	private String nodeName;
	
	public TreeviewItem(){
		super();
	}
	
	public TreeviewItem(Long grpId, String grpName){
		this.grpId = grpId;
		this.grpName = grpName;
		this.nodeId = null;
		this.nodeName = null;
	}
	public TreeviewItem(Long grpId, String grpName, Long nodeId, String nodeName){
		this.grpId = grpId;
		this.grpName = grpName;
		this.nodeId = nodeId;
		this.nodeName = nodeName;
	}

	public int compare(final Object obj1, final Object obj2)
	{
		final TreeviewItem treeviewItem1;
		final TreeviewItem treeviewItem2;
		treeviewItem1 = (TreeviewItem)obj1;
		treeviewItem2 = (TreeviewItem)obj2;
		return treeviewItem1.compareTo(treeviewItem2);	
	}
	
	public int compareTo(final TreeviewItem treeviewItem)
	{
		final Long grpId = treeviewItem.getGrpId();
		final String nodeName = treeviewItem.getNodeName();		
	
		// Sort by groupId and then nodeName
		int result = this.getGrpId().compareTo( grpId );
		if(0 == result) {
			if( this.getNodeName() != null && nodeName != null ){
				result = this.getNodeName().compareToIgnoreCase( nodeName );
			}else{
				result = -1;
			}
		}
		return result;
	}

	public int compareTo(final Object obj){
		final TreeviewItem treeviewItem;
		treeviewItem = (TreeviewItem)obj;
		return compareTo(treeviewItem);
	}
	

	
	/**
	 * @return the grpId
	 */
	public Long getGrpId() {
		return grpId;
	}

	/**
	 * @param grpId the grpId to set
	 */
	public void setGrpId(Long grpId) {
		this.grpId = grpId;
	}

	/**
	 * @return the grpName
	 */
	public String getGrpName() {
		return grpName;
	}

	/**
	 * @param grpName the grpName to set
	 */
	public void setGrpName(String grpName) {
		this.grpName = grpName;
	}

	/**
	 * @return the nodeId
	 */
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId the nodeId to set
	 */
	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	
}
