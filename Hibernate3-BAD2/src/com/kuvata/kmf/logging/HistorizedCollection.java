package com.kuvata.kmf.logging;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import org.hibernate.collection.PersistentCollection;

public class HistorizedCollection implements Historizable
{
	private Long parentEntityId;
	private String oldValue;
	private String propertyName;
	private HashMap<Long, HistorizableCollectionMember> addedCollectionMembers = new HashMap<Long, HistorizableCollectionMember>();
	private HashMap<Long, HistorizableCollectionMember> removedCollectionMembers = new HashMap<Long, HistorizableCollectionMember>();
	
	public HistorizedCollection(PersistentCollection collection, String parentEntityId, HistorizedCollection previouslySavedHistorizedCollection )
	{
		String oldValue = "";
		String newValue = "";
		String propertyName = "";
				
		// TODO: Is this required??
		//if (collection.isDirectlyAccessible() == false) {
		//	continue;
		//}
		if( collection != null )
		{
			// retrieve Snapshot
			Serializable snapshotOfCollection = collection.getStoredSnapshot();
			if( snapshotOfCollection != null )
			{
				Collection<HistorizableCollectionMember> oldCollection = null;
				
				// For some unknown reason, hibernate's PersistentSet returns a HashMap when retrieving the snapshot
				if( snapshotOfCollection instanceof HashMap ){
					HashMap<HistorizableCollectionMember,HistorizableCollectionMember> map = (HashMap<HistorizableCollectionMember,HistorizableCollectionMember>)snapshotOfCollection;
					oldCollection = map.values();
				}else{
					oldCollection = (Collection<HistorizableCollectionMember>)snapshotOfCollection;
				}
				Collection<HistorizableCollectionMember> newCollection = (Collection<HistorizableCollectionMember>)collection;
									
				// Locate all items that were removed
				for( HistorizableCollectionMember historizable : oldCollection ){
					if( newCollection.contains( historizable ) == false ){
						removedCollectionMembers.put( historizable.getEntityId(), historizable );
						
						// Set the parentEntityId to the entityId that was specified in getHistoryEntityId
						// This is required in cases like PairedAssets:
						//  - The parentEntityId is the pairedDisplayareaId, however, we want to associate
						//    the paired assets with either the associated playlist or asset. Therefore, we 
						//    must refer to the historyEntityId.
						parentEntityId = historizable.getHistoryEntityId().toString();
					}
				}
				
				// Locate all items that were added
				for( HistorizableCollectionMember historizable : newCollection ){
					if( oldCollection.contains( historizable ) == false ){
						addedCollectionMembers.put( historizable.getEntityId(), historizable );
						
						// Set the parentEntityId to the entityId that was specified in getHistoryEntityId
						// This is required in cases like PairedAssets:
						//  - The parentEntityId is the pairedDisplayareaId, however, we want to associate
						//    the paired assets with either the associated playlist or asset. Therefore, we 
						//    must refer to the historyEntityId.
						parentEntityId = historizable.getHistoryEntityId().toString();
					}											
				}	
			}	
			propertyName = collection.getRole();
		}
		
		// If we have previously saved off history for this collection (within the same hibernate transaction)
		if( previouslySavedHistorizedCollection != null )
		{
			// Append the previously saved values to the current values
			addedCollectionMembers.putAll( previouslySavedHistorizedCollection.getAddedCollectionMembers() );
			removedCollectionMembers.putAll( previouslySavedHistorizedCollection.getRemovedCollectionMembers() );
		}		
		this.parentEntityId = Long.valueOf( parentEntityId );		
		this.propertyName = propertyName;	
	}
	
	public Long getEntityId(){
		return this.parentEntityId;
	}
	
	// Not being used
	public String getEntityName(){
		return null;
	}
	
	/**
	 * @return the oldValue
	 */
	public String getOldValue() 
	{
		String result = "";
		for( HistorizableCollectionMember collectionMember : removedCollectionMembers.values() ){
			if( result.length() > 0 ){
				result += ", ";
			}
			result += collectionMember.getEntityName();
		}
		return result;
	}

	/**
	 * @param oldValue the oldValue to set
	 */
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	/**
	 * Builds a comma delimited string of entity names that were members of the this historized collection
	 * @return
	 */
	public String getNewValue() 
	{
		String result = "";
		for( HistorizableCollectionMember collectionMember : addedCollectionMembers.values() ){
			if( result.length() > 0 ){
				result += ", ";
			}
			result += collectionMember.getEntityName();
		}
		return result;
	}

	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the addedCollectionMembers
	 */
	public HashMap<Long, HistorizableCollectionMember> getAddedCollectionMembers() {
		return addedCollectionMembers;
	}

	/**
	 * @param addedCollectionMembers the addedCollectionMembers to set
	 */
	public void setAddedCollectionMembers(
			HashMap<Long, HistorizableCollectionMember> addedCollectionMembers) {
		this.addedCollectionMembers = addedCollectionMembers;
	}

	/**
	 * @return the removedCollectionMembers
	 */
	public HashMap<Long, HistorizableCollectionMember> getRemovedCollectionMembers() {
		return removedCollectionMembers;
	}

	/**
	 * @param removedCollectionMembers the removedCollectionMembers to set
	 */
	public void setRemovedCollectionMembers(
			HashMap<Long, HistorizableCollectionMember> removedCollectionMembers) {
		this.removedCollectionMembers = removedCollectionMembers;
	}
	
}
