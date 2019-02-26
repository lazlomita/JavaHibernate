package com.kuvata.kmf.billing;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.kuvata.kmf.Entity;
import com.kuvata.kmf.HibernateSession;

public class VenuePartner extends Entity {

	private Long venuePartnerId;
	private String venuePartnerName;
	private Float revenueSharePercentage;
	private Float visitors;
	private Float dwellTime;
	private Float impressionDiscount;
	private Float openHours;
	
	public static VenuePartner getVenuePartner(Long venuePartnerId){
		return (VenuePartner)Entity.load(VenuePartner.class, venuePartnerId);
	}
	public Long getEntityId()
	{
		return this.getVenuePartnerId();
	}
	
	public Long getVenuePartnerId() {
		return venuePartnerId;
	}
	public void setVenuePartnerId(Long venuePartnerId) {
		this.venuePartnerId = venuePartnerId;
	}
	public String getVenuePartnerName() {
		return venuePartnerName;
	}
	public void setVenuePartnerName(String venuePartnerName) {
		this.venuePartnerName = venuePartnerName;
	}
	public Float getRevenueSharePercentage() {
		return revenueSharePercentage;
	}
	public void setRevenueSharePercentage(Float revenueSharePercentage) {
		this.revenueSharePercentage = revenueSharePercentage;
	}
	
	public static List getVenuePartners(){
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT vp "
				+ "FROM VenuePartner as vp "
				+ "ORDER BY UPPER(vp.venuePartnerName)";
		
		return session.createQuery( hql ).list();
	}
	
	public static List getVenuePartnerNames(List venuePartnerIds){
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT vp.venuePartnerName "
				+ "FROM VenuePartner as vp WHERE vp.venuePartnerId IN (:venuePartnerIds) "
				+ "ORDER BY UPPER(vp.venuePartnerName)";
		
		return session.createQuery( hql ).setParameterList("venuePartnerIds", venuePartnerIds).list();
	}
	
	public static List<VenuePartner> getVenuePartners(Set venuePartnerIds){
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT vp FROM VenuePartner as vp WHERE vp.venuePartnerId IN (:venuePartnerIds)";
		
		return session.createQuery( hql ).setParameterList("venuePartnerIds", venuePartnerIds).list();
	}
	
	public static VenuePartner getVenuePartner(String venuePartnerName){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT vp "
				+ "FROM VenuePartner as vp WHERE vp.venuePartnerName = ?";;
		
		List l = session.createQuery( hql ).setParameter(0, venuePartnerName).list();
		
		if(l.size() > 0){
			return (VenuePartner)l.get(0);
		}else{
			return null;
		}
	}
	
	public int getDeviceCount(){
		int result = 0;
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT count(d) "
				+ "FROM Device as d WHERE d.venuePartner.venuePartnerId = ? ";
		
		Iterator i = session.createQuery(hql).setParameter(0, this.getVenuePartnerId()).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}
	
	private void removeDeviceAssociations(){
		// Remove all device associations for this venue partner
		Session session = HibernateSession.currentSession();
		HibernateSession.beginTransaction();			
		String hql = "UPDATE Device SET venue_partner_id = NULL WHERE venue_partner_id = ?";
		session.createQuery( hql ).setParameter(0, this.getVenuePartnerId()).executeUpdate();
		HibernateSession.commitTransaction();
	}
	
	public void delete() throws HibernateException
	{
		this.removeDeviceAssociations();
		super.delete();
	}
	public Float getVisitors() {
		return visitors;
	}
	public void setVisitors(Float visitors) {
		this.visitors = visitors;
	}
	public Float getDwellTime() {
		return dwellTime;
	}
	public void setDwellTime(Float dwellTime) {
		this.dwellTime = dwellTime;
	}
	public Float getImpressionDiscount() {
		return impressionDiscount;
	}
	public void setImpressionDiscount(Float impressionDiscount) {
		this.impressionDiscount = impressionDiscount;
	}
	public Float getOpenHours() {
		return openHours;
	}
	public void setOpenHours(Float openHours) {
		this.openHours = openHours;
	}
	
}
