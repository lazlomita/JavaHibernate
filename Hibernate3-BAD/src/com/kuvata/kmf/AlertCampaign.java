package com.kuvata.kmf;

import com.kuvata.kmf.billing.Campaign;
import com.kuvata.kmf.logging.HistorizableCollectionMember;

public class AlertCampaign extends Entity implements HistorizableCollectionMember
{
	private Long alertCampaignId;
	private Alert alert;
	private Campaign campaign;
	
	/**
	 * 
	 *
	 */
	public AlertCampaign()
	{		
	}
	
	/**
	 * Creates a new AlertDevice object for the given alert and device
	 * @param device
	 * @param asset
	 */
	public static AlertCampaign create(Alert alert, Campaign campaign)
	{
		AlertCampaign alertCampaign = new AlertCampaign();
		alertCampaign.setAlert( alert );
		alertCampaign.setCampaign( campaign );
		alertCampaign.save();
		return alertCampaign;
	}
	
	/**
	 * 
	 */
	public Long getEntityId()
	{
		return this.getAlertCampaignId();
	}
	
	/**
	 * 
	 */
	public Long getHistoryEntityId()
	{
		return this.getEntityId();
	}	
	
	/**
	 * 
	 */
	public String getEntityName()
	{
		String result = "";
		if( this.getCampaign() != null ){
			result = this.getCampaign().getCampaignName();
		}
		return result;
	}	

	/**
	 * @return Returns the alert.
	 */
	public Alert getAlert() {
		return alert;
	}
	

	/**
	 * @param alert The alert to set.
	 */
	public void setAlert(Alert alert) {
		this.alert = alert;
	}
	
	public Campaign getCampaign() {
		return campaign;
	}

	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}

	public Long getAlertCampaignId() {
		return alertCampaignId;
	}

	public void setAlertCampaignId(Long alertCampaignId) {
		this.alertCampaignId = alertCampaignId;
	}
}