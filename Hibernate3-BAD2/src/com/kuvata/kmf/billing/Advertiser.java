package com.kuvata.kmf.billing;

import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.kuvata.kmf.Entity;
import com.kuvata.kmf.HibernateSession;

public class Advertiser extends Entity{

	private Long advertiserId;
	private String advertiserName;
	
	public static List<Advertiser> getAdvertisers(){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT a FROM Advertiser as a "
				+ "ORDER BY UPPER(a.advertiserName)";
		
		List l = session.createQuery( hql ).list();
		
		return l;
	}
	
	public static Advertiser getAdvertiser(String advertiserName){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT a FROM Advertiser as a WHERE a.advertiserName = ?";
		
		List l = session.createQuery( hql ).setParameter(0, advertiserName).list();
		
		if(l.size() > 0){
			return (Advertiser)l.get(0);
		}else{
			return null;
		}
	}
	
	public static List getAdvertiserNames(List advertiserIds){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT a.advertiserName FROM Advertiser as a WHERE a.advertiserId IN (:advertiserIds) ORDER BY UPPER(a.advertiserName)";
		
		return session.createQuery( hql ).setParameterList("advertiserIds", advertiserIds).list();
	}
	
	public static List getAds(List advertiserIds){
		List result = null;
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT ad From Ad as ad "
				+ "WHERE ad.advertiser.advertiserId IN (:advertiserIds)";
		
		result = session.createQuery(hql).setParameterList("advertiserIds", advertiserIds).list();
		return result;
	}
	
	public static int getAdvertiserCount(){
		int result = 0;
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT count(a) "
				+ "FROM Advertiser as a";
		
		Iterator i = session.createQuery(hql).iterate();
		result = ( (Long) i.next() ).intValue();
		Hibernate.close( i );
		return result;
	}
	
	public Long getEntityId()
	{
		return this.getAdvertiserId();
	}
	
	public static Advertiser getAdvertiser(Long advertiserId){
		return (Advertiser)Entity.load(Advertiser.class, advertiserId);
	}
	
	public Long getAdvertiserId() {
		return advertiserId;
	}
	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
	}
	public String getAdvertiserName() {
		return advertiserName;
	}
	public void setAdvertiserName(String advertiserName) {
		this.advertiserName = advertiserName;
	}
}
