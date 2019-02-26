package com.kuvata.kmf.attr;

import java.text.SimpleDateFormat;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import parkmedia.KmfException;
import parkmedia.usertype.AttrType;
import parkmedia.usertype.SearchInterfaceType;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.DynamicQueryPart;
import com.kuvata.kmf.EntityInstance;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.PersistentEntity;
import com.kuvata.kmf.TargetedAssetMember;
import com.kuvata.kmf.logging.HistorizableChildEntity;
import com.kuvata.kmf.util.Reformat;

public class Attr extends PersistentEntity implements IAttr, HistorizableChildEntity {

	private Long attrId;
	private String attrName;
	private AttrDefinition attrDefinition;
	private Long ownerId;
	private Long valueId;
	private AttrType attrType;
		
	/**
	 * Constructor	 
	 */
	public Attr()
	{		
	}
	/**
	 * 
	 */
	public boolean equals(Object other)
	{	
		boolean result = false;		
		if(this == other) result = true;
		if( !(other instanceof Attr) ) result = false;
		
		Attr a = (Attr) other;		
		if(this.hashCode() == a.hashCode())
			result =  true;
		
		return result;					
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "Attr".hashCode();
		result = Reformat.getSafeHash(this.getAttrName(),result,2);
		result = Reformat.getSafeHash(this.getOwnerId(),result,3);
		result = Reformat.getSafeHash(this.getAttrType(),result,5);
		result = Reformat.getSafeHash(this.getFormattedValue(),result,7);		
		return result;
	}	
	
	/**
	 * Implementation of the inherited abstract method Entity.getEntityId().
	 * Returns the attrId.
	 */
	public Long getEntityId()
	{
		return this.getAttrId();
	}
	
	public Long getHistoryEntityId()
	{
		return this.getOwnerId();
	}	
	
	public String getEntityName()
	{
		return this.getAttrDefinition().getAttrDefinitionName();
	}		
	
	/**
	 * Construtor. Creates a new attribute definition with the given name.
	 * @param name
	 * @throws Exception
	 */
	public Attr(String name, AttrType attrType, SearchInterfaceType searchTool) throws HibernateException, KmfException
	{
		AttrDefinition.addAttrDefinition( this.getClass().getName(), attrName, attrType, searchTool, Boolean.FALSE, Boolean.FALSE );
	}
	
	/**
	 * 
	 * @param attrId
	 * @return
	 * @throws HibernateException
	 */
	public static Attr getAttr(Long attrId) throws HibernateException
	{
		return (Attr)PersistentEntity.load(Attr.class, attrId);		
	}
	
	/**
	 * 
	 * @param entityId
	 * @param attrDefinitionId
	 * @return
	 * @throws HibernateException
	 */
	public static Attr getAttribute(Long entityId, Long attrDefinitionId) throws HibernateException
	{		
		Session session = HibernateSession.currentSession();

		// Try to find an existing Attr object for the given AttrDefinition and Entity
		Attr result = (Attr)session.createCriteria(Attr.class)
				.createAlias("attrDefinition", "ad")
				.add( Expression.eq("ad.attrDefinitionId", attrDefinitionId) )
				.add( Expression.eq("ownerId", entityId) )
				.uniqueResult();		
		return result;		
	}
	
	/**
	 * 
	 * @param entityId
	 * @param attrDefinitionId
	 * @return
	 * @throws HibernateException
	 */
	public static List<Attr> getAttributes(Long entityId, Long attrDefinitionId) throws HibernateException
	{		
		Session session = HibernateSession.currentSession();
		return session.createQuery("SELECT a FROM StringAttr a WHERE a.attrDefinition.attrDefinitionId = :attrDefinitionId AND a.ownerId = :ownerId ORDER BY UPPER(a.value)")
		.setParameter("attrDefinitionId", attrDefinitionId)
		.setParameter("ownerId", entityId)
		.list();
	}

	/**
	 * Retrieves a list of AttrDefinition objects with the given className
	 * 
	 * @param className
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	public static List getAdditionalAttributes(Long entityId) throws HibernateException
	{
		Session session = HibernateSession.currentSession();
						
		// Find all Attr object belonging to this entity that do not have an AttrDefinition associated with it
		List l = session.createQuery("SELECT a FROM Attr a "
									+ "WHERE a.ownerId = "+ entityId + " "
									+ "AND a.attrDefinition IS NULL "
									+ "ORDER BY UPPER(a.attrName)")
									.list();				
		return l;
	}	
	
	/**
	 * Retrieves a list of Attr objects belonging to this entity
	 * 
	 * @param className
	 * @param attributeName
	 * @return
	 * @throws Exception
	 */
	public static List getAttributes(Long entityId) throws HibernateException
	{
		// Find all Attr object belonging to this entity
		Session session = HibernateSession.currentSession();				
		List l = session.createQuery("SELECT a FROM Attr a "
									+ "WHERE a.ownerId = "+ entityId + " "									
									+ "ORDER BY UPPER(a.attrName)")
									.list();
		return l;
	}
	
	public static void updateMultiSelectAttrValue(String attrId, String newValue){
		Attr attr = Attr.getAttr(Long.valueOf(attrId));
		if(attr != null){
			String oldValue = attr.getFormattedValue();
			Query q = HibernateSession.currentSession().createQuery("UPDATE StringAttr a SET a.value = :newValue WHERE a.attrDefinition.attrDefinitionId = :attrDefinitionId AND a.value = :oldValue");
			q.setParameter("newValue", newValue).setParameter("attrDefinitionId", attr.getAttrDefinition().getAttrDefinitionId()).setParameter("oldValue", oldValue).executeUpdate();
			
			// Update dynamic query parts or targeted asset members
			if(attr.getAttrDefinition().getEntityClass().getClassName().equals(Asset.class.getName())){
				for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(attr.getAttrDefinition())){
					boolean update = false;
					String[] parts = dqp.getValue().split("~");
					String newSelectedValues = "";
					for(String s : parts){
						if(s.equals(oldValue)){
							update = true;
							newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
							newSelectedValues += newValue;
						}else{
							newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
							newSelectedValues += s;
						}
					}
					
					if(update){
						dqp.setValue(newSelectedValues);
						dqp.update();
					}
				}
			}else if(attr.getAttrDefinition().getEntityClass().getClassName().equals(Device.class.getName())){
				for(TargetedAssetMember tam : TargetedAssetMember.getTargetedAssetMembers(attr.getAttrDefinition())){
					boolean update = false;
					String[] parts = tam.getAttrValue().split("~");
					String newSelectedValues = "";
					for(String s : parts){
						if(s.equals(oldValue)){
							update = true;
							newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
							newSelectedValues += newValue;
						}else{
							newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
							newSelectedValues += s;
						}
					}
					
					if(update){
						tam.setAttrValue(newSelectedValues);
						tam.update();
					}
				}
			}
		}
	}
	
	public void delete(boolean removeFromParentCollection, boolean deleteFromDynamicEntities) throws HibernateException{
		
		if(deleteFromDynamicEntities){
			// Update dynamic query parts or targeted asset members
			if(this.getAttrDefinition().getEntityClass().getClassName().equals(Asset.class.getName())){
				for(DynamicQueryPart dqp : DynamicQueryPart.getDynamicQueryParts(this.getAttrDefinition())){
					boolean update = false;
					String newSelectedValues = "";
					
					if(dqp.getValue() != null){
						String[] parts = dqp.getValue().split("~");
						for(String s : parts){
							if(s.equals(this.getFormattedValue())){
								update = true;
							}else{
								newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
								newSelectedValues += s;
							}
						}
					}
					
					if(update){
						dqp.setValue(newSelectedValues);
						dqp.update();
					}
				}
			}else if(this.getAttrDefinition().getEntityClass().getClassName().equals(Device.class.getName())){
				for(TargetedAssetMember tam : TargetedAssetMember.getTargetedAssetMembers(this.getAttrDefinition())){
					boolean update = false;
					String newSelectedValues = "";
					
					if(tam.getAttrValue() != null){
						String[] parts = tam.getAttrValue().split("~");
						for(String s : parts){
							if(s.equals(this.getFormattedValue())){
								update = true;
							}else{
								newSelectedValues += newSelectedValues.length() > 0 ? "~" : "";
								newSelectedValues += s;
							}
						}
					}
					
					if(update){
						// If all selections were removed
						if(newSelectedValues.length() == 0){
							tam.setAttrDefinition(null);
							tam.setAttrValue(null);
						}else{
							tam.setAttrValue(newSelectedValues);
						}
						
						tam.update();
					}
				}
			}
		}
		
		if( removeFromParentCollection ){
			// Remove this object from the parent collection
			this.attrDefinition.getAttrs().remove( this );
		}
		
		super.delete();
	}
	
	public Object getValue()
	{
		Object result = null;
		if( this instanceof StringAttr ){
			StringAttr stringAttr = (StringAttr)this;
			result = stringAttr.getValue();
		}else if( this instanceof DateAttr ){
			DateAttr dateAttr = (DateAttr)this;
			result = dateAttr.getValue();			
		}else if( this instanceof NumberAttr ){
			NumberAttr numberAttr = (NumberAttr)this;			
			result = numberAttr.getValue();
		}
		return result;
	}
	
	public String getFormattedValue()
	{
		String result = "";
		if( this instanceof StringAttr ){
			StringAttr stringAttr = (StringAttr)this;
			result = stringAttr.getValue().trim();
		}else if( this instanceof DateAttr ){
			DateAttr dateAttr = (DateAttr)this;
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat( Constants.DATE_TIME_FORMAT_DISPLAYABLE );
			result = dateTimeFormat.format( dateAttr.getValue() );			
		}else if( this instanceof NumberAttr ){
			NumberAttr numberAttr = (NumberAttr)this;			
			if( numberAttr.getValue() != null ){
				result = numberAttr.getValue().toString();	
			}						
		}
		return result;
	}
	
	/**
	 * Returns the class name of the Entity that corresponds to this object's
	 * valueId in the EntityInstance table.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getAttrValueType() throws HibernateException 
	{	
		// Look up the class name associated with this value id 		
		EntityInstance ei = EntityInstance.getEntityInstance( this.getValueId() );
		return ei.getEntityClass().getClassName();			
	}

	/**
	 * @return Returns the attrName.
	 */
	public String getAttrName() {
		return attrName;
	}

	/**
	 * @param attrName The attrName to set.
	 */
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	/**
	 * @return Returns the ownerId.
	 */
	public Long getOwnerId() {
		return ownerId;
	}

	/**
	 * @param ownerId The ownerId to set.
	 */
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * @return Returns the valueId.
	 */
	public Long getValueId() {
		return valueId;
	}

	/**
	 * @param valueId The valueId to set.
	 */
	public void setValueId(Long valueId) {
		this.valueId = valueId;
	}

	/**
	 * @return Returns the attrId.
	 */
	public Long getAttrId() {
		return attrId;
	}

	/**
	 * @param attrId The attrId to set.
	 */
	public void setAttrId(Long attrId) {
		this.attrId = attrId;
	}

	/**
	 * @return Returns the attrDefinition.
	 */
	public AttrDefinition getAttrDefinition() {
		return attrDefinition;
	}

	/**
	 * @param attrDefinition The attrDefinition to set.
	 */
	public void setAttrDefinition(AttrDefinition attrDefinition) {
		this.attrDefinition = attrDefinition;
	}
	/**
	 * @return Returns the attrType.
	 */
	public AttrType getAttrType() {
		return attrType;
	}
	
	/**
	 * @param attrType The attrType to set.
	 */
	public void setAttrType(AttrType attrType) {
		this.attrType = attrType;
	}
}
