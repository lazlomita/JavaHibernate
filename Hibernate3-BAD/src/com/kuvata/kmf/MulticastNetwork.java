package com.kuvata.kmf;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;

import parkmedia.usertype.StatusType;


public class MulticastNetwork extends Entity{

	private Long multicastNetworkId;
	private String name;
	private String multicastAddress;
	private Integer bandwidth;
	private Integer serverPort;
	private Integer devicePort;
	
	public static List<MulticastNetwork> getMulticastNetworks(){
		
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT mn "
				+ "FROM MulticastNetwork as mn "
				+ "ORDER BY UPPER(mn.name)";
		
		List l = session.createQuery( hql ).list();
		
		return l;
	}
	
	public static Long create(String name, String address, int bandwidth, int serverPort, int devicePort){
		MulticastNetwork mn = new MulticastNetwork();
		mn.name = name;
		mn.multicastAddress = address;
		mn.bandwidth = bandwidth;
		mn.serverPort = serverPort;
		mn.devicePort = devicePort;
		return mn.save();
	}
	
	public void update(String name, String address, int bandwidth, int serverPort, int devicePort){
		this.name = name;
		this.multicastAddress = address;
		this.bandwidth = bandwidth;
		this.serverPort = serverPort;
		this.devicePort = devicePort;
		this.update();
	}
	
	public static MulticastNetwork getMulticastNetwork(Long multicastNetworkId){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT mn "
				+ "FROM MulticastNetwork as mn "
				+ "WHERE mn.multicastNetworkId = :id";
		
		return (MulticastNetwork)session.createQuery( hql ).setParameter("id", multicastNetworkId).uniqueResult();
	}
	
	public static MulticastNetwork getMulticastNetwork(String multicastAddress, Integer serverPort, Integer devicePort){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT mn "
				+ "FROM MulticastNetwork as mn "
				+ "WHERE mn.multicastAddress = :address AND mn.serverPort = :serverPort AND mn.devicePort = :devicePort";
		
		return (MulticastNetwork)session.createQuery( hql ).setParameter("address", multicastAddress).setParameter("serverPort", serverPort).setParameter("devicePort", devicePort).uniqueResult();
	}
	
	public static List<MulticastNetwork> getMulticastNetworkMatchingAtleastOneProperty(String name, String multicastAddress, Integer serverPort){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT mn "
				+ "FROM MulticastNetwork as mn "
				+ "WHERE mn.name = :name OR mn.multicastAddress = :address OR mn.serverPort = :serverPort";
		
		return session.createQuery( hql ).setParameter("address", multicastAddress).setParameter("serverPort", serverPort).setParameter("name", name).list();
	}
	
	public List getDeviceNames(){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT d.deviceName "
				+ "FROM Device as d "
				+ "WHERE d.multicastNetwork.multicastNetworkId = :multicastNetworkId ORDER BY UPPER(d.deviceName)";
		
		return session.createQuery( hql ).setParameter("multicastNetworkId", this.multicastNetworkId).list();
	}
	
	public List<Device> getDevices(){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT d "
				+ "FROM Device as d "
				+ "WHERE d.multicastNetwork.multicastNetworkId = :multicastNetworkId";
		
		return session.createQuery( hql ).setParameter("multicastNetworkId", this.multicastNetworkId).list();
	}
	
	// Get a list of multicast files that are not set to success and all the multicast files created in the last day
	public List<MulticastNetworkFiles> getRecentMulticastNetworkFiles(){
		Session session = HibernateSession.currentSession();
		
		String hql = "SELECT mnf "
				+ "FROM MulticastNetworkFiles as mnf "
				+ "WHERE mnf.multicastNetwork.multicastNetworkId = :multicastNetworkId AND "
				+ "(mnf.createDt >= :yesterday OR mnf.status != '" + StatusType.SUCCESS.getPersistentValue() + "') "
				+ "AND mnf.fileloc NOT LIKE 'multicast_tests%' "
				+ "ORDER BY mnf.status, mnf.createDt DESC";
		
		return session.createQuery( hql ).setParameter("multicastNetworkId", this.multicastNetworkId).setParameter("yesterday", new Date(System.currentTimeMillis() - Constants.MILLISECONDS_IN_A_DAY)).list();
	}
	
	public Long getEntityId(){
		return multicastNetworkId;
	}
	public Long getMulticastNetworkId() {
		return multicastNetworkId;
	}
	public void setMulticastNetworkId(Long multicastNetworkId) {
		this.multicastNetworkId = multicastNetworkId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMulticastAddress() {
		return multicastAddress;
	}
	public void setMulticastAddress(String multicastAddress) {
		this.multicastAddress = multicastAddress;
	}
	public Integer getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(Integer bandwidth) {
		this.bandwidth = bandwidth;
	}
	public Integer getServerPort() {
		return serverPort;
	}
	public void setServerPort(Integer serverPort) {
		this.serverPort = serverPort;
	}
	public Integer getDevicePort() {
		return devicePort;
	}
	public void setDevicePort(Integer devicePort) {
		this.devicePort = devicePort;
	}
}
