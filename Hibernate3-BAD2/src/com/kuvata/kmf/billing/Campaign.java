package com.kuvata.kmf.billing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.kuvata.kmf.Asset;
import com.kuvata.kmf.Entity;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.comparator.BeanPropertyComparator;
import com.kuvata.kmf.logging.Historizable;
import com.kuvata.kmf.logging.HistorizableSet;

public class Campaign extends Entity implements Historizable, Comparable<Campaign>{
	
	private Long campaignId;
	private String campaignName;
	private Date startDt;
	private Date endDt;
	private Integer numDevices;
	private Integer frequency;
	private Boolean isBillable;
	private Advertiser advertiser;
	private Float cpm;
	private Integer dailyMinImpressions;
	private Integer maxChargedImpressions;
	private String comments;
	private Set<CampaignAsset> campaignAssets = new HistorizableSet<CampaignAsset>();
	private TreeSet<CampaignAsset> sortedCampaignAssets;
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
	
	public Long getEntityId(){
		return campaignId;
	}

	public static List<Campaign> getCampaigns(){
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT c "
				+ "FROM Campaign as c "
				+ "ORDER BY UPPER(c.campaignName)";
		
		List l = session.createQuery( hql ).list();
		
		return l;
	}
	
	public static Campaign getCampaign(Long campaignId){
		return (Campaign)Entity.load(Campaign.class, campaignId);
	}
	
	public static List<Campaign> getCampaigns(String campaignName){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT c "
				+ "FROM Campaign as c "
				+ "WHERE c.campaignName = :campaignName";
		
		return session.createQuery( hql ).setParameter("campaignName", campaignName).list();
	}
	
	public static List<Campaign> getCampaigns(Set campaignIds){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT c "
				+ "FROM Campaign as c "
				+ "WHERE c.campaignId IN (:campaignIds)";
		
		return session.createQuery( hql ).setParameterList("campaignIds", campaignIds).list();
	}
	
	public CampaignAsset createCampaignAsset(Asset asset){
		CampaignAsset ca = new CampaignAsset();
		ca.setAsset(asset);
		ca.setCampaign(this);
		ca.save();
		return ca;
	}
	
	public void delete(){
		
		HibernateSession.startBulkmode();
		
		// Delete all campaign assets
		for(CampaignAsset ca : campaignAssets){
			ca.delete();
		}
		
		super.delete();
		
		HibernateSession.currentSession().createQuery("DELETE FROM AlertCampaign ac WHERE ac.campaign.campaignId = ?")
		.setParameter(0, this.getCampaignId()).executeUpdate();
		
		HibernateSession.stopBulkmode();
	}
	
	public Long getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public Date getStartDt() {
		return startDt;
	}

	public void setStartDt(Date startDt) {
		this.startDt = startDt;
	}

	public Date getEndDt() {
		return endDt;
	}

	public void setEndDt(Date endDt) {
		this.endDt = endDt;
	}

	public Integer getNumDevices() {
		return numDevices;
	}

	public void setNumDevices(Integer numDevices) {
		this.numDevices = numDevices;
	}

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public Set<CampaignAsset> getCampaignAssets() {
		return campaignAssets;
	}

	public void setCampaignAssets(Set<CampaignAsset> campaignAssets) {
		this.campaignAssets = campaignAssets;
	}

	public TreeSet<CampaignAsset> getSortedCampaignAssets() {
		if(sortedCampaignAssets == null){
			BeanPropertyComparator comparator1 = new BeanPropertyComparator( "assetName" );
			BeanPropertyComparator comparator2 = new BeanPropertyComparator( "asset", comparator1 );
			sortedCampaignAssets = new TreeSet<CampaignAsset>(comparator2);
			sortedCampaignAssets.addAll(campaignAssets);
		}
		// If sortedCampaignAssets has only one asset, we need to initialize it
		if(sortedCampaignAssets.size() == 1){
			Hibernate.initialize(sortedCampaignAssets.last().getAsset());
		}
		return sortedCampaignAssets;
	}
	
	// Used to sort a list of users by user name
	public int compareTo(Campaign c){
		return this.getCampaignName().toLowerCase().compareTo(c.getCampaignName().toLowerCase());
	}

	public Boolean getIsBillable() {
		return isBillable;
	}

	public void setIsBillable(Boolean isBillable) {
		this.isBillable = isBillable;
	}

	public Advertiser getAdvertiser() {
		return advertiser;
	}

	public void setAdvertiser(Advertiser advertiser) {
		this.advertiser = advertiser;
	}

	public Float getCpm() {
		return cpm;
	}

	public void setCpm(Float cpm) {
		this.cpm = cpm;
	}
	
	public Integer getMaxChargedImpressions() {
		return maxChargedImpressions;
	}

	public void setMaxChargedImpressions(Integer maxChargedImpressions) {
		this.maxChargedImpressions = maxChargedImpressions;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Integer getDailyMinImpressions() {
		return dailyMinImpressions;
	}

	public void setDailyMinImpressions(Integer dailyMinImpressions) {
		this.dailyMinImpressions = dailyMinImpressions;
	}
}
