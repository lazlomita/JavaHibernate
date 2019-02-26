package com.kuvata.kmf;

import java.util.Date;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.ContentRotationImportType;
import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class ContentRotationImport extends Entity implements HistorizableCollectionMember 
{
	private Long contentRotationImportId;
	private ContentRotation contentRotation;
	private ContentRotation contentRotationToImport;
	private Date importDate;
	private ContentRotationImportType importType;	
	
	/**
	 * 
	 *
	 */
	public ContentRotationImport()
	{		
	}
	
	/**
	 * Returns a ContentRotationImport with the given contentRotationImportId
	 * 
	 * @param contentRotationId
	 * @return
	 * @throws HibernateException
	 */
	public static ContentRotationImport getContentRotationImport(Long contentRotationImportId) throws HibernateException
	{
		return (ContentRotationImport)Entity.load(ContentRotationImport.class, contentRotationImportId);		
	}	
	
	/**
	 * Creates a new ContentRotationImport object.
	 * 
	 * @param contentRotation
	 * @param contentRotation
	 * @param layout
	 * @param displayarea
	 * @param importDate
	 * @param importType
	 */
	public static void create(ContentRotation contentRotation, ContentRotation contentRotationToImport, Date importDate, ContentRotationImportType importType)
	{
		// Strange, but true -- we have to put the collection of contentRotationImports into memory before adding to its collection
		// If we do not do this, the collection will not become and will not historize correctly
		int numContentRotationImports = contentRotation.getContentRotationImports().size();
		ContentRotationImport contentRotationImport = new ContentRotationImport();
		contentRotationImport.setContentRotation( contentRotation );
		contentRotationImport.setContentRotationToImport( contentRotationToImport );
		contentRotationImport.setImportDate( importDate );
		contentRotationImport.setImportType( importType );
		contentRotationImport.save();
		contentRotation.getContentRotationImports().add( contentRotationImport );
		contentRotation.update();
	}
	
	
			
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getContentRotationImportId();
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
		return this.getContentRotation().getContentRotationName();
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
	 * @return Returns the contentRotationImportId.
	 */
	public Long getContentRotationImportId() {
		return contentRotationImportId;
	}
	
	/**
	 * @param contentRotationImportId The contentRotationImportId to set.
	 */
	public void setContentRotationImportId(Long contentRotationImportId) {
		this.contentRotationImportId = contentRotationImportId;
	}

	/**
	 * @return Returns the importDate.
	 */
	public Date getImportDate() {
		return importDate;
	}

	/**
	 * @param importDate The importDate to set.
	 */
	public void setImportDate(Date importDate) {
		this.importDate = importDate;
	}

	/**
	 * @return Returns the importType.
	 */
	public ContentRotationImportType getImportType() {
		return importType;
	}

	/**
	 * @param importType The importType to set.
	 */
	public void setImportType(ContentRotationImportType importType) {
		this.importType = importType;
	}

	public ContentRotation getContentRotationToImport() {
		return contentRotationToImport;
	}

	public void setContentRotationToImport(ContentRotation contentRotationToImport) {
		this.contentRotationToImport = contentRotationToImport;
	}
}
