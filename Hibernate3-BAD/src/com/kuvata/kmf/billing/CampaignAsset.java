package com.kuvata.kmf.billing;

import java.util.List;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Entity;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.IAsset;
import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class CampaignAsset extends Entity implements HistorizableCollectionMember{

	private Long campaignAssetId;
	private Campaign campaign;
	private IAsset asset;
	
	public static CampaignAsset getCampaignAsset(Long campaignAssetId){
		return (CampaignAsset)Entity.load(CampaignAsset.class, campaignAssetId);
	}
	
	public static void deleteCampaignAssets(Asset a){
		HibernateSession.currentSession().createQuery("DELETE FROM CampaignAsset ca WHERE ca.asset.assetId = :assetId")
		.setParameter("assetId", a.getAssetId()).executeUpdate();
	}
	
	public static List getAssetCampaignNames(Asset a){
		return HibernateSession.currentSession().createQuery("SELECT ca.campaign.campaignName FROM CampaignAsset ca WHERE ca.asset.assetId = :assetId")
		.setParameter("assetId", a.getAssetId()).list();
	}
	
	public String getEntityName(){
		return asset.getAssetName();
	}
	public Long getHistoryEntityId(){
		return campaign.getCampaignId();
	}
	public Long getEntityId(){
		return campaignAssetId;
	}
	public Long getCampaignAssetId() {
		return campaignAssetId;
	}
	public void setCampaignAssetId(Long campaignAssetId) {
		this.campaignAssetId = campaignAssetId;
	}
	public Campaign getCampaign() {
		return campaign;
	}
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
	public IAsset getAsset() {
		return asset;
	}
	public void setAsset(IAsset asset) {
		this.asset = asset;
	}
}
