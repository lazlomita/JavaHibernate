/*
 * Created on July 21, 2005
 */
package com.kuvata.kmf.asset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.commons.beanutils.BeanComparator;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import parkmedia.KmfException;
import com.kuvata.kmf.usertype.AssetType;
import com.kuvata.kmf.usertype.AttrType;
import com.kuvata.kmf.usertype.DateRangeType;
import com.kuvata.kmf.usertype.DefaultAssetAffinityType;
import com.kuvata.kmf.usertype.DeviceSearchType;
import com.kuvata.kmf.usertype.MultipleTargetsBehaviorType;
import com.kuvata.kmf.usertype.SearchInterfaceType;

import com.kuvata.dispatcher.scheduling.ContentScheduler;
import com.kuvata.kmf.Asset;
import com.kuvata.kmf.AssetPresentation;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.DeviceGrpMember;
import com.kuvata.kmf.Displayarea;
import com.kuvata.kmf.Grp;
import com.kuvata.kmf.GrpMember;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.Layout;
import com.kuvata.kmf.TargetedAssetMember;
import com.kuvata.kmf.attr.AttrDefinition;
import com.kuvata.kmf.logging.HistorizableLinkedSet;
import com.kuvata.kmm.EntityResultsPage;

/**
 * @author jrandesi
 */
public class TargetedAsset extends Asset implements ITargetedAsset
{
	public static final String PRESENTATION_TYPE = "TargetedAsset";
	private static String createAssetPage = "createAssetTargetedAsset";
	private static String assetPropertiesPage = "assetPropertiesTargetedAsset";	
	private Set<TargetedAssetMember> targetedAssetMembers = new HistorizableLinkedSet<TargetedAssetMember>();	
	private DefaultAssetAffinityType defaultAssetAffinityType;
	private MultipleTargetsBehaviorType multipleTargetsBehavior;
	private Boolean useMemberPairing;
	
	
	/**
	 * 
	 *
	 */
	public TargetedAsset()
	{}	
		
	/**
	 * 
	 */
	public String getPresentationType()
	{
	    return PRESENTATION_TYPE;
	}

	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getCreateAssetPage()
	{
		return TargetedAsset.createAssetPage; 
	}
	
	/**
	 * Implements the parent's abstract method. Used to determine
	 * which page to display in the create asset wizard for this asset type.
	 */
	public String getAssetPropertiesPage()
	{
		return TargetedAsset.assetPropertiesPage; 
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public AssetType getAssetType()
	{
		return AssetType.TARGETED_ASSET;
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public void createThumbnail(int maxDimension) throws FileNotFoundException, IOException
	{				
	}
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getPreviewPath() throws HibernateException
	{
		return "";			
	}	
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String getThumbnailPath() throws HibernateException
	{
		// Do not show preview icon
		return "";					
	}		
	
	/**
	 * Implements the parent's abstract method.
	 */
	public String renderHTML()
	{
		String result = "";
		try {
			result = "<img src=\""+ getThumbnailPath() +"\">";
		} catch (HibernateException e) {
			e.printStackTrace();
		}
		return result;		
	}	
	
	/**
	 * Implements the parent's abstract method
	 * Since this asset type does not have a referenced file, return true
	 */
	public boolean getReferencedFileExists()
	{
		return true;
	}	
	
	public static TargetedAsset create(String assetName, Float length, String defaultAssetAffinityType, String multipleTargetsBehavior, Boolean variableLength, Boolean useMemberPairing, String[] selectedTargetedAssetMemberIds, Displayarea da, Layout l) throws IOException, ClassNotFoundException
	{
		AssetPresentation ap = new AssetPresentation();
		ap.setLength( length );				
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.setVariableLength( variableLength );
		ap.save();
		
		// Create a new object of the given type			
		TargetedAsset a = new TargetedAsset();			
		a.setAssetName( assetName );
		a.setDefaultAssetAffinityType( DefaultAssetAffinityType.getDefaultAssetAffinityType( defaultAssetAffinityType ) );
		a.setMultipleTargetsBehavior( MultipleTargetsBehaviorType.getMultpleTargetsBehaviorTypeByPersistentValue( multipleTargetsBehavior ) );
		a.setAssetPresentation( ap );	
		a.setUseMemberPairing(useMemberPairing);
		
		Long assetId = a.save();
		
		// Save the collection of targeted asset members
		if(selectedTargetedAssetMemberIds != null){
			a.addTargetedAssetMembers( selectedTargetedAssetMemberIds );
		}
		
		return a;
	}
	
	public void update(String assetName, Float length, String defaultAssetAffinityType, String multipleTargetsBehavior, Boolean variableLength, Boolean useMemberPairing, String[] selectedTargetedAssetMemberIds, Displayarea da, Layout l) 
		throws IOException, ClassNotFoundException
	{			
		this.setDefaultAssetAffinityType( DefaultAssetAffinityType.getDefaultAssetAffinityType( defaultAssetAffinityType ) );
		this.setMultipleTargetsBehavior( MultipleTargetsBehaviorType.getMultpleTargetsBehaviorTypeByPersistentValue( multipleTargetsBehavior ) );
		AssetPresentation ap = this.getAssetPresentation();
		
		// If the default length has changed
		boolean lengthChanged = false;
		if( length != null && ap.getLength().equals( length ) == false ) {
			ap.setLength( length );
			lengthChanged = true;
		}			
		this.makeDirty( lengthChanged, false );	
		
		ap.setDisplayarea( da );
		ap.setLayout( l );
		ap.setVariableLength( variableLength );
		ap.update();
													
		this.setAssetName( assetName );			
		this.setAssetPresentation( ap );
		this.setUseMemberPairing(useMemberPairing);
		this.update();
		
		// Save the collection of targeted asset members 
		if(selectedTargetedAssetMemberIds != null){
			addTargetedAssetMembers( selectedTargetedAssetMemberIds );
		}
	}	
	
	/**
	 * Copies this asset and assigns the given new asset name.
	 * 
	 * @param newAssetName
	 * @return
	 */
	public Long copy(String newAssetName) throws ClassNotFoundException
	{				
		// Create a new asset object
		TargetedAsset newAsset = new TargetedAsset();		
		newAsset.setAssetName( newAssetName );
		newAsset.setDefaultAssetAffinityType( this.getDefaultAssetAffinityType() );
		newAsset.setMultipleTargetsBehavior( this.getMultipleTargetsBehavior() );
		newAsset.setUseMemberPairing( this.getUseMemberPairing() );
		newAsset.setAssetPresentation( this.getAssetPresentation().copy() );
		newAsset.setStartDate( this.getStartDate() );
		newAsset.setEndDate( this.getEndDate() );

		// Save the asset but do not create permission entries since we are going to copy them		
		newAsset.save( false );
		newAsset.copyPermissionEntries( this );
		
		// Copy the targetedAssetMembers
		for( TargetedAssetMember targetedAssetMember : targetedAssetMembers )
		{
			TargetedAssetMember newTargetedAssetMember = new TargetedAssetMember();
			newTargetedAssetMember.setTargetedAsset( newAsset );
			newTargetedAssetMember.setAsset( targetedAssetMember.getAsset() );
			newTargetedAssetMember.setDevice( targetedAssetMember.getDevice() );
			newTargetedAssetMember.setDeviceGrp( targetedAssetMember.getDeviceGrp() );
			newTargetedAssetMember.setAttrDefinition( targetedAssetMember.getAttrDefinition() );
			newTargetedAssetMember.setAttrValue( targetedAssetMember.getAttrValue() );
			newTargetedAssetMember.setHql( targetedAssetMember.getHql() );
			newTargetedAssetMember.setSeqNum( targetedAssetMember.getSeqNum() );
			newTargetedAssetMember.save();
		}	

		// Copy any metadata associated with this asset
		this.copyMetadata( newAsset.getAssetId() );
		return newAsset.getAssetId();
	}
	
	/**
	 * 
	 * @param f
	 * @param s
	 * @throws HibernateException
	 */
	private void addTargetedAssetMembers(String[] selectedTargetedAssetMemberIds) throws HibernateException, ClassNotFoundException, KmfException
	{
		// Add members in bulk mode for history purposes
		HibernateSession.startBulkmode();
		
		boolean doUpdate = false;
		
		// Identify which assets were removed
		Set<TargetedAssetMember> removedAssets = new HashSet<TargetedAssetMember>();
		for(TargetedAssetMember tam : this.getTargetedAssetMembers()){
			boolean match = false;
			for(int j=0; j<selectedTargetedAssetMemberIds.length; j++){
				String assetId = selectedTargetedAssetMemberIds[j].substring(0, selectedTargetedAssetMemberIds[j].indexOf("_"));
				if(tam.getAsset().getAssetId().toString().equals(assetId)){
					match = true;
					break;
				}
			}
			
			if(match == false){
				removedAssets.add(tam);
			}
		}
		
		// Remove all targeted_asset_member rows for the assets which were removed
		for(TargetedAssetMember removedTam : removedAssets){
			this.getTargetedAssetMembers().remove( removedTam );
			removedTam.delete();				
			doUpdate = true;
		}

		// Remove all targeted_asset_member rows for the assets whose targets were modified
		for(int j=0; j<selectedTargetedAssetMemberIds.length; j++){
			// If this existing members don't match, remove existing rows
			if( this.targetedAssetMembersMatch( selectedTargetedAssetMemberIds[j], false, j ) == false){
				doUpdate = true;
			}
		}
		
		// Create rows for newly added and/or modified members
		for(int j=0; j<selectedTargetedAssetMemberIds.length; j++){
			// If this existing members don't match, create new rows
			if(this.targetedAssetMembersMatch( selectedTargetedAssetMemberIds[j], true, j ) == false){
				doUpdate = true;
			}
		}	
		
		// Make sure the sequence numbers are correct
		HashMap<String, ArrayList<TargetedAssetMember>> tams = arrangeTargetedAssetMembersByAssetId();
		for(int j=0; j<selectedTargetedAssetMemberIds.length; j++){
			String assetId = selectedTargetedAssetMemberIds[j].substring(0, selectedTargetedAssetMemberIds[j].indexOf("_"));
			ArrayList<TargetedAssetMember> l = (ArrayList<TargetedAssetMember>)tams.get(assetId);
			for (Iterator<TargetedAssetMember> it = l.iterator(); it.hasNext();) {
				TargetedAssetMember tam = it.next();
				if(tam != null && tam.getSeqNum().intValue() != j){
					tam.setSeqNum(j);
					tam.update();
				}
			}
		}

		if(doUpdate){
			this.update();
		}
		
		// Stop bulk mode
		HibernateSession.stopBulkmode();
	}	

	private HashMap<String, ArrayList<TargetedAssetMember>> arrangeTargetedAssetMembersByAssetId() {
		HashMap<String, ArrayList<TargetedAssetMember>> tams = new HashMap<String, ArrayList<TargetedAssetMember>>();
		for (TargetedAssetMember tam : this.getTargetedAssetMembers()) {
			if (tams.get(tam.getAsset().getAssetId().toString()) == null) {
				ArrayList<TargetedAssetMember> l = new ArrayList<TargetedAssetMember>();
				l.add(tam);
				tams.put(tam.getAsset().getAssetId().toString(), l);
			}
			else {
				ArrayList<TargetedAssetMember> l = tams.get(tam.getAsset().getAssetId().toString());
				l.add(tam);
			}
		}
		
		return tams;
	}
	
	private boolean targetedAssetMembersMatch(String selectedTargetedAssetMemberString, boolean createNewMembers, int seqNum)
	{
		boolean result = false;
		String assetId = selectedTargetedAssetMemberString.substring(0, selectedTargetedAssetMemberString.indexOf("_"));
		Asset asset = null;
		String deviceId = null;
		Device device = null;
		String deviceGroupId = null;
		Grp deviceGroup = null;
		String attrDefinitionId = null;
		AttrDefinition attrDefinition = null;
		String attrValue = null;
		String hql = null;
		boolean memberCreated = false;
		
		// If the assetId associated with the existing targeted asset member matches the assetId associated with the selectedTargetedAssetMemberString
		ArrayList<TargetedAssetMember> tams = new ArrayList<TargetedAssetMember>();
		for( TargetedAssetMember tam : this.getTargetedAssetMembers() ) {
			if(tam.getAsset().getAssetId().toString().equals(assetId)){
				tams.add(tam);
			}
		}
		
		// If this selected member contains an attrDefinitionId
		if( selectedTargetedAssetMemberString.indexOf( Constants.ATTR_DEFINITION_SUFFIX ) >= 0 ){
			
			String metadataInformation = selectedTargetedAssetMemberString.substring( selectedTargetedAssetMemberString.indexOf("_") + 1 );				
			attrDefinitionId = metadataInformation.substring(0, metadataInformation.indexOf( Constants.ATTR_DEFINITION_SUFFIX ) );
			if( metadataInformation.indexOf("~") >= 0 ){
				
				attrValue = metadataInformation.substring( metadataInformation.indexOf("~") + 1 );
				for (Iterator<TargetedAssetMember> it = tams.iterator(); it.hasNext();) {
					TargetedAssetMember tam = it.next();
					// And this existing targeted asset member is targeted to the same attrDefinition							
					if( tam.getAttrDefinition() != null && tam.getAttrDefinition().getAttrDefinitionId().toString().equals(attrDefinitionId) ){
						// And this existing targeted asset member is targeted to the same metadata value							
						if( tam.getAttrValue() != null && tam.getAttrValue().equalsIgnoreCase( attrValue ) ){
							// Then the already is a targeted asset member defined for this asset/attr definition
							result = true;
							break;
						} else {
							this.getTargetedAssetMembers().remove( tam );
							tam.delete();
						}
					} else {
						this.getTargetedAssetMembers().remove( tam );
						tam.delete();
					}
				}
			}
		}
		
		// If we are targeting using hql
		else if(selectedTargetedAssetMemberString.contains("hql")){
			hql = selectedTargetedAssetMemberString.substring( selectedTargetedAssetMemberString.indexOf("~") + 1 );
			for (Iterator<TargetedAssetMember> it = tams.iterator(); it.hasNext();) {
				TargetedAssetMember tam = it.next();
				if(tam.getHql() != null && tam.getHql().equalsIgnoreCase(hql)){
					result = true;
					break;
				} else {
					this.getTargetedAssetMembers().remove( tam );
					tam.delete();
				}
			}
		}
		
		// This selected member must be targeted to a device/device group
		else {
			// If selectedTargetedAssetMemberString is a "~" delimited string (because the asset should be targeted to multiple devices/groups)
			String deviceInformation = selectedTargetedAssetMemberString.substring( selectedTargetedAssetMemberString.indexOf("_") + 1 );
			String[] deviceInformationParts = new String[]{ deviceInformation };
			if( deviceInformation.indexOf("~") > 0 ){
				// Split on the "~" in order to get each target (either device or device group)
				deviceInformationParts = deviceInformation.split("\\~");
			}
						
			// check if there are fewer deviceInformationParts than target asset members for this asset_id -- if so, something must have changed -- return false
			if( !createNewMembers) {
				for (Iterator<TargetedAssetMember> it = tams.iterator(); it.hasNext();) {
					TargetedAssetMember removedTam = it.next();
					boolean match = false;
					for( int j=0; j<deviceInformationParts.length; j++ ) {
						String deviceInformationPart = deviceInformationParts[j];
						
						deviceId = null;
						deviceGroupId = null;
						// If this selected targeted asset member is targeted to a device group
						if( deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) > 0 ){				
							deviceGroupId = deviceInformationPart.substring( 0, deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) );
							if( removedTam.getDeviceGrp() != null && removedTam.getDeviceGrp().getGrpId().toString().equals(deviceGroupId) ){
								match = true;
								break;
							}
						}else if (deviceInformationPart.length() > 0){
							deviceId = deviceInformationPart;
							if (removedTam.getDevice() != null && removedTam.getDevice().getDeviceId().toString().equals(deviceId)){
								match = true;
								break;
							}
						}
						else if (removedTam.getDeviceGrp() == null && removedTam.getDevice() == null) {
							// In case of empty values on deviceInformationPart and target asset member
							match = true;
							break;
						}
					}
					
					if (match == false) {
						this.getTargetedAssetMembers().remove( removedTam );
						removedTam.delete();
					}
				}
				
				return false;
			}
			
			// For each entity (either device or device group) that was targeted to this selected targeted asset member
			for( int j=0; j<deviceInformationParts.length; j++ ){
				// Reset all necessary variables
				String deviceInformationPart = deviceInformationParts[j];
				deviceId = null;
				deviceGroupId = null;
				device = null;
				deviceGroup = null;
				result = false;
				
				if(deviceInformationPart != null && deviceInformationPart.length() > 0){
					// If this selected targeted asset member is targeted to a device group
					if( deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) > 0 ){				
						deviceGroupId = deviceInformationPart.substring( 0, deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) );
					}else{
						deviceId = deviceInformationPart;
					}
				}
				
				// If this selected targeted asset member is targeted to a device group
				if( deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) > 0 ){				
					deviceGroupId = deviceInformationPart.substring( 0, deviceInformationPart.indexOf( Constants.DEVICE_GROUP_SUFFIX ) );
				}else if( deviceInformationPart.length() > 0 ){
					deviceId = deviceInformationPart;
				}
								
				// Attempt to locate an existing targeted asset member
				for (Iterator<TargetedAssetMember> it = tams.iterator(); it.hasNext();) {
					TargetedAssetMember tam = it.next();
					// If this selected targeted asset member is targeted to a device group
					if( deviceGroupId != null ){
						// And this existing targeted asset member is targeted to the same device group									
						if( tam.getDeviceGrp() != null && tam.getDeviceGrp().getGrpId().toString().equals(deviceGroupId) ){
							// Then the already is a targeted asset member defined for this asset/device group
							result = true;
							break;
						}
					}
					// If this selected targeted asset member is targeted to a device
					else if( deviceId != null ){
						// And this existing targeted asset member is targeted to the same device									
						if( tam.getDevice() != null && tam.getDevice().getDeviceId().toString().equals(deviceId) ){
							// Then the already is a targeted asset member defined for this asset/device							
							result = true;
							break;
						}
					}
					// If there are no targets
					else if( tam.getDeviceGrp() == null && tam.getDevice() == null ){
						// Then the already is a targeted asset member defined for this asset/device							
						result = true;
						break;
					}
				}
				
				// If we did not find an existing targeted asset member
				if( result == false ){
					if(createNewMembers){
						// Load necessary objects
						asset = assetId != null ? Asset.getAsset( Long.valueOf( assetId ) ) : null;
						device = deviceId != null ? Device.getDevice( Long.valueOf( deviceId ) ) : null;
						deviceGroup = deviceGroupId != null ? Grp.getGrp( Long.valueOf( deviceGroupId ) ) : null;
						attrDefinition = attrDefinitionId != null ? AttrDefinition.getAttrDefinition( Long.valueOf( attrDefinitionId ) ) : null;
						
						// Create a new targeted asset member
						if (asset != null) {
							TargetedAssetMember tam = TargetedAssetMember.create(this, asset, device, deviceGroup, attrDefinition, attrValue, hql, seqNum );
							this.getTargetedAssetMembers().add( tam );
							memberCreated = true;
						}
					}else{
						break;
					}
				}						
			}
		}
		
		if( result == false && createNewMembers && memberCreated == false ){
			// Load necessary objects
			asset = assetId != null ? Asset.getAsset( Long.valueOf( assetId ) ) : null;
			device = deviceId != null ? Device.getDevice( Long.valueOf( deviceId ) ) : null;
			deviceGroup = deviceGroupId != null ? Grp.getGrp( Long.valueOf( deviceGroupId ) ) : null;
			attrDefinition = attrDefinitionId != null && attrDefinitionId.length() > 0 ? AttrDefinition.getAttrDefinition( Long.valueOf( attrDefinitionId ) ) : null;
			if (attrDefinition == null)
				attrValue = null;
			
			// Create a new targeted asset member
			if (asset != null) {
				TargetedAssetMember tam = TargetedAssetMember.create(this, asset, device, deviceGroup, attrDefinition, attrValue, hql, seqNum );
				this.getTargetedAssetMembers().add( tam );
			}
		}
		
		return result;
	}	
	
	/*
	 * Given a device, find the right Asset
	 */
	public IAsset selectAsset(Device device, Displayarea displayarea) throws KmfException, ClassNotFoundException, ParseException
	{
		IAsset result = null;

		/*
		 * Figure out which referenced file should be used by determining the asset this device is a target of.
		 */	
	    if( this.getTargetedAssetMembers() == null || this.getTargetedAssetMembers().size() == 0 ){
	    	return null;
		}

	    // Attempt to get the saved collection of targetedAssetMembers for this asset/device (we do this for performance reasons)
	    HashMap<Long, LinkedHashSet<TargetedAssetMember>> targetedAssetMembers = ContentScheduler.getTargetedAssetMembers().get( this.getAssetId() );
	    
	    // If we have not already queried for targeted asset members for this targeted asset/device
	    if( targetedAssetMembers == null ){
	    	targetedAssetMembers = getTargetedAssetMembersForCache();
	    	ContentScheduler.getTargetedAssetMembers().put( this.getAssetId(), targetedAssetMembers );
	    }
	    
	    // A list to hold targeted asset members that are not excluded for this device
	    List<TargetedAssetMember> includedTargetedAssetMembers = targetedAssetMembers.get(device.getDeviceId()) != null ? new LinkedList(targetedAssetMembers.get(device.getDeviceId())) : new LinkedList();
	    
	    // Remove the excluded assets for this device
	    for(Iterator<TargetedAssetMember> i = includedTargetedAssetMembers.iterator(); i.hasNext();){
	    	// Remove the member if its excluded
	    	if(ContentScheduler.isAssetExcludedOrExpired(i.next().getAsset().getAssetId(), device)){
	    		i.remove();
	    	}
	    }
	    
		// If this device is targeted to more than one TargetedAssetMember
		if( includedTargetedAssetMembers.size() > 1 )
		{
			// If we are meant to select the TargetedAssetMembers in random order
			MultipleTargetsBehaviorType multipleTargetsBehavior = this.getMultipleTargetsBehavior() != null ? this.getMultipleTargetsBehavior() : MultipleTargetsBehaviorType.RANDOM;
			if( multipleTargetsBehavior.getPersistentValue().equalsIgnoreCase( MultipleTargetsBehaviorType.RANDOM.getPersistentValue() ) )
			{
				// Randomly select an asset for this device
				Random rand = new Random();
				
				result = includedTargetedAssetMembers.get( rand.nextInt(includedTargetedAssetMembers.size()) ).getAsset();
			}
			// If we are meant to select the targeted asset members in sequential order			
			else
			{
				String targetedAssetKey = this.getAssetId().toString() +"_"+ device.getDeviceId().toString() +"_"+ displayarea.getDisplayareaId().toString();
				Integer previouslyScheduledTargetedAssetPosition = ContentScheduler.getScheduledTargetedAssets().get( targetedAssetKey );
				
				// If this targeted asset has not yet been scheduled to this device/displayarea
				if( previouslyScheduledTargetedAssetPosition == null ){
					previouslyScheduledTargetedAssetPosition = -1;
				}
				// If we have already looped through all TargetedAssetMembers
				else if( previouslyScheduledTargetedAssetPosition >= includedTargetedAssetMembers.size() - 1 )
				{
					// Reset the index so that we start at the beginning again
					previouslyScheduledTargetedAssetPosition = -1;
				}
				
				// Get the next targeted asset member and update the scheduledTargetedAssets collection with the current position
				Integer currentScheduledTargetedAssetPosition = previouslyScheduledTargetedAssetPosition.intValue() + 1;
				result = includedTargetedAssetMembers.get( currentScheduledTargetedAssetPosition.intValue() ).getAsset();
				ContentScheduler.getScheduledTargetedAssets().put( targetedAssetKey, currentScheduledTargetedAssetPosition );				
			}
		}
		// If this device is targeted to one and only one TargetedAssetMember 
		else if( includedTargetedAssetMembers.size() == 1 )
		{
			// Use it
			result = includedTargetedAssetMembers.get(0).getAsset();
		}
		// If we did not find this device to be targeted to any TargetedAssetMembers
		else if( includedTargetedAssetMembers.size() == 0 )
		{
			// Assign a referenced file one way or the other
			if( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.FIRST_IN_LIST ) )
			{
				// Get the first asset in the set of targeted asset members
				Iterator<TargetedAssetMember> i = this.getTargetedAssetMembers().iterator();
				
				// Keep iterating till we find an asset that is not excluded for this device
				do{
					result = i.next().getAsset();
				}while(ContentScheduler.isAssetExcludedOrExpired(result.getAssetId(), device) && i.hasNext());
				
				// If we ran thru all the assets in the list and the last asset is also excluded from this device
				if(ContentScheduler.isAssetExcludedOrExpired(result.getAssetId(), device)){
					result = null;
				}
			}
			else if ( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.RANDOM ) )
			{
				Random rand = new Random();
				HashSet randomNumbersGenerated = new HashSet();
				
				// Keep picking assets randomly till we find an asset that is not excluded from this device
				do{
					int randomInt;
					
					// Generate a random number we haven't generated before
					do{
						randomInt = rand.nextInt( this.getTargetedAssetMembers().size() );
					}while(randomNumbersGenerated.contains(randomInt));
					
					randomNumbersGenerated.add(randomInt);
					
					int counter = 0;
					for( Iterator<TargetedAssetMember> i=this.getTargetedAssetMembers().iterator(); i.hasNext() && result == null; ){
						TargetedAssetMember tam = i.next();
						if( counter++ == randomInt ){
							result = tam.getAsset();
						}
					}
					
					// If we have iterated thru all the members of this targeted asset
					if(randomNumbersGenerated.size() == this.getTargetedAssetMembers().size()){
						
						// Make sure that the last randomly selected asset is not excluded
						if(ContentScheduler.isAssetExcludedOrExpired(result.getAssetId(), device)){
							result = null;
						}
						
						// Get out of the while loop
						break;
					}
					
				}
				// Keep picking assets randomly till we find an asset that is not excluded from this device
				while(ContentScheduler.isAssetExcludedOrExpired(result.getAssetId(), device));
			}else if( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.SKIP_ASSET ) ){
				result = null;
			}
		}		
		return result;
	}
	
	public Object[] getAllPossibleMemberAssets(Device device) throws KmfException, ClassNotFoundException, ParseException
	{
		Boolean foundNoTargets = false;
		List<IAsset> listOfPossibleMembers = new LinkedList<IAsset>();

		/*
		 * Figure out which referenced file should be used by determining the asset this device is a target of.
		 */	
	    if( this.getTargetedAssetMembers() == null || this.getTargetedAssetMembers().size() == 0 ){
	    	return null;
		}

	    // Attempt to get the saved collection of targetedAssetMembers for this asset/device (we do this for performance reasons)
	    HashMap<Long, LinkedHashSet<TargetedAssetMember>> targetedAssetMembers = ContentScheduler.getTargetedAssetMembers().get( this.getAssetId() );
	    
	    // If we have not already queried for targeted asset members for this targeted asset/device
	    if( targetedAssetMembers == null ){
	    	targetedAssetMembers = getTargetedAssetMembersForCache();
	    	ContentScheduler.getTargetedAssetMembers().put( this.getAssetId(), targetedAssetMembers );
	    }
	    
	    // A list to hold targeted asset members that are not excluded for this device
	    List<TargetedAssetMember> includedTargetedAssetMembers = targetedAssetMembers.get(device.getDeviceId()) != null ? new LinkedList(targetedAssetMembers.get(device.getDeviceId())) : new LinkedList();
	    
	    // Remove the excluded assets for this device
	    for(Iterator<TargetedAssetMember> i = includedTargetedAssetMembers.iterator(); i.hasNext();){
	    	// Remove the member if its excluded
	    	if(ContentScheduler.isAssetExcludedOrExpired(i.next().getAsset().getAssetId(), device)){
	    		i.remove();
	    	}
	    }
	    
		// If this device is targeted to more than one TargetedAssetMember
		if( includedTargetedAssetMembers.size() > 1 ){
			for(TargetedAssetMember tam: includedTargetedAssetMembers){
				listOfPossibleMembers.add(tam.getAsset());
			}
		}
		// If this device is targeted to one and only one TargetedAssetMember 
		else if( includedTargetedAssetMembers.size() == 1 ){
			// Use it
			listOfPossibleMembers.add(includedTargetedAssetMembers.get(0).getAsset());
		}
		// If we did not find this device to be targeted to any TargetedAssetMembers
		else if( includedTargetedAssetMembers.size() == 0 ){
			
			// Set the flag to denote that no valid members were found
			foundNoTargets = true;
			
			// Assign a referenced file one way or the other
			if( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.FIRST_IN_LIST ) ){
				// Keep iterating till we find an asset that is not excluded for this device
				for(TargetedAssetMember tam : this.getTargetedAssetMembers()){
					if(ContentScheduler.isAssetExcludedOrExpired(tam.getAsset().getAssetId(), device) == false){
						listOfPossibleMembers.add(tam.getAsset());
						break;
					}
				}
			}
			else if ( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.RANDOM ) ){
				// Keep iterating till we find assets that are not excluded for this device
				for(TargetedAssetMember tam : this.getTargetedAssetMembers()){
					if(ContentScheduler.isAssetExcludedOrExpired(tam.getAsset().getAssetId(), device) == false){
						listOfPossibleMembers.add(tam.getAsset());
					}
				}
			}else if( this.getDefaultAssetAffinityType().equals( DefaultAssetAffinityType.SKIP_ASSET ) ){
				return null;
			}
		}		
		return new Object[]{foundNoTargets, listOfPossibleMembers};
	}
	
	public HashMap<Long, LinkedHashSet<TargetedAssetMember>> getTargetedAssetMembersForCache() throws ParseException
	{
		HashMap<Long, LinkedHashSet<TargetedAssetMember>> result = new HashMap<Long, LinkedHashSet<TargetedAssetMember>>();
		
		// Select all TargetedAssetMembers that are targeted to a device
		Session session = HibernateSession.currentSession();
		String hql = "SELECT DISTINCT tam FROM TargetedAssetMember as tam WHERE tam.targetedAsset.assetId = :assetId AND (tam.device IS NOT NULL OR tam.deviceGrp IS NOT NULL)";
		List<TargetedAssetMember> tams = session.createQuery( hql ).setParameter("assetId", this.getAssetId()).list();
		
		// Populate the result hashmap
		for(TargetedAssetMember tam : tams){
			if(tam.getDevice() != null){
				LinkedHashSet<TargetedAssetMember> underlyingList = result.get(tam.getDevice().getDeviceId()) != null ? result.get(tam.getDevice().getDeviceId()) : new LinkedHashSet<TargetedAssetMember>();
				underlyingList.add(tam);
				result.put(tam.getDevice().getDeviceId(), underlyingList);
			}else if(tam.getDeviceGrp() != null){
				for(GrpMember gm : tam.getDeviceGrp().getGrpMembers()){
					DeviceGrpMember dgm = (DeviceGrpMember)gm;
					LinkedHashSet<TargetedAssetMember> underlyingList = result.get(dgm.getDevice().getDeviceId()) != null ? result.get(dgm.getDevice().getDeviceId()) : new LinkedHashSet<TargetedAssetMember>();
					underlyingList.add(tam);
					result.put(dgm.getDevice().getDeviceId(), underlyingList);
				}
			}
		}
		
		// In addition -- see if there are any TargetedAssetMembers that are targeted to the given device via metadata
		for( TargetedAssetMember tam : this.getTargetedAssetMembers() )
		{
			if( tam.getAttrDefinition() != null )
			{
				AttrDefinition ad = tam.getAttrDefinition();
				String deviceGroupId = "-1"; // Search all device groups
				String selectedSearchOption = ad.getAttrDefinitionId().toString();
				String searchString = null;
				String[] selectedSearchOptionItems = null;
				String dateRangeType = null;
				String minDate = null;
				String maxDate = null;
				String minNumber = null;
				String maxNumber = null;

				if( tam.getAttrValue() != null )
				{
					// If the selected items is a delimited string -- parse it
					String[] attrValueParts = new String[]{ tam.getAttrValue() };
					if(tam.getAttrValue().indexOf("~") >= 0){
						attrValueParts = tam.getAttrValue().split("\\~");				
					}							
					if( tam.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.DATE.getPersistentValue() ) ){
						dateRangeType = attrValueParts[0];
						minDate = attrValueParts[1];
						maxDate = attrValueParts[2];
						
						// If this isn't a Custom date range
						DateRangeType drt = DateRangeType.getDateRangeType(dateRangeType);
						if(drt != null){
							String[] dates = drt.getDateRange(-1, -1);
							minDate = dates[0];
							maxDate = dates[1];
						}
					}else if( tam.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.NUMBER.getPersistentValue() ) ){
						minNumber = attrValueParts[0];
						maxNumber = attrValueParts[1];
					}else if( tam.getAttrDefinition().getType().getPersistentValue().equalsIgnoreCase( AttrType.STRING.getPersistentValue() ) )
					{
						// If this is a multi-select attrDefinition
						if( tam.getAttrDefinition().getSearchInterface().getPersistentValue().equalsIgnoreCase( SearchInterfaceType.MULTI_SELECT.getPersistentValue() ) ){
							selectedSearchOptionItems = attrValueParts;
						}else{
							searchString = tam.getAttrValue();
						}
					}	
				}

				// We will use "-1" for page size to get all results
				EntityResultsPage page = (EntityResultsPage)Device.searchDevices( ad, DeviceSearchType.DEVICE_NAME, null, deviceGroupId, selectedSearchOption, 
						searchString, selectedSearchOptionItems, minDate, maxDate,
						minNumber, maxNumber, null, 0, -1, false, false, true, false );
				
				// For each returned deviceId
				for( Iterator<Long> i = page.getList().iterator(); i.hasNext(); ){
					Long deviceId = i.next();
					LinkedHashSet<TargetedAssetMember> underlyingList = result.get(deviceId) != null ? result.get(deviceId) : new LinkedHashSet<TargetedAssetMember>();
					underlyingList.add(tam);
					result.put(deviceId, underlyingList);
				}
			}else if(tam.getHql() != null && tam.getHql().length() > 0){
				List<Long> deviceIds = HibernateSession.currentSession().createQuery(tam.getHql()).list();
				for(long deviceId : deviceIds){
					LinkedHashSet<TargetedAssetMember> underlyingList = result.get(deviceId) != null ? result.get(deviceId) : new LinkedHashSet<TargetedAssetMember>();
					underlyingList.add(tam);
					result.put(deviceId, underlyingList);
				}
			}
		}
		
		// Copy the result
		HashMap<Long, LinkedHashSet<TargetedAssetMember>> copiedResult = new HashMap<Long, LinkedHashSet<TargetedAssetMember>>();
		copiedResult.putAll(result);
		result.clear();
		
		// Sort the result set
		for(Entry<Long, LinkedHashSet<TargetedAssetMember>> e : copiedResult.entrySet()){
			Long deviceId = e.getKey();
			LinkedHashSet<TargetedAssetMember> members = e.getValue();
			
			ArrayList<TargetedAssetMember> list = new ArrayList<TargetedAssetMember>();
			list.addAll(members);
			
			BeanComparator comparator = new BeanComparator("seqNum");
			Collections.sort( list, comparator );
			
			members.clear();
			members.addAll(list);
			
			result.put(deviceId, members);
		}
		
		return result;
	}
	
	public List<TargetedAssetMember> getTargetedAssetMembers(Long assetId) throws HibernateException
	{
		String hql = "SELECT tam FROM TargetedAssetMember tam WHERE tam.targetedAsset.assetId = :targetedAssetId AND tam.asset.assetId = :assetId";
		return HibernateSession.currentSession().createQuery(hql).setParameter("targetedAssetId", this.getAssetId()).setParameter("assetId", assetId).list();		
	}
	
	public List<TargetedAssetMember> getTargetedAssetMembersByDevice(Long deviceId) throws HibernateException
	{
		String hql = "SELECT tam FROM TargetedAssetMember tam WHERE tam.targetedAsset.assetId = :targetedAssetId AND tam.device.deviceId = :deviceId";
		return HibernateSession.currentSession().createQuery(hql).setParameter("targetedAssetId", this.getAssetId()).setParameter("deviceId", deviceId).list();		
	}
	
	/**
	 * @return Returns the targetedAssetMembers.
	 */
	public Set<TargetedAssetMember> getTargetedAssetMembers() {
		return targetedAssetMembers;
	}
	/**
	 * @param targetedAssetMembers The targetedAssetMembers to set.
	 */
	public void setTargetedAssetMembers(Set<TargetedAssetMember> targetedAssetMembers) {
		this.targetedAssetMembers = targetedAssetMembers;
	}
	/**
	 * @return Returns the defaultAssetAffinityType.
	 */
	public DefaultAssetAffinityType getDefaultAssetAffinityType() {
		return defaultAssetAffinityType;
	}
	
	/**
	 * @param defaultAssetAffinityType The defaultAssetAffinityType to set.
	 */
	public void setDefaultAssetAffinityType(DefaultAssetAffinityType defaultAssetAffinityType) {
		this.defaultAssetAffinityType = defaultAssetAffinityType;
	}
	
	/**
	 * @return the multipleTargetsBehavior
	 */
	public MultipleTargetsBehaviorType getMultipleTargetsBehavior() {
		return multipleTargetsBehavior;
	}

	/**
	 * @param multipleTargetsBehavior the multipleTargetsBehavior to set
	 */
	public void setMultipleTargetsBehavior(
			MultipleTargetsBehaviorType multipleTargetsBehavior) {
		this.multipleTargetsBehavior = multipleTargetsBehavior;
	}

	public Boolean getUseMemberPairing() {
		return useMemberPairing;
	}

	public void setUseMemberPairing(Boolean useMemberPairing) {
		this.useMemberPairing = useMemberPairing;
	}
	
}
