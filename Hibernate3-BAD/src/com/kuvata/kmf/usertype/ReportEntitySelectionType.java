package com.kuvata.kmf.usertype;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Device;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.SelectedEntities;
import com.kuvata.kmf.billing.Advertiser;
import com.kuvata.kmf.billing.Campaign;
import com.kuvata.kmf.billing.VenuePartner;
import com.kuvata.kmf.permissions.FilterManager;
import com.kuvata.kmf.permissions.FilterType;
import com.kuvata.kmf.util.Reformat;

public class ReportEntitySelectionType {

	private String selectedIds;
	private Class entityClass;
	
	private Long selectionId;
	private String selectionNames;
	
	private int entityCount;

	public ReportEntitySelectionType(String selectedIds, Class entityClass, boolean createSelectedEntities, boolean populateNames){
		this.selectedIds = selectedIds;
		this.entityClass = entityClass;
		
		if(createSelectedEntities){
			if( selectedIds != null && selectedIds.length() > 0) {
				
				String entityIdStr = null;
				String className = null;
				if(entityClass.equals(Asset.class)){
					FilterManager.enableFilter(FilterType.ASSETS_FILTER);
					entityIdStr = "assetId";
					className = "Asset";
				}else if(entityClass.equals(Device.class)){
					FilterManager.enableFilter(FilterType.DEVICES_FILTER);
					entityIdStr = "deviceId";
					className = "Device";
				}else if(entityClass.equals(Advertiser.class)){
					FilterManager.enableFilter(FilterType.ADVERTISERS_FILTER);
					entityIdStr = "advertiserId";
					className = "Advertiser";
				}else if(entityClass.equals(VenuePartner.class)){
					FilterManager.enableFilter(FilterType.VENUE_PARTNERS_FILTER);
					entityIdStr = "venuePartnerId";
					className = "VenuePartner";
				}else if(entityClass.equals(Campaign.class)){
					FilterManager.enableFilter(FilterType.CAMPAIGNS_FILTER);
					entityIdStr = "campaignId";
					className = "Campaign";
				}
				
				if(selectedIds.equals("-1") == false){
					this.selectionId = SelectedEntities.createSelectedEntities(selectedIds, null);
					this.entityCount = SelectedEntities.getSelectedIds(this.selectionId).size();
				}else{
					List<Long> allowedIds = HibernateSession.currentSession().createQuery("SELECT " + entityIdStr + " FROM " + className).list();
					this.entityCount = allowedIds.size();
					if(allowedIds.size() > 0){
						this.selectionId = SelectedEntities.createSelectedEntities(allowedIds, null);
					}
				}
			}
		}
		
		if(populateNames){
			if(entityClass.equals(Asset.class)){
				if(this.selectedIds.equals("-1")){
					this.selectionNames = "All Assets";
				}else{
					this.selectionNames = getSelectedAssetNames();
				}
			}else if(entityClass.equals(Device.class)){
				if(this.selectedIds.equals("-1")){
					this.selectionNames = "All Devices";
				}else{
					this.selectionNames = getSelectedDeviceNames();
				}
			}else if(entityClass.equals(Campaign.class)){
				Campaign c = Campaign.getCampaign(Long.parseLong(selectedIds));
				if(c != null){
					this.selectionNames = c.getCampaignName();
				}
			}else if(entityClass.equals(Advertiser.class) || entityClass.equals(VenuePartner.class)){
				if(this.selectedIds.equals("-1")){
					this.selectionNames = entityClass.equals(Advertiser.class) ? "All Advertisers" : "All Venues";
				}else{
					List<Long> ids = new LinkedList();
					for(String s : this.selectedIds.split(", ")){
						ids.add(Long.parseLong(s));
					}
					if(entityClass.equals(Advertiser.class)){
						this.selectionNames = Reformat.convertListToCommaDelimitedString(Advertiser.getAdvertiserNames(ids));
					}else{
						this.selectionNames = Reformat.convertListToCommaDelimitedString(VenuePartner.getVenuePartnerNames(ids));
					}
				}
			}
		}
	}
	
	public static ReportEntitySelectionType getReportEntitySelectionTypeByClass(ReportEntitySelectionType[] selections, Class entityClass){
		ReportEntitySelectionType result = null;
		for(ReportEntitySelectionType rest : selections){
			if(rest.entityClass.equals(entityClass)){
				result = rest;
			}
		}
		return result;
	}
	
	private String getSelectedAssetNames()
	{
		String result = "";
		if( selectionId != null )
		{
			String hql = "SELECT asset " 
				  + "FROM Asset as asset "
				  + "WHERE asset.assetId IN (  SELECT entityId FROM SelectedEntities WHERE selectionId = :assetSelectionId ) "
				  + "ORDER BY UPPER(asset.assetName)";		
			Session session = HibernateSession.currentSession();
			Query q = session.createQuery( hql );
			List<IAsset> results = q.setParameter("assetSelectionId", selectionId).list();
			for( Iterator<IAsset> i=results.iterator(); i.hasNext(); )
			{
				Asset a = Asset.convert(i.next());
				if( result.length() > 0 ){
					result += ", ";
				}
				result += a.getAssetName();				
			}
		}	
		return result;
	}
	
	private String getSelectedDeviceNames()
	{
		String result = "";
		if( selectionId != null )
		{
			String hql = "SELECT device " 
				  + "FROM Device as device "
				  + "WHERE device.deviceId IN (  SELECT entityId FROM SelectedEntities WHERE selectionId = :deviceSelectionId ) "
				  + "ORDER BY UPPER(device.deviceName)";	
			
			Session session = HibernateSession.currentSession();
			Query q = session.createQuery( hql );
			List<Device> results = q.setParameter("deviceSelectionId", selectionId).list();
			for( Iterator<Device> i=results.iterator(); i.hasNext(); )
			{
				Device d = i.next();
				if( result.length() > 0 ){
					result += ", ";
				}
				result += d.getDeviceName();				
			}
		}
		return result;
	}
	
	protected void finalize(){
		// Delete from selected entities
		if(selectionId != null){
			SelectedEntities.deleteSelectedEntities(selectionId);
		}
	}
	
	public void delete() {
		if(selectionId != null){
			SelectedEntities.deleteSelectedEntities(selectionId);
		}
	}
	
	public String getSelectedIds() {
		return selectedIds;
	}
	public Class getEntityClass() {
		return entityClass;
	}

	public Long getSelectionId() {
		return selectionId;
	}

	public String getSelectionNames() {
		return selectionNames;
	}

	public void setSelectionId(Long selectionId) {
		this.selectionId = selectionId;
	}

	public int getEntityCount() {
		return entityCount;
	}
}
