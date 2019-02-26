package com.kuvata.kmf;

import com.kuvata.kmf.logging.HistorizableCollectionMember;
import com.kuvata.kmf.util.Reformat;


/**
 * 
 * 
 * @author Jeff Randesi
 * Created on Jul 8, 2004
 * Copyright 2004, Kuvata, Inc.
 */
public class ContentRotationDisplayarea implements HistorizableCollectionMember
{
	private ContentRotation contentRotation;
	private Displayarea displayarea;
	
	/**
	 * 
	 *
	 */
	public ContentRotationDisplayarea()
	{		
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		// The content_rotation_displayarea table does not have a primary key
		// Therefore we must create a composite key in order to ensure a unique id (for logging purposes)
		int result = 0;
		result = Reformat.getSafeHash( this.getContentRotation().getContentRotationId().hashCode(), result, 13 );
		result = Reformat.getSafeHash( this.getDisplayarea().getDisplayareaId().hashCode(), result, 29 );		
		return  Long.valueOf( result );
	}	
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getContentRotation().getContentRotationId();
	}	
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		return this.getDisplayarea().getDisplayareaName();
	}		

	/**
	 * @return Returns the contentRotation.
	 */
	public ContentRotation getContentRotation() {
		return contentRotation;
	}
	

	/**
	 * @param contentRotation The contentRotation to set.
	 */
	public void setContentRotation(ContentRotation contentRotation) {
		this.contentRotation = contentRotation;
	}
	

	/**
	 * @return Returns the displayarea.
	 */
	public Displayarea getDisplayarea() {
		return displayarea;
	}
	

	/**
	 * @param displayarea The displayarea to set.
	 */
	public void setDisplayarea(Displayarea displayarea) {
		this.displayarea = displayarea;
	}
	
		


}
